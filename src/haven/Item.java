/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ender.CurioInfo;

public class Item extends Widget implements DTarget {
    static Coord shoff = new Coord(1, 3);
    static final Pattern patt = Pattern.compile("quality (\\d+) ", Pattern.CASE_INSENSITIVE);
    static Map<Integer, Tex> qmap;
    static Resource missing = Resource.load("gfx/invobjs/missing");
    static Color outcol = new Color(0,0,0,255);
    boolean dm = false;
    int q, q2;
    boolean hq;
    Coord doff;
    String tooltip;
    int num = -1;
    Indir<Resource> res;
    Tex sh;
    Color olcol = null;
    Tex mask = null;
    int meter = 0;
    String curioStr = null;
	
    static {
	Widget.addtype("item", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    int res = (Integer)args[0];
		    int q = (Integer)args[1];
		    int num = -1;
		    String tooltip = null;
		    int ca = 3;
		    Coord drag = null;
		    if((Integer)args[2] != 0)
			drag = (Coord)args[ca++];
		    if(args.length > ca)
			tooltip = (String)args[ca++];
		    if((tooltip != null) && tooltip.equals(""))
			tooltip = null;
		    if(args.length > ca)
			num = (Integer)args[ca++];
		    Item item = new Item(c, res, q, parent, drag, num);
		    item.settip(tooltip);
		    return(item);
		}
	    });
	missing.loadwait();
	qmap = new HashMap<Integer, Tex>();
    }
    
    public void settip(String t){
	tooltip = t;
	q2 = -1;
	if(tooltip != null){
	    try{
		Matcher m =patt.matcher(tooltip); 
		while(m.find()){
		    q2 = Integer.parseInt(m.group(1));
		}
	    } catch(IllegalStateException e){
		System.out.println(e.getMessage());
	    }
	}
	calcFEP();
	calcCurio();
	shorttip = longtip = null;
    }
    
    private void fixsize() {
	if(res.get() != null) {
	    Tex tex = res.get().layer(Resource.imgc).tex();
	    sz = tex.sz().add(shoff);
	} else {
	    sz = new Coord(30, 30);
	}
    }

    public void draw(GOut g) {
	final Resource ttres;
	if(res.get() == null) {
	    sh = null;
	    sz = new Coord(30, 30);
	    g.image(missing.layer(Resource.imgc).tex(), Coord.z, sz);
	    ttres = missing;
	} else {
	    Tex tex = res.get().layer(Resource.imgc).tex();
	    fixsize();
	    if(dm) {
		g.chcolor(255, 255, 255, 128);
		g.image(tex, Coord.z);
		g.chcolor();
	    } else {
		g.image(tex, Coord.z);
	    }
	    if(num >= 0) {
		//g.chcolor(Color.WHITE);
		//g.atext(Integer.toString(num), new Coord(0, 30), 0, 1);
		g.aimage(getqtex(num), Coord.z, 0, 0);
	    }
	    if(meter > 0) {
		double a = ((double)meter) / 100.0;
		int r = (int) ((1-a)*255);
		int gr = (int) (a*255);
		int b = 0;
		g.chcolor(r, gr, b, 255);
		//g.fellipse(sz.div(2), new Coord(15, 15), 90, (int)(90 + (360 * a)));
		g.frect(new Coord(sz.x-5,(int) ((1-a)*sz.y)), new Coord(5,(int) (a*sz.y)));
		g.chcolor();
	    }
	    int tq = (q2>0)?q2:q;
	    if(Config.showq && (tq > 0)){
		tex = getqtex(tq);
		g.aimage(tex, sz.sub(1,1), 1, 1);
	    }
	    ttres = res.get();
	}
	if(olcol != null) {
	    Tex bg = ttres.layer(Resource.imgc).tex();
	    if((mask == null) && (bg instanceof TexI)) {
		mask = ((TexI)bg).mkmask();
	    }
	    if(mask != null) {
		g.chcolor(olcol);
		g.image(mask, Coord.z);
		g.chcolor();
	    }
	}
	if(FEP == null){calcFEP();}
	if(curioStr == null){calcCurio();}
    }

    static Tex getqtex(int q){
	synchronized (qmap) {
	    if(qmap.containsKey(q)){
		return qmap.get(q);
	    } else {
		BufferedImage img = Text.render(Integer.toString(q)).img;
		img = Utils.outline2(img, outcol, true);
		Tex tex = new TexI(img);
		qmap.put(q, tex);
		return tex;
	    }
	}
    }
    
    static Tex makesh(Resource res) {
	BufferedImage img = res.layer(Resource.imgc).img;
	Coord sz = Utils.imgsz(img);
	BufferedImage sh = new BufferedImage(sz.x, sz.y, BufferedImage.TYPE_INT_ARGB);
	for(int y = 0; y < sz.y; y++) {
	    for(int x = 0; x < sz.x; x++) {
		long c = img.getRGB(x, y) & 0x00000000ffffffffL;
		int a = (int)((c & 0xff000000) >> 24);
		sh.setRGB(x, y, (a / 2) << 24);
	    }
	}
	return(new TexI(sh));
    }
    
    public String name() {
	Resource res = this.res.get();
	if(res != null){
	    if(res.layer(Resource.tooltip) != null) {
		return res.layer(Resource.tooltip).t;
	    } else {
		return(this.tooltip);
	    }
	}
	return null;
    }
    
    public String shorttip() {
	if(this.tooltip != null)
	    return(this.tooltip);
	Resource res = this.res.get();
	if((res != null) && (res.layer(Resource.tooltip) != null)) {
	    String tt = res.layer(Resource.tooltip).t;
	    if(tt != null) {
		if(q > 0) {
		    tt = tt + ", quality " + q;
		    if(hq)
			tt = tt + "+";
		}
		return(tt);
	    }
	}
	return(null);
    }
    
    long hoverstart;
    Text shorttip = null, longtip = null;
    public double qmult;
    private String FEP = null;
    public Object tooltip(Coord c, boolean again) {
	long now = System.currentTimeMillis();
	if(!again)
	    hoverstart = now;
	Resource res = this.res.get();
	Resource.Pagina pg = (res!=null)?res.layer(Resource.pagina):null;
	if(((now - hoverstart) < 500)||(pg == null)) {
	    if(shorttip == null) {
		String tt = shorttip();
		if(tt != null) {
		    tt = RichText.Parser.quote(tt);
		    if(meter > 0) {
			tt = tt + " (" + meter + "%)";
		    }
		    if(FEP != null){
			tt += FEP;
		    }
		    if(curioStr != null){
			tt += curioStr;
		    }
		    shorttip = RichText.render(tt, 200);
		}
	    }
	    return(shorttip);
	} else {
	    if((longtip == null) && (res != null)) {
		String tip = shorttip();
		if(tip == null)
		    return(null);
		String tt = RichText.Parser.quote(tip);
		if(meter > 0) {
		    tt = tt + " (" + meter + "%)";
		}
		if(FEP != null){
		    tt += FEP;
		}
		if(curioStr != null){
		    tt += curioStr;
		}
		if(pg != null)
		    tt += "\n\n" + pg.text;
		longtip = RichText.render(tt, 200);
	    }
	    return(longtip);
	}
    }
    
    private void resettt() {
	shorttip = null;
	longtip = null;
    }

    private void decq(int q)
    {
	if(q < 0) {
	    this.q = q;
	    hq = false;
	} else {
	    int fl = (q & 0xff000000) >> 24;
	    this.q = (q & 0xffffff);
	    hq = ((fl & 1) != 0);
	}
    }

    public Item(Coord c, Indir<Resource> res, int q, Widget parent, Coord drag, int num) {
	super(c, Coord.z, parent);
	this.res = res;
	decq(q);
	fixsize();
	this.num = num;
	if(drag == null) {
	    dm = false;
	} else {
	    dm = true;
	    doff = drag;
	    ui.grabmouse(this);
	    this.c = ui.mc.add(doff.inv());
	}
	qmult = Math.sqrt((float)q/10);
	calcFEP();
	calcCurio();
    }

    private void calcFEP() {
	Map<String, Float> fep;
	String name = name();
	if(name == null){return;}
	if(name.equals("Ring of Brodgar")){
	    if(res.get().name.equals("gfx/invobjs/bread-brodgar")){name = "Ring of Brodgar (Baking)";}
	    if(res.get().name.equals("gfx/invobjs/feast-rob")){name = "Ring of Brodgar (Seafood)";}
	}
	name = name.toLowerCase();
	boolean isItem = false;
	if((fep = Config.FEPMap.get(name)) != null){
	    if(fep.containsKey("isItem")){
		isItem = true;
	    }
	    FEP = "\n";
	    for(String key:fep.keySet()){
		float val = (float) (fep.get(key)*qmult);
		if(key.equals("isItem")){continue;}
		if(isItem){
		    val = (float) Math.floor(val);
		    FEP += String.format("%s:%.0f ", key, val);
		} else {
		    FEP += String.format("%s:%.1f ", key, val);
		}
	    }
	    shorttip = longtip = null;
	}
    }
    
    public int getLP() {
	String name = name();
	if(name == null){return 0;}
	name = name.toLowerCase();
	CurioInfo curio;
	if((curio = Config.curios.get(name)) != null){
	    return (int) (curio.LP*qmult*ui.sess.glob.cattr.get("expmod").comp/100);
	}
	return 0;
    }
    
    private void calcCurio(){
	String name = name();
	if(name == null){return;}
	name = name.toLowerCase();
	CurioInfo curio;
	if((curio = Config.curios.get(name)) != null){
	    int LP = (int) (curio.LP*qmult*ui.sess.glob.cattr.get("expmod").comp/100);
	    int time = curio.time*(100 - meter)/100;
	    int h = time/60;
	    int m = time%60;
	    curioStr = String.format("\nLP: %d, Weight: %d\nStudy time: %dh %2dm", LP,curio.weight,h,m);
	    shorttip = longtip = null;
	}
    }

    public Item(Coord c, int res, int q, Widget parent, Coord drag, int num) {
	this(c, parent.ui.sess.getres(res), q, parent, drag, num);
    }

    public Item(Coord c, Indir<Resource> res, int q, Widget parent, Coord drag) {
	this(c, res, q, parent, drag, -1);
    }
	
    public Item(Coord c, int res, int q, Widget parent, Coord drag) {
	this(c, parent.ui.sess.getres(res), q, parent, drag);
    }

    public boolean dropon(Widget w, Coord c) {
	for(Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
	    if(wdg == this)
		continue;
	    Coord cc = w.xlate(wdg.c, true);
	    if(c.isect(cc, (wdg.hsz == null)?wdg.sz:wdg.hsz)) {
		if(dropon(wdg, c.add(cc.inv())))
		    return(true);
	    }
	}
	if(w instanceof DTarget) {
	    if(((DTarget)w).drop(c, c.add(doff.inv())))
		return(true);
	}
	if(w instanceof DTarget2) {
	    if(((DTarget2)w).drop(c, c.add(doff.inv()), this))
		return(true);
	}
	return(false);
    }
	
    public boolean interact(Widget w, Coord c) {
	for(Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
	    if(wdg == this)
		continue;
	    Coord cc = w.xlate(wdg.c, true);
	    if(c.isect(cc, (wdg.hsz == null)?wdg.sz:wdg.hsz)) {
		if(interact(wdg, c.add(cc.inv())))
		    return(true);
	    }
	}
	if(w instanceof DTarget) {
	    if(((DTarget)w).iteminteract(c, c.add(doff.inv())))
		return(true);
	}
	return(false);
    }
	
    public void chres(Indir<Resource> res, int q) {
	this.res = res;
	sh = null;
	decq(q);
    }

    public void uimsg(String name, Object... args)  {
	if(name == "num") {
	    num = (Integer)args[0];
	} else if(name == "chres") {
	    chres(ui.sess.getres((Integer)args[0]), (Integer)args[1]);
	    resettt();
	} else if(name == "color") {
	    olcol = (Color)args[0];
	} else if(name == "tt") {
	    if((args.length > 0) && (((String)args[0]).length() > 0))
		settip((String)args[0]);
	    else
		settip(null);
	    resettt();
	} else if(name == "meter") {
	    meter = (Integer)args[0];
	    shorttip = null;
	    longtip = null;
	    calcCurio();
	}
    }
	
    public boolean mousedown(Coord c, int button) {
	if(!dm) {
	    if(button == 1) {
		if(ui.modshift)
		    wdgmsg("transfer", c);
		else if(ui.modctrl)
		    wdgmsg("drop", c);
		else
		    wdgmsg("take", c);
		return(true);
	    } else if(button == 3) {
		wdgmsg("iact", c);
		return(true);
	    }
	} else {
	    if(button == 1) {
		dropon(parent, c.add(this.c));
	    } else if(button == 3) {
		interact(parent, c.add(this.c));
	    }
	    return(true);
	}
	return(false);
    }

    public void mousemove(Coord c) {
	if(dm)
	    this.c = this.c.add(c.add(doff.inv()));
    }
	
    public boolean drop(Coord cc, Coord ul) {
	return(false);
    }
	
    public boolean iteminteract(Coord cc, Coord ul) {
	wdgmsg("itemact", ui.modflags());
	return(true);
    }
}
