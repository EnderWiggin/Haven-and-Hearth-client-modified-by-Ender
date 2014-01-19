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
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ender.GoogleTranslator;

public class Window extends Widget implements DTarget {
    static Tex bg = Resource.loadtex("gfx/hud/bgtex");
    static Tex cl = Resource.loadtex("gfx/hud/cleft");
    static Tex cm = Resource.loadtex("gfx/hud/cmain");
    static Tex cr = Resource.loadtex("gfx/hud/cright");
    private final static Set<String> storePosSet = new HashSet<String>();
    protected static BufferedImage[] cbtni = new BufferedImage[] {
	Resource.loadimg("gfx/hud/cbtn"),
	Resource.loadimg("gfx/hud/cbtnd"),
	Resource.loadimg("gfx/hud/cbtnh")}; 
    protected static BufferedImage[] fbtni = new BufferedImage[] {
	Resource.loadimg("gfx/hud/fbtn"),
	Resource.loadimg("gfx/hud/fbtnd"),
	Resource.loadimg("gfx/hud/fbtnh")}; 
    static Color cc = Color.YELLOW;
    static Text.Foundry cf = new Text.Foundry(new Font("Serif", Font.PLAIN, 12));
    static IBox wbox;
    boolean dt = false;
    public boolean justclose = false;
    public Text cap;
    boolean dm = false;
    public Coord atl, asz, wsz = new Coord();
    public Coord tlo, rbo;
    public Coord mrgn = new Coord(13, 13);
    public Coord doff;
    public IButton cbtn;
    public IButton fbtn;
    public boolean folded;
    ArrayList<Widget> wfolded;
    protected Coord ssz;
	
    static {
	Widget.addtype("wnd", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    if(args.length < 2)
			return(new Window(c, (Coord)args[0], parent, null));
		    else
			return(new Window(c, (Coord)args[0], parent, (String)args[1]));
		}
	    });
	wbox = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");
	storePosSet.add("Inventory");
	storePosSet.add("Cupboard");
	storePosSet.add("Equipment");
	storePosSet.add("Character Sheet");
	storePosSet.add("Kin");
	storePosSet.add("Study");
    }

    protected void placecbtn() {
	cbtn.c = new Coord(wsz.x - 3 - Utils.imgsz(cbtni[0]).x, 3).add(mrgn.inv().add(wbox.tloff().inv()));
	fbtn.c = new Coord(cbtn.c.x - 1 - Utils.imgsz(fbtni[0]).x, cbtn.c.y);
    }
	
    public Window(Coord c, Coord sz, Widget parent, String cap, Coord tlo, Coord rbo) {
	super(c, new Coord(0, 0), parent);
	this.tlo = tlo;
	this.rbo = rbo;
	cbtn = new IButton(Coord.z, this, cbtni[0], cbtni[1], cbtni[2]);
	fbtn = new IButton(Coord.z, this, fbtni[0], fbtni[1], fbtni[2]);
	fbtn.hide();
	folded = false;
	wfolded = new ArrayList<Widget>();
	if(cap != null)
	    this.cap = cf.render(cap, cc);
	ssz = new Coord(sz);
	sz = sz.add(tlo).add(rbo).add(wbox.bisz()).add(mrgn.mul(2));
	this.sz = sz;
	atl = new Coord(wbox.bl.sz().x, wbox.bt.sz().y).add(tlo);
	wsz = sz.add(tlo.inv()).add(rbo.inv());
	asz = new Coord(wsz.x - wbox.bl.sz().x - wbox.br.sz().x - mrgn.x, wsz.y - wbox.bt.sz().y - wbox.bb.sz().y - mrgn.y);
	placecbtn();
	setfocustab(true);
	parent.setfocus(this);
	loadpos();
    }
	
    public Window(Coord c, Coord sz, Widget parent, String cap) {
	this(c, sz, parent, cap, new Coord(0, 0), new Coord(0, 0));
    }
	
    public void cdraw(GOut g) {
    }
	
    public void draw(GOut og) {
	GOut g = og.reclip(tlo, wsz);
	Coord bgc = new Coord();
	for(bgc.y = 3; bgc.y < wsz.y - 6; bgc.y += bg.sz().y) {
	    for(bgc.x = 3; bgc.x < wsz.x - 6; bgc.x += bg.sz().x)
		g.image(bg, bgc, new Coord(3, 3), wsz.add(new Coord(-6, -6)));
	}
	cdraw(og.reclip(xlate(Coord.z, true), sz));
	wbox.draw(g, Coord.z, wsz);
	if(cap != null) {
	    GOut cg = og.reclip(new Coord(0, -7), sz.add(0, 7));
	    int w = cap.tex().sz().x;
		int x0 = (folded)?(mrgn.x + (w / 2)):(sz.x / 2) - (w / 2);
	    cg.image(cl, new Coord(x0 - cl.sz().x, 0));
	    cg.image(cm, new Coord(x0, 0), new Coord(w, cm.sz().y));
	    cg.image(cr, new Coord(x0 + w, 0));
	    cg.image(cap.tex(), new Coord(x0, 0));
	}
	super.draw(og);
    }
	
    public void checkfold() {
	for(Widget wdg = child; wdg != null; wdg = wdg.next) {
	    if((wdg == cbtn)||(wdg == fbtn))
		continue;
		if(folded) {
		    if(wdg.visible) {
			wdg.hide();
			wfolded.add(wdg);
		    }
		} else if (wfolded.contains(wdg)){
		    wdg.show();
		}
	}
	Coord max = new Coord(ssz);
	if(folded) {
	    max.y = 0;
	} else {
	    wfolded.clear();
	}
	
	recalcsz(max);
    }
    
    protected void recalcsz(Coord max)
    {
	sz = max.add(wbox.bsz().add(mrgn.mul(2)).add(tlo).add(rbo)).add(-1, -1);
	wsz = sz.sub(tlo).sub(rbo);
	if(folded)
	    wsz.y = wsz.y/2;
	asz = wsz.sub(wbox.bl.sz()).sub(wbox.br.sz()).sub(mrgn.mul(2));
    }
    
    public void pack() {
	boolean isrunestone = cap.text.equals("Runestone"); 
	Coord max = new Coord(0, 0);
	for(Widget wdg = child; wdg != null; wdg = wdg.next) {
	    if((wdg == cbtn)||(wdg == fbtn))
		continue;
	    if((isrunestone)&&(wdg instanceof Label)) {
		Label lbl = (Label)wdg;
		lbl.settext(GoogleTranslator.translate(lbl.texts));
	    }
	    Coord br = wdg.c.add(wdg.sz);
	    if(br.x > max.x)
		max.x = br.x;
	    if(br.y > max.y)
		max.y = br.y;
	}
	ssz = max;
	checkfold();
	placecbtn();
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "pack") {
	    pack();
	} else if(msg == "dt") {
	    dt = (Integer)args[0] != 0;
	} else {
	    super.uimsg(msg, args);
	}
    }
	
    public Coord xlate(Coord c, boolean in) {
	Coord ctl = wbox.tloff();
	if(in)
	    return(c.add(ctl).add(tlo).add(mrgn));
	else
	    return(c.add(ctl.inv()).add(tlo.inv()).add(mrgn.inv()));
    }
	
    public boolean mousedown(Coord c, int button) {
	parent.setfocus(this);
	raise();
	if(super.mousedown(c, button))
	    return(true);
	if(!c.isect(tlo, sz.add(tlo.inv()).add(rbo.inv())))
	    return(false);
	if(button == 1) {
	    ui.grabmouse(this);
	    dm = true;
	    doff = c;
	}
	return(true);
    }
	
    public boolean mouseup(Coord c, int button) {
	if(dm) {
	    ui.grabmouse(null);
	    dm = false;
	    storepos();
	} else {
	    super.mouseup(c, button);
	}
	return(true);
    }
	
    public void mousemove(Coord c) {
	if(dm) {
	    this.c = this.c.add(c.add(doff.inv()));
	} else {
	    super.mousemove(c);
	}
    }
    
    private void storepos(){
	if(cap == null){return;}
	String name = cap.text;
	if(storePosSet.contains(name)){
	    Config.setWindowOpt(name+"_pos", c.toString());
	    return;
	}
    }
    
    private void loadpos(){
	if(cap == null){return;}
	String name = cap.text;
	if(storePosSet.contains(name)){
	    c = new Coord(Config.window_props.getProperty(name+"_pos", c.toString()));
	    return;
	}
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == cbtn) {
	    if(justclose)
		ui.destroy(this);
	    else
		wdgmsg("close");
	} else if(sender == fbtn){
		folded = !folded;
		checkfold();
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
	
    public boolean type(char key, java.awt.event.KeyEvent ev) {
	if(key == 27) {
	    if(justclose)
		ui.destroy(this);
	    else
		wdgmsg("close");
	    return(true);
	}
	return(super.type(key, ev));
    }
	
    public boolean drop(Coord cc, Coord ul) {
	if(dt) {
	    wdgmsg("drop", cc);
	    return(true);
	}
	return(false);
    }
	
    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }
    
    public Object tooltip(Coord c, boolean again) {
	Object ret = super.tooltip(c, again);
	if(ret != null)
	    return(ret);
	else
	    return("");
    }
	
	public void moveWindowToView(){
		Coord innerSZ = MainFrame.getInnerSize();
		if(c.x < 0) c.x = 0;
		if(c.y < 0) c.y = 0;
		if( (c.x + wsz.x) > innerSZ.x ) c.x = innerSZ.x - wsz.x;
		if( (c.y + wsz.y) > innerSZ.y ) c.y = innerSZ.y - wsz.y;
	}
}
