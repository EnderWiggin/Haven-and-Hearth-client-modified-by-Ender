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

import static haven.MCache.cmaps;
import static haven.MCache.tilesz;
import haven.MCache.Grid;
import haven.MCache.Overlay;
import haven.Resource.Tile;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ender.HLInfo;

public class MapView extends Widget implements DTarget, Console.Directory {
    static Color[] olc = new Color[31];
    static Map<String, Class<? extends Camera>> camtypes = new HashMap<String, Class<? extends Camera>>();
    public Coord mc, mousepos, pmousepos;
    Camera cam;
    Sprite.Part[] clickable = {};
    List<Sprite.Part> obscured = Collections.emptyList();
    private int[] visol = new int[31];
    private long olftimer = 0;
    private int olflash = 0;
    Grabber grab = null;
    ILM mask;
    final MCache map;
    public final Glob glob;
    Collection<Gob> plob = null;
    boolean plontile;
    int plrad = 0;
    public int playergob = -1;
    public Profile prof = new Profile(300);
    private Profile.Frame curf;
    Coord plfpos = null;
    long lastmove = 0;
    Sprite.Part obscpart = null;
    Gob obscgob = null;
    static Text.Foundry polownertf = new Text.Foundry("serif", 20);
    public Text polownert = null;
    public String polowner = null;
    public Gob onmouse;
    public Coord moveto = null;
    long polchtm = 0;
    int si = 4;
    double _scale = 1;
    double scales[] = {0.4, 0.5, 0.66, 0.8, 0.9, 1, 1.25, 1.5, 1.75, 2, 2.25}; // new
	double smoothScale = 1;
    Map<String, Integer> radiuses;
    int beast_check_delay = 0;
	long lastah = 0;
    
	static boolean fixCameraBug = false; // new
	public static Gob gobAtMouse; // new
	
    public double getScale() {
        return Config.zoom?_scale:1;
    }
    
    public void setScale(double value) {
	_scale = value;
	mask.setScale(value);
    }

    public static final Comparator<Sprite.Part> clickcmp = new Comparator<Sprite.Part>() {
	public int compare(Sprite.Part a, Sprite.Part b) {
	    return(-Sprite.partidcmp.compare(a, b));
	}
    };
    
    static {
	Widget.addtype("mapview", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    Coord sz = MainFrame.getInnerSize();
		    Coord mc = (Coord)args[1];
		    int pgob = -1;
		    if(args.length > 2)
			pgob = (Integer)args[2];
		    return(new MapView(c, sz, parent, mc, pgob));
		}
	    });
	olc[0] = new Color(255, 0, 128);
	olc[1] = new Color(0, 0, 255);
	olc[2] = new Color(255, 0, 0);
	olc[3] = new Color(128, 0, 255);
	olc[16] = new Color(0, 255, 0);
	olc[17] = new Color(255, 255, 0);
    }
    
    public interface Grabber {
	void mmousedown(Coord mc, int button);
	void mmouseup(Coord mc, int button);
	void mmousemove(Coord mc);
    }
    
    @SuppressWarnings("serial")
    public static class GrabberException extends RuntimeException{}
    
    public static class Camera {
	public void setpos(MapView mv, Gob player, Coord sz) {}
	
	public boolean click(MapView mv, Coord sc, Coord mc, int button) {
	    return(false);
	}
	
	public void move(MapView mv, Coord sc, Coord mc) {}
	
	public boolean release(MapView mv, Coord sc, Coord mc, int button) {
	    return(false);
		}
	
	public void moved(MapView mv) {}
	
	public static void borderize(MapView mv, Gob player, Coord sz, Coord border) {
	    if(Config.noborders){return;}
	    Coord mc = mv.mc;
	    Coord oc = m2s(mc).inv();
	    int bt = -((sz.y / 2) - border.y);
	    int bb = (sz.y / 2) - border.y;
	    int bl = -((sz.x / 2) - border.x);
	    int br = (sz.x / 2) - border.x;
	    Coord sc = m2s(player.getc()).add(oc);
	    if(sc.x < bl)
		mc = mc.add(s2m(new Coord(sc.x - bl, 0)));
	    if(sc.x > br)
		mc = mc.add(s2m(new Coord(sc.x - br, 0)));
	    if(sc.y < bt)
		mc = mc.add(s2m(new Coord(0, sc.y - bt)));
	    if(sc.y > bb)
		mc = mc.add(s2m(new Coord(0, sc.y - bb)));
	    mv.mc = mc;
	}

	public void reset() {}
    }
    
    private static abstract class DragCam extends Camera {
	Coord o, mo;
	boolean dragging = false;
	boolean needreset = false;
	
	public boolean click(MapView mv, Coord sc, Coord mc, int button) {
	    if(button == 2) {
		mv.ui.grabmouse(mv);
		o = sc;
		mo = null;
		dragging = true;
		return(true);
	    }
	    return(false);
	}
	
	public void move(MapView mv, Coord sc, Coord mc) {
	    if(dragging) {
		Coord off = sc.add(o.inv());
		if((mo == null) && (off.dist(Coord.z) > 5))
		    mo = mv.mc;
		if(mo != null) {
		    mv.mc = mo.add(s2m(off).inv());
		    moved(mv);
		}
	    }
	}
	
	public void reset(){
	    needreset = true;
	}
	
	public boolean release(MapView mv, Coord sc, Coord mc, int button) {
	    if((button == 2) && dragging) {
		mv.ui.grabmouse(null);
		dragging = false;
		if(mo == null) {
		    mv.mc = mc;
		    moved(mv);
		}
		return(true);
	    }
	    return(false);
	}
    }
    
    static class OrigCam extends Camera {
	public final Coord border = new Coord(250, 150);
	
	public void setpos(MapView mv, Gob player, Coord sz) {
	    borderize(mv, player, sz, border);
	}
	
	public boolean click(MapView mv, Coord sc, Coord mc, int button) {
	    if(button == 1)
		mv.mc = mc;
	    return(false);
	}
    }
    static {camtypes.put("orig", OrigCam.class);}

    static class OrigCam2 extends DragCam {
	public final Coord border = new Coord(250, 125);
	private final double v;
	private Coord tgt = null;
	private long lmv;
	
	public OrigCam2(double v) {
	    this.v = Math.log(v) / 0.02; /* 1 / 50 FPS = 0.02 s */
	}
	
	public OrigCam2() {
	    this(0.9);
	}
	
	public OrigCam2(String... args) {
	    this((args.length < 1)?0.9:Double.parseDouble(args[0]));
	}
	
	public void setpos(MapView mv, Gob player, Coord sz) {
	    
	    if(needreset){
		needreset = false;
		mv.mc = player.getc();
	    }
	    
	    if(tgt != null) {
		if(mv.mc.dist(tgt) < 10) {
		    tgt = null;
		} else {
		    long now = System.currentTimeMillis();
		    double dt = (now - lmv) / 1000.0;
		    lmv = now;
		    mv.mc = tgt.add(mv.mc.add(tgt.inv()).mul(Math.exp(v * dt)));
		}
	    }
	    borderize(mv, player, sz, border);
	}
	
	public boolean click(MapView mv, Coord sc, Coord mc, int button) {
	    if((button == 1) && (mv.ui.root.cursor == RootWidget.defcurs)) {
		tgt = mc;
		lmv = System.currentTimeMillis();
	    }
	    return(super.click(mv, sc, mc, button));
	}
	
	public void moved(MapView mv) {
	    tgt = null;
	}
	
	public void reset() {
	    super.reset();
	    tgt = null;
	}
    }
    static {camtypes.put("clicktgt", OrigCam2.class);}

    static class WrapCam extends Camera {
	public final Coord region = new Coord(200, 150);
	
	public void setpos(MapView mv, Gob player, Coord sz) {
	    Coord sc = m2s(player.getc().add(mv.mc.inv()));
	    if(sc.x < -region.x)
		mv.mc = mv.mc.add(s2m(new Coord(-region.x * 2, 0)));
	    if(sc.x > region.x)
		mv.mc = mv.mc.add(s2m(new Coord(region.x * 2, 0)));
	    if(sc.y < -region.y)
		mv.mc = mv.mc.add(s2m(new Coord(0, -region.y * 2)));
	    if(sc.y > region.y)
		mv.mc = mv.mc.add(s2m(new Coord(0, region.y * 2)));
	}
    }
    static {camtypes.put("kingsquest", WrapCam.class);}

    static class BorderCam extends DragCam {
	public final Coord border = new Coord(250, 150);

	public void setpos(MapView mv, Gob player, Coord sz) {
	    if(needreset){
		needreset = false;
		mv.mc = player.getc();
	    }
	    borderize(mv, player, sz, border);
	}
    }
    static {camtypes.put("border", BorderCam.class);}
    
    static class PredictCam extends DragCam {
	private double xa = 0, ya = 0;
	private boolean reset = true;
	private final double speed = 0.15, rspeed = 0.15;
	private double sincemove = 0;
	private long last = System.currentTimeMillis();
	
	public void setpos(MapView mv, Gob player, Coord sz) {
	    long now = System.currentTimeMillis();
	    double dt = ((double)(now - last)) / 1000.0;
	    last = now;
	    
	    if(needreset){
		needreset = false;
		mv.mc = player.getc();
	    }
	    
	    Coord mc = mv.mc.add(s2m(sz.add(mv.sz.inv()).div(2)));
	    Coord sc = m2s(player.getc()).add(m2s(mc).inv());
	    if(reset) {
		xa = (double)sc.x / (double)sz.x;
		ya = (double)sc.y / (double)sz.y;
		if(xa < -0.25) xa = -0.25;
		if(xa > 0.25) xa = 0.25;
		if(ya < -0.15) ya = -0.15;
		if(ya > 0.25) ya = 0.25;
		reset = false;
	    }
	    Coord vsz = sz.div(16);
	    Coord vc = new Coord((int)(sz.x * xa), (int)(sz.y * ya));
	    boolean moved = false;
	    if(sc.x < vc.x - vsz.x) {
		if(xa < 0.25)
		    xa += speed * dt;
		moved = true;
		mc = mc.add(s2m(new Coord(sc.x - (vc.x - vsz.x) - 4, 0)));
	    }
	    if(sc.x > vc.x + vsz.x) {
		if(xa > -0.25)
		    xa -= speed * dt;
		moved = true;
		mc = mc.add(s2m(new Coord(sc.x - (vc.x + vsz.x) + 4, 0)));
	    }
	    if(sc.y < vc.y - vsz.y) {
		if(ya < 0.25)
		    ya += speed * dt;
		moved = true;
		mc = mc.add(s2m(new Coord(0, sc.y - (vc.y - vsz.y) - 2)));
	    }
	    if(sc.y > vc.y + vsz.y) {
		if(ya > -0.15)
		    ya -= speed * dt;
		moved = true;
		mc = mc.add(s2m(new Coord(0, sc.y - (vc.y + vsz.y) + 2)));
	    }
	    if(!moved) {
		sincemove += dt;
		if(sincemove > 1) {
		    if(xa < -0.1)
			xa += rspeed * dt;
		    if(xa > 0.1)
			xa -= rspeed * dt;
		    if(ya < -0.1)
			ya += rspeed * dt;
		    if(ya > 0.1)
			ya -= rspeed * dt;
		}
	    } else {
		sincemove = 0;
	    }
	    mv.mc = mc.add(s2m(mv.sz.add(sz.inv()).div(2)));
	}
	
	public void moved(MapView mv) {
	    reset = true;
	}
	
	public void reset(){
	    reset = true;
	    xa = ya = 0;
	    super.reset();
	}
    }
    static {camtypes.put("predict", PredictCam.class);}
    
    static class FixedCam extends DragCam {
	public final Coord border = new Coord(250, 150);
	private Coord off = Coord.z;
	private boolean setoff = false;
	
	public void setpos(MapView mv, Gob player, Coord sz) {
	    if(setoff) {
			borderize(mv, player, sz, border);
			off = mv.mc.add(player.getc().inv());
			
			if(fixCameraBug){ // new
				fixCameraBug = false;
				off = Coord.z;
			}
			
			setoff = false;
	    }
	    mv.mc = player.getc().add(off);
	}
	
	public void moved(MapView mv) {
	    setoff = true;
	}
	
	public void reset() {
	    off = Coord.z;
	}
    }
    static {camtypes.put("fixed", FixedCam.class);}
    
    static class CakeCam extends Camera {
	private Coord border = new Coord(250, 150);
	private Coord size, center, diff;

	public void setpos(MapView mv, Gob player, Coord sz) {
	    if(size == null || !size.equals(sz)) {
		size = new Coord(sz);
		center = size.div(2);
		diff = center.sub(border);
	    }
	    if(player != null && mv.pmousepos != null)
		mv.mc = player.getc().sub(s2m(center.sub(mv.pmousepos).mul(diff).div(center)));
	}
    }
    static {camtypes.put("cake", CakeCam.class);}

    static class FixedCakeCam extends DragCam {
	public final Coord border = new Coord(250, 150);
	private Coord size, center, diff;
	private boolean setoff = false;
	private Coord off = Coord.z;
	private Coord tgt = null;
	private Coord cur = off;
	private double vel = 0.2;

	public FixedCakeCam(double vel) {
	    this.vel = Math.min(1.0, Math.max(0.1, vel));
	}

	public FixedCakeCam(String... args) {
	    this(args.length < 1 ? 0.2 : Double.parseDouble(args[0]));
	}

	public void setpos(MapView mv, Gob player, Coord sz) {
	    if(setoff) {
		borderize(mv, player, sz, border);
		off = mv.mc.add(player.getc().inv());
		setoff = false;
	    }
	    if(mv.pmousepos != null && (mv.pmousepos.x == 0 || mv.pmousepos.x == sz.x - 1 || mv.pmousepos.y == 0 || mv.pmousepos.y == sz.y - 1)) {
		if(size == null || !size.equals(sz)) {
		    size = new Coord(sz);
		    center = size.div(2);
		    diff = center.sub(border);
		}
		if(player != null && mv.pmousepos != null)
		    tgt = player.getc().sub(s2m(center.sub(mv.pmousepos).mul(diff).div(center))).sub(player.getc());
	    } else {
		tgt = off;
	    }
 	    cur = cur.add(tgt.sub(cur).mul(vel));
 	    mv.mc = player.getc().add(cur);
	}

	public void moved(MapView mv) {
	    setoff = true;
	}
	
	public void reset(){
	    off = new Coord();
	}
    }
    static {camtypes.put("fixedcake", FixedCakeCam.class);}
    
    private class Loading extends Exception {}
    
    private static Camera makecam(Class<? extends Camera> ct, String... args) throws ClassNotFoundException {
	try {
	    try {
		Constructor<? extends Camera> cons = ct.getConstructor(String [].class);
		return(cons.newInstance(new Object[] {args}));
	    } catch(IllegalAccessException e) {
	    } catch(NoSuchMethodException e) {
	    }
	    try {
		return(ct.newInstance());
	    } catch(IllegalAccessException e) {
	    }
	} catch(InstantiationException e) {
	    throw(new Error(e));
	} catch(InvocationTargetException e) {
	    if(e.getCause() instanceof RuntimeException)
		throw((RuntimeException)e.getCause());
	    throw(new RuntimeException(e));
	}
	throw(new ClassNotFoundException("No valid constructor found for camera " + ct.getName()));
    }

    private static Camera restorecam() {
	Class<? extends Camera> ct = camtypes.get(Utils.getpref("defcam", "border"));
	if(ct == null)
	    return(new BorderCam());
	String[] args = (String [])Utils.deserialize(Utils.getprefb("camargs", null));
	if(args == null) args = new String[0];
	try {
	    return(makecam(ct, args));
	} catch(ClassNotFoundException e) {
	    return(new BorderCam());
	}
    }

    public MapView(Coord c, Coord sz, Widget parent, Coord mc, int playergob) {
	super(c, sz, parent);
	isui = false;
	this.mc = mc;
	this.playergob = playergob;
	this.cam = restorecam();
	setcanfocus(true);
	glob = ui.sess.glob;
	map = glob.map;
	mask = new ILM(MainFrame.getScreenSize(), glob.oc);
	radiuses = new HashMap<String, Integer>();
    }
    
    public void resetcam(){
	if(cam != null){
	    cam.reset();
	}
    }
    
    public static Coord m2s(Coord c) {
	return(new Coord((c.x * 2) - (c.y * 2), c.x + c.y));
    }
	
    public static Coord s2m(Coord c) {
	return(new Coord( (int)( (float)c.x / 4 + (float)c.y / 2 ), (int)( (float)c.y / 2 - (float)c.x / 4 ) ) ); // new
    }
	
    static Coord viewoffset(Coord sz, Coord vc) {
	return(m2s(vc).inv().add(sz.div(2)));
    }
	
    public void grab(Grabber grab) {
	this.grab = grab;
    }
	
    public void release(Grabber grab) {
	if(this.grab == grab)
	    this.grab = null;
    }
	
    private Gob gobatpos(Coord c) {
	for(Sprite.Part d : obscured) {
	    Gob gob = (Gob)d.owner;
	    if(gob == null)
		continue;
	    if(d.checkhit(c.add(gob.sc.inv())))
		return(gob);
	}
	for(Sprite.Part d : clickable) {
	    Gob gob = (Gob)d.owner;
	    if(gob == null)
		continue;
	    if(d.checkhit(c.add(gob.sc.inv())))
		return(gob);
	}
	return(null);
    }
	
	void runFlaskScript(int button){ //new flask script
		if(button == 1) Config.runFlask = true;
		if(button == 3)	Config.runFlask = false;
		
		if(Config.runFlaskSuppression){
			Config.runFlask = false;
			Config.runFlaskSuppression = false;
			//System.out.println("runflask Suppressed");
		}
	}
	
	boolean checkMinesuportSafeTile(Coord c){//new
		int minesupportRadius = 100;
		if(map.gettilen(c.div(11) ) != 255) return true;
		
		synchronized(glob.oc){
			for(Gob g : glob.oc){
				if(g.resname().equals("gfx/terobjs/mining/minesupport") ){
					double dist = tilify(c).dist(g.getc() );
					//System.out.println(dist);
					if(dist < minesupportRadius ) return true;
				}
			}
		}
		
		return false;
	}// new
	
    public boolean mousedown(Coord c, int button) {
	int modflag;
	setfocus(this);
	Coord c0 = c;
	c = new Coord((int)(c.x/getScale()), (int)(c.y/getScale()));
	Gob hit = gobatpos(c);
	Coord mc = s2m(c.add(viewoffset(sz, this.mc).inv()));
	if(grab != null) {
	    try{
		grab.mmousedown(mc, button);
		return true;
	    } catch (GrabberException e){}
	}
	
	if(Config.minerSafety && button == 1)
		if(!checkMinesuportSafeTile(mousepos) ) return true;
	
	if(Config.pathDrinker) runFlaskScript(button); //new flask script
	
	modflag = ui.modflags(); // new
	if(modflag == 5) modflag = 0; // manually set all modflags to a new variable
	
	if((cam != null) && cam.click(this, c, mc, button)) {
	    /* Nothing */
	} else if(plob != null) {
	    Gob gob = null;
	    for(Gob g : plob)
		gob = g;
	    wdgmsg("place", gob.rc, button, modflag); // new
	} else {
	    if(hit == null){
		if(modflag == 1 && (button == 1)){ // new
		    glob.oc.enqueue(mc);
		    glob.oc.checkqueue();
		} else {
		    glob.oc.clearqueue();
		    wdgmsg("click", c0, mc, button, modflag); // new
		}
	    } else {
		if(modflag == 4){ // new
		    ui.chat.getawnd().wdgmsg("msg","@$["+hit.id+"]");
		} else {
		    wdgmsg("click", c0, mc, button, modflag, hit.id, hit.getc()); // new
		}
	    }
	}
	return(true);
    }
	
    public boolean mouseup(Coord c, int button) {
	c = new Coord((int)(c.x/getScale()), (int)(c.y/getScale()));
	Coord mc = s2m(c.add(viewoffset(sz, this.mc).inv()));
	if(grab != null) {
	    try {
		grab.mmouseup(mc, button);
		return(true);
	    } catch (GrabberException e){}
	}
	if((cam != null) && cam.release(this, c, mc, button)) {
	    return(true);
	} else {
	    return(true);
	}
    }
	
    public void mousemove(Coord c) {
	c = new Coord((int)(c.x/getScale()), (int)(c.y/getScale()));
	this.pmousepos = c;
	Coord mc = s2m(c.add(viewoffset(sz, this.mc).inv()));
	this.mousepos = mc;
	Gob hit = gobAtMouse = gobatpos(c);
	if(hit == null){
	    tip = null;
	    tips = null;
	}
	Collection<Gob> plob = this.plob;
	if(cam != null)
	    cam.move(this, c, mc);
	
	if(grab != null) {
	    try{
		grab.mmousemove(mc);
		return;
	    } catch (GrabberException e){}
	}
	if(plob != null) {
	    Gob gob = null;
	    for(Gob g : plob)
		gob = g;
	    boolean plontile = this.plontile ^ ui.modshift;
	    gob.move(plontile?tilify(mc):mc);
	} else if(hit != null && ui.modshift){
	    String s;
	    s = "Res found on gob " + hit.id;
	    String names[] = hit.resnames();
	    if(names.length > 0){
		for(String name : names){
		    if(name.contains("gfx/borka")){
			name = name.replace("gfx/borka/","");
			if(name.contains("/"))
			    name = name.substring(0,name.indexOf("/"));
			if(name.equals("body") || name.startsWith("hair") || s.contains(name))
			    continue;
		    }
		    s += "\n"+name;
		}
	    }

	    if(tip == null || !tips.equals(s)){
		tips = s;
		tip = null;
		tooltip(null,false);
	    }
	} 
    }
    
    String tips;
    Text tip = null;
    public Object tooltip(Coord c,boolean again){
	if(tip != null)
	    return(tip);
	if(tips != null && !tips.equals("")){
	    tip = RichText.render(tips,200);
	}
	return(tip);
    }
    
    public boolean mousewheel(Coord c, int amount) {
	if(!Config.zoom)
	    return false;
	
	if(Config.smoothScale){
		smoothScale = smoothScale + smoothScale * 0.01 * amount * -1;
		setScale(smoothScale);
	}else{
		si = Math.min(scales.length-1, Math.max(0, si - amount));
		setScale(scales[si]);
	}
	
	return(true);
    }
	
    public void move(Coord mc) {
	this.mc = mc;
    }
	
    private static Coord tilify(Coord c) {
	c = c.div(tilesz);
	c = c.mul(tilesz);
	c = c.add(tilesz.div(2));
	return(c);
    }
	
    private void unflashol() {
	for(int i = 0; i < visol.length; i++) {
	    if((olflash & (1 << i)) != 0)
		visol[i]--;
	}
	olflash = 0;
	olftimer = 0;
    }

    public void uimsg(String msg, Object... args) {
	if(msg == "move") {
	    move((Coord)args[0]);
	    if(cam != null){ // new
			cam.moved(this);
			fixCameraBug = true;
		}
	} else if(msg == "flashol") {
	    unflashol();
	    olflash = (Integer)args[0];
	    for(int i = 0; i < visol.length; i++) {
		if((olflash & (1 << i)) != 0)
		    visol[i]++;
	    }
	    olftimer = System.currentTimeMillis() + (Integer)args[1];
	} else if(msg == "place") {
	    Collection<Gob> plob = this.plob;
	    if(plob != null) {
		this.plob = null;
		glob.oc.lrem(plob);
	    }
	    plob = new LinkedList<Gob>();
	    plontile = (Integer)args[2] != 0;
	    Gob gob = new Gob(glob, plontile?tilify(mousepos):mousepos);
	    Resource res = Resource.load((String)args[0], (Integer)args[1]);
	    gob.setattr(new ResDrawable(gob, res));
	    plob.add(gob);
	    glob.oc.ladd(plob);
	    if(args.length > 3) {
		plrad = (Integer)args[3];
		radiuses.put(res.name, plrad);
		if(res.name.equals("gfx/terobjs/bhive"))
		    radiuses.put("gfx/terobjs/bhived", plrad);
	    }
	    this.plob = plob;
	} else if(msg == "unplace") {
	    if(plob != null)
		glob.oc.lrem(plob);
	    plob = null;
	    plrad = 0;
	} else if(msg == "polowner") {
	    String o = ((String)args[0]).intern();
	    if(o != polowner) {
		if(o.length() == 0) {
		    if(this.polowner != null)
			this.polownert = polownertf.render("Leaving " + this.polowner);
		    this.polowner = null;
		} else {
		    this.polowner = o;
		    this.polownert = polownertf.render("Entering " + o);
		}
		this.polchtm = System.currentTimeMillis();
	    }
	} else {
	    super.uimsg(msg, args);
	}
    }
	
    public void enol(int... overlays) {
	for(int ol : overlays)
	    visol[ol]++;
    }
	
    public void disol(int... overlays) {
	for(int ol : overlays)
	    visol[ol]--;
    }
	
    private int gettilen(Coord tc) throws Loading {
	int r = map.gettilen(tc);
	if(r == -1)
	    throw(new Loading());
	return(r);
    }
	
    private Tile getground(Coord tc) throws Loading {
	Tile r = map.getground(tc);
	if(r == null)
	    throw(new Loading());
	return(r);
    }
	
    private Tile[] gettrans(Coord tc) throws Loading {
	Tile[] r = map.gettrans(tc);
	if(r == null)
	    throw(new Loading());
	return(r);
    }

    private int getol(Coord tc)  throws Loading {
	int ol = map.getol(tc);
	if(ol == -1)
	    throw(new Loading());
	return(ol);
    }
	
    private void drawtile(GOut g, Coord tc, Coord sc) {
	Tile t;
		
	try {
	    t = getground(tc);
	    //t = gettile(tc).ground.pick(0);
	    g.image(t.tex(), sc);
	    //g.setColor(FlowerMenu.pink);
	    //Utils.drawtext(g, Integer.toString(t.i), sc);
		if(!Config.edgedTiles){ // new
			for(Tile tt : gettrans(tc)) {
			g.image(tt.tex(), sc);
			}
		}
	} catch (Loading e) {}
    }
    
    private void drawol(GOut g, Coord tc, Coord sc) {
	int ol;
	int i;
	double w = 2;
		
	try {
	    ol = getol(tc);

	    if(ol == 0)
		return;
	    Coord c1 = sc;
	    Coord c2 = sc.add(m2s(new Coord(0, tilesz.y)));
	    Coord c3 = sc.add(m2s(new Coord(tilesz.x, tilesz.y)));
	    Coord c4 = sc.add(m2s(new Coord(tilesz.x, 0)));
	    for(i = 0; i < olc.length; i++) {
		if(olc[i] == null)
		    continue;
		if(((ol & (1 << i)) == 0) || (visol[i] < 1))
		    continue;
		Color fc = new Color(olc[i].getRed(), olc[i].getGreen(), olc[i].getBlue(), 32);
		g.chcolor(fc);
		g.frect(c1, c2, c3, c4);
		if(((ol & ~getol(tc.add(new Coord(-1, 0)))) & (1 << i)) != 0) {
		    g.chcolor(olc[i]);
		    g.line(c2, c1, w);
		}
		if(((ol & ~getol(tc.add(new Coord(0, -1)))) & (1 << i)) != 0) {
		    g.chcolor(olc[i]);
		    g.line(c1.add(1, 0), c4.add(1, 0), w);
		}
		if(((ol & ~getol(tc.add(new Coord(1, 0)))) & (1 << i)) != 0) {
		    g.chcolor(olc[i]);
		    g.line(c4.add(1, 0), c3.add(1, 0), w);
		}
		if(((ol & ~getol(tc.add(new Coord(0, 1)))) & (1 << i)) != 0) {
		    g.chcolor(olc[i]);
		    g.line(c3, c2, w);
		}
	    }
	    g.chcolor(Color.WHITE);
	} catch (Loading e) {}
    }
    
    private void drawradius(GOut g, Coord c, int radius) {
	g.fellipse(c, new Coord((int)(radius * 4 * Math.sqrt(0.5)), (int)(radius * 2 * Math.sqrt(0.5))));
    }
    
    private void drawplobeffect(GOut g) {
	if(plob == null)
	    return;
	Gob gob = null;
	for(Gob tg : plob)
	    gob = tg;
	if(gob.sc == null)
	    return;
	if(plrad > 0) {
	    String name = gob.resname();
	    g.chcolor(0, 255, 0, 32);
	    synchronized (glob.oc) {
		for (Gob tg : glob.oc)
		    if ((tg.sc != null) && (tg.resname() == name))
			drawradius(g, tg.sc, plrad);
	    }
	    g.chcolor();
	}
    }
    
    private void draweffectradius(GOut g) {
	String name;
	g.chcolor(0, 255, 0, 32);
	synchronized (glob.oc) {
	    for (Gob tg : glob.oc) {
		name = tg.resname();
		if (radiuses.containsKey(name) && (tg.sc != null)) {
		    drawradius(g, tg.sc, radiuses.get(name));
		}
	    }
	}
	g.chcolor();
    }
    
    private void drawobjradius(GOut g) {
	synchronized (glob.oc) {
	    for (Gob gob : glob.oc) {
		if(gob.sc == null){continue;}
		
		if (Config.showBeast && gob.isBeast()) {
		    HLInfo inf = Config.hlcfg.get(gob.beastname);
		    g.chcolor(inf.col.getRed(), inf.col.getGreen(), inf.col.getBlue(), 96);
		    if(inf.show) // new
				drawradius(g, gob.sc, 100);
		}
		
		if(gob.isHuman() && !ui.sess.glob.party.memb.keySet().contains(gob.id)){
		    g.chcolor(255, 0, 255, 96);
		    drawradius(g, gob.sc, 10);
			if (Config.autohearth) {
				autohearth(gob);
			}
		}
		
		if(gob.isHighlight() && Config.highlightItemList.contains(gob.resname())){
		    g.chcolor(255, 128, 64, 96);
		    drawradius(g, gob.sc, 10);
		}
	    }
	}
	g.chcolor();
    }
	
	private void autohearth(Gob gob) {
		if (System.currentTimeMillis() > lastah) {
			KinInfo kin = gob.getattr(KinInfo.class);
			if(kin == null){
				if (Config.hearthunknown) {
					hearth();
				}
			} else {
				if (kin.group == 2) {
					if (Config.hearthred)
						hearth();
				}
			}
		}
	}
	
	private void hearth() {
		String[] action = {"theTrav", "hearth"};
		UI.instance.mnu.wdgmsg("act", (Object[])action);
		lastah = System.currentTimeMillis() + 5000;
	}
    
    private void drawtracking(GOut g) {
	g.chcolor(255, 0, 255, 128);
	Coord oc = viewoffset(sz, mc);
	for(int i = 0; i<TrackingWnd.instances.size(); i++){
	    TrackingWnd wnd = TrackingWnd.instances.get(i);
	    if(wnd.pos == null){continue;}
	    Coord c = m2s(wnd.pos).add(oc);
	    g.fellipse(c, new Coord(100, 50), wnd.a1, wnd.a2);
	}
	g.chcolor();
    }
    
    private boolean follows(Gob g1, Gob g2) {
	Following flw;
	if((flw = g1.getattr(Following.class)) != null) {
	    if(flw.tgt() == g2)
		return(true);
	}
	if((flw = g2.getattr(Following.class)) != null) {
	    if(flw.tgt() == g1)
		return(true);
	}
	return(false);
    }

    private List<Sprite.Part> findobsc() {
	ArrayList<Sprite.Part> obsc = new ArrayList<Sprite.Part>();
	if(obscgob == null)
	    return(obsc);
	boolean adding = false;
	for(Sprite.Part p : clickable) {
	    Gob gob = (Gob)p.owner;
	    if(gob == null)
		continue;
	    if(gob == obscgob) {
		adding = true;
		continue;
	    }
	    if(follows(gob, obscgob))
		continue;
	    if(adding && obscpart.checkhit(gob.sc.add(obscgob.sc.inv())))
		obsc.add(p);
	}
	return(obsc);
    }
    
    private void drawols(GOut g, Coord sc) {
	synchronized(map.grids){
	    for(Coord gc : map.grids.keySet()){
		Grid grid = map.grids.get(gc);
		for(Overlay lol : grid.ols ){
		    int id = getolid(lol.mask);
		    if(visol[id] < 1){
			continue;
		    }
		    Coord c0 = gc.mul(cmaps);
		    drawol2(g, id, c0.add(lol.c1), c0.add(lol.c2), sc);
		}
	    }
	}
	for(Overlay lol : map.ols ){
	    int id = getolid(lol.mask);
	    if(visol[id] < 1){
		continue;
	    }
	    drawol2(g, id, lol.c1, lol.c2, sc);
	}
	g.chcolor();
    }
    
    private int getolid(int mask){
	for(int i=0; i<olc.length; i++){
	    if((mask & (1 << i)) != 0){
		return i;
	    }
	}
	return 0;
    }
    
    private void drawol2(GOut g, int id, Coord c0, Coord cx, Coord sc){
	cx = cx.add(1,1);
	Coord c1 = m2s(c0.mul(tilesz)).add(sc);
	Coord c2 = m2s(new Coord(c0.x, cx.y).mul(tilesz)).add(sc);
	Coord c3 = m2s(cx.mul(tilesz)).add(sc);
	Coord c4 = m2s(new Coord(cx.x, c0.y).mul(tilesz)).add(sc);
	
	Color fc = new Color(olc[id].getRed(), olc[id].getGreen(), olc[id].getBlue(), 32);
	g.chcolor(fc);
	g.frect(c1, c2, c3, c4);
	cx = cx.sub(1,1);
	drawline(g, new Coord(0,-1), c0.y, id, c0, cx, sc);
	drawline(g, new Coord(0,1), cx.y, id, c0, cx, sc);
	drawline(g, new Coord(1,0), cx.x, id, c0, cx, sc);
	drawline(g, new Coord(-1,0), c0.x, id, c0, cx, sc);
	g.chcolor();
    }
    
    private void drawline(GOut g, Coord d, int med, int id, Coord c0, Coord cx, Coord sc){
	
	Coord m = d.abs();
	Coord r = m.swap();
	Coord off = m.mul(med).add(d.add(m).div(2));
	int min = c0.mul(r).sum();
	int max = cx.mul(r).sum()+1;
	boolean t=false;
	int begin = min;
	int ol = 1<<id;
	g.chcolor(olc[id]);
	for(int i=min; i<=max; i++){
	    Coord c = r.mul(i).add(m.mul(med)).add(d);
	    int ol2;
	    try{ol2 = getol(c);}catch(Loading e){ol2 = ol;}
	    if(t){
		if(((ol2&ol)!=0)||i==max){
		    t = false;
		    Coord cb = m2s(tilesz.mul(r.mul(begin).add(off))).add(sc);
		    Coord ce = m2s(tilesz.mul(r.mul(i).add(off))).add(sc);
		    g.line(cb, ce, 2);
		}
	    } else {
		if((ol2&ol)==0){
		    t = true;
		    begin = i;
		}
	    }
	}
    }
    
    public void drawmap(GOut g) {
	int x, y, i;
	int stw, sth;
	Coord oc, tc, ctc, sc;
	
	if(Config.profile)
	    curf = prof.new Frame();
	stw = (tilesz.x * 4) - 2;
	sth = tilesz.y * 2;
	oc = viewoffset(sz, mc);
	tc = mc.div(tilesz);
	tc.x += -(sz.x / (2 * stw)) - (sz.y / (2 * sth)) - 2;
	tc.y += (sz.x / (2 * stw)) - (sz.y / (2 * sth));
	for(y = 0; y < (sz.y / sth) + 2; y++) {
	    for(x = 0; x < (sz.x / stw) + 3; x++) {
		for(i = 0; i < 2; i++) {
		    ctc = tc.add(new Coord(x + y, -x + y + i));
		    sc = m2s(ctc.mul(tilesz)).add(oc);
		    sc.x -= tilesz.x * 2;
		    drawtile(g, ctc, sc);
		    sc.x += tilesz.x * 2;
		    if(!Config.newclaim){drawol(g, ctc, sc);}
		}
	    }
	}
	if(Config.newclaim){drawols(g, oc);}
	if(Config.grid){
	    g.chcolor(new Color(40, 40, 40));
	    Coord c1, c2, d;
	    d = tc.mul(tilesz);
	    int hy = (sz.y / sth)*tilesz.y;
	    int hx = (sz.x / stw)*tilesz.x;
	    c1 = d.add(0, 0);
	    c2 = d.add(5*hx/2,0);
	    for(y = d.y - hy; y < d.y + hy; y = y + tilesz.y){
		c1.y = y;
		c2.y = c1.y;
		g.line(m2s(c1).add(oc), m2s(c2).add(oc), 1);
	    }
	    c1 = d.add(0, -hy);
	    c2 = d.add(0, hy);
	    
	    for(x = d.x; x < d.x + 5*hx/2; x = x + tilesz.x){
		c1.x = x;
		c2.x = c1.x;
		g.line(m2s(c1).add(oc), m2s(c2).add(oc), 1);
	    }
	    
		
		//add mapgrid lines
		
		g.chcolor();
	}
	if(curf != null)
	    curf.tick("map");

	if(Config.showRadius)
	    draweffectradius(g);
	else
	    drawplobeffect(g);
	
	if(Config.radar){drawobjradius(g);}
	drawtracking(g);
	
	if(curf != null)
	    curf.tick("plobeff");
	
	if(Config.combatCross)
		drawTargetCross(g);
	
	final List<Sprite.Part> sprites = new ArrayList<Sprite.Part>();
	ArrayList<Speaking> speaking = new ArrayList<Speaking>();
	ArrayList<KinInfo> kin = new ArrayList<KinInfo>();
	class GobMapper implements Sprite.Drawer {
	    Gob cur = null;
	    Sprite.Part.Effect fx = null;
	    int szo = 0;
	    
	    public void chcur(Gob cur) {
		this.cur = cur;
		GobHealth hlt = cur.getattr(GobHealth.class);
		fx = null;
		if(hlt != null)
		    fx = hlt.getfx();
		if(cur.highlight != null){
		    long t = System.currentTimeMillis() - cur.highlight.time;
		    if(t > 5000){
			cur.highlight = null;
		    }
		}
		if(cur.highlight != null){
		    fx = cur.highlight;
		}
		Following flw = cur.getattr(Following.class);
		szo = 0;
		if(flw != null)
		    szo = flw.szo;
	    }

	    public void addpart(Sprite.Part p) {
		p.effect = fx;
		if((p.ul.x >= sz.x) ||
		   (p.ul.y >= sz.y) ||
		   (p.lr.x < 0) ||
		   (p.lr.y < 0))
		    return;
		sprites.add(p);
		p.owner = cur;
		p.szo = szo;
	    }
	}
	
	if(Config.showHidden && Config.hide) {
		g.chcolor(255, 0, 0, 128);
		synchronized(glob.oc) {
		    for(Gob gob : glob.oc) {
			Resource res = gob.getres();
			if(!gob.hide || ((res != null)&&(res.skiphighlight)))
			    continue;
			Resource.Neg neg = gob.getneg();
			if(neg == null)
			    continue;
			if((neg.bs.x > 0) && (neg.bs.y > 0)) {
			    Coord c1 = gob.getc().add(neg.bc);
			    Coord c2 = c1.add(neg.bs);
			    g.frect(m2s(c1).add(oc),
				    m2s(new Coord(c2.x, c1.y)).add(oc),
				    m2s(c2).add(oc),
				    m2s(new Coord(c1.x, c2.y)).add(oc));
			}
		    }
		}
		g.chcolor();
	    }
	
	GobMapper drawer = new GobMapper();
	synchronized(glob.oc) {
	    for(Gob gob : glob.oc) {
		drawer.chcur(gob);
		Coord dc = m2s(gob.getc()).add(oc);
		gob.sc = dc;
		gob.drawsetup(drawer, dc, sz);
		Speaking s = gob.getattr(Speaking.class);
		if(s != null)
		    speaking.add(s);
		KinInfo k = gob.getattr(KinInfo.class);
		if(k != null)
		    kin.add(k);
	    }
	    if(curf != null)
		curf.tick("setup");
	    Collections.sort(sprites, Sprite.partidcmp);
	    {
		Sprite.Part[] clickable = new Sprite.Part[sprites.size()];
		for(int o = 0, u = clickable.length - 1; o < clickable.length; o++, u--)
		    clickable[u] = sprites.get(o);
		this.clickable = clickable;
	    }
	    if(curf != null)
		curf.tick("sort");
	    onmouse = null;
	    if(pmousepos != null)
		onmouse = gobatpos(pmousepos);
	    obscured = findobsc();
	    if(curf != null)
		curf.tick("obsc");
	    for(Sprite.Part part : sprites) {
			if(part.effect != null){
				part.draw(part.effect.apply(g));
			}else{
				part.draw(g);
			}
			
			if(Config.objectHealth)
				drawObjectHealth(g, (Gob)part.owner);
	    }
	    for(Sprite.Part part : obscured) {
			GOut g2 = new GOut(g);
			GobHealth hlt;
			if((part.owner != null) && (part.owner instanceof Gob) && ((hlt = ((Gob)part.owner).getattr(GobHealth.class)) != null)){
				g2.chcolor(255, (int)(hlt.asfloat() * 255), 0, 255);
			}else{
				g2.chcolor(255, 255, 0, 255);
				part.drawol(g2);
			}
	    }
		
		if(Config.combatHalo)
			drawRedCombatHalo(g);
	    
	    if(curf != null)
		curf.tick("draw");
	    //Illumination
	    g.gl.glPopMatrix();
	    GOut gilm = g.reclip(Coord.z, hsz);
	    gilm.image(mask, Coord.z);
	    g.gl.glPushMatrix();
	    g.scale(getScale());
	    //*****************
	    long now = System.currentTimeMillis();
	    RootWidget.names_ready = (RootWidget.screenshot && Config.sshot_nonames);
	    if(!RootWidget.names_ready){
		for(KinInfo k : kin) {
		    Tex t = k.rendered();
		    Coord gc = k.gob.sc;
		    String name = k.gob.resname();
		    boolean isother = name.contains("hearth") 
		    	|| name.contains("skeleton");
		    if(gc.isect(Coord.z, sz)) {
			if(k.seen == 0)
			    k.seen = now;
			int tm = (int)(now - k.seen);
			Color show = null;
			boolean auto = (k.type & 1) == 0;
			if((isother && Config.showOtherNames)||(!isother && Config.showNames)||(k.gob == onmouse)) {
			    show = Color.WHITE;
			} else if(auto && (tm < 7500)) {
			    show = Utils.clipcol(255, 255, 255, 255 - ((255 * tm) / 7500));
			}
			if(show != null) {
			    g.chcolor(show);
			    g.image(t, gc.add(-t.sz().x / 2, -40 - t.sz().y));
			    g.chcolor();
			}
		    } else {
			k.seen = 0;
		    }
		}
	    }
	    if(!Config.muteChat){
		for(Speaking s : speaking) {
		    s.draw(g, s.gob.sc.add(s.off));
		}
	    }
	    if(curf != null) {
		curf.tick("aux");
		curf.fin();
		curf = null;
	    }
	    //System.out.println(curf);
	}
    }
	
    public void drawarrows(GOut g) {
	Coord oc = viewoffset(sz, mc);
	Coord hsz = sz.div(2);
	double ca = -Coord.z.angle(hsz);
	for(Party.Member m : glob.party.memb.values()) {
	    //Gob gob = glob.oc.getgob(id);
	    Coord mc = m.getc();
	    if(mc == null)
		continue;
	    Coord sc = m2s(mc).add(oc);
	    if(!sc.isect(Coord.z, sz)) {
		double a = -hsz.angle(sc);
		Coord ac;
		if((a > ca) && (a < -ca)) {
		    ac = new Coord(sz.x, hsz.y - (int)(Math.tan(a) * hsz.x));
		} else if((a > -ca) && (a < Math.PI + ca)) {
		    ac = new Coord(hsz.x - (int)(Math.tan(a - Math.PI / 2) * hsz.y), 0);
		} else if((a > -Math.PI - ca) && (a < ca)) {
		    ac = new Coord(hsz.x + (int)(Math.tan(a + Math.PI / 2) * hsz.y), sz.y);
		} else {
		    ac = new Coord(0, hsz.y + (int)(Math.tan(a) * hsz.x));
		}
		g.chcolor(m.col);
		Coord bc = ac.add(Coord.sc(a, -10));
		g.line(bc, bc.add(Coord.sc(a, -40)), 2);
		g.line(bc, bc.add(Coord.sc(a + Math.PI / 4, -10)), 2);
		g.line(bc, bc.add(Coord.sc(a - Math.PI / 4, -10)), 2);
		g.chcolor(Color.WHITE);
	    }
	}
    }
	
    private void checkplmove() {
	Gob pl;
	long now = System.currentTimeMillis();
	if((playergob != -1) && ((pl = glob.oc.getgob(playergob)) != null) && (pl.sc != null)) {
	    Coord plp = pl.getc();
	    if((plfpos == null) || !plfpos.equals(plp)) {
		lastmove = now;
		plfpos = plp;
		if((obscpart != null) && !obscpart.checkhit(pl.sc.add(obscgob.sc.inv()))) {
		    obscpart = null;
		    obscgob = null;
		}
	    } else if(now - lastmove > 500) {
		for(Sprite.Part p : clickable) {
		    Gob gob = (Gob)p.owner;
		    if((gob == null) || (gob.sc == null))
			continue;
		    if(gob == pl)
			break;
		    if(p.checkhit(pl.sc.add(gob.sc.inv()))) {
			obscpart = p;
			obscgob = gob;
			break;
		    }
		}
	    }
	}
    }

    private void checkmappos() {
	if(cam == null)
	    return;
	Coord sz = this.sz;
	SlenHud slen = ui.slen;
	if(slen != null)
	    sz = sz.add(0, -slen.foldheight());
	Gob player = glob.oc.getgob(playergob);
	if(player != null)
	    cam.setpos(this, player, sz);
    }

    public void draw(GOut og) {
	if(moveto != null){
	    wdgmsg("click", moveto, moveto, 1, 0);
	    moveto = null;
	}
	hsz = MainFrame.getInnerSize();
	sz = hsz.mul(1/getScale());
	GOut g = og.reclip(Coord.z, sz);
	g.gl.glPushMatrix();
	g.scale(getScale());
	checkmappos();
	Coord requl = mc.add(-500, -500).div(tilesz).div(cmaps);
	Coord reqbr = mc.add(500, 500).div(tilesz).div(cmaps);
	Coord cgc = new Coord(0, 0);
	for(cgc.y = requl.y; cgc.y <= reqbr.y; cgc.y++) {
	    for(cgc.x = requl.x; cgc.x <= reqbr.x; cgc.x++) {
		if(map.grids.get(cgc) == null)
		    map.request(new Coord(cgc));
	    }
	}
	long now = System.currentTimeMillis();
	if((olftimer != 0) && (olftimer < now))
	    unflashol();
	map.sendreqs();
	checkplmove();
//	try {
	    if(((mask.amb = glob.amblight) == null) || Config.nightvision)
		mask.amb = new Color(0, 0, 0, 0);
	    drawmap(g);
	    
	    //movement highlight
	    if(Config.showgobpath||Config.showothergobpath){
		drawGobPath(g);
	    }
	    if(Config.showpath){
		drawPlayerPath(g);
	    }
	    //###############
	    
	    drawarrows(g);
	    g.chcolor(Color.WHITE);
	    if(Config.dbtext)
		g.atext(mc.toString(), new Coord(10, 560), 0, 1);
//	} catch(Loading l) {
//	    String text = "Loading...";
//	    g.chcolor(Color.BLACK);
//	    g.frect(Coord.z, sz);
//	    g.chcolor(Color.WHITE);
//	    g.atext(text, sz, 0.5, 0.5);
//	}
	long poldt = now - polchtm;
	if((polownert != null) && (poldt < 6000)) {
	    int a;
	    if(poldt < 1000)
		a = (int)((255 * poldt) / 1000);
	    else if(poldt < 4000)
		a = 255;
	    else
		a = (int)((255 * (2000 - (poldt - 4000))) / 2000);
	    g.chcolor(255, 255, 255, a);
	    g.aimage(polownert.tex(), sz.div(2), 0.5, 0.5);
	    g.chcolor();
	}
	g.gl.glPopMatrix();
	super.draw(og);
    }

    private void drawPlayerPath(GOut g) {
	Coord oc = viewoffset(sz, mc);
	Coord pc, cc;
	Moving m;
	LinMove lm;
	Gob player = glob.oc.getgob(playergob);
	if(player != null){
	    m = player.getattr(Moving.class);
	    g.chcolor(Color.GREEN);
	    if((m != null) && (m instanceof LinMove)){
		lm = (LinMove)m;
		pc = m2s(lm.t).add(oc);
		
		if(player.sc != null) g.line(player.sc, pc, 2); // new
		
		for(Coord c:glob.oc.movequeue){
		    cc = m2s(c).add(oc);
		    g.line(pc, cc, 2);
		    pc = cc;
		}
	    }
	    g.chcolor();
	}
    }

    private void drawGobPath(GOut g) {
	Moving m;
	LinMove lm;
	Coord oc = viewoffset(sz, mc);
	g.chcolor(Color.ORANGE);
	synchronized (glob.oc) {
	    for (Gob gob : glob.oc){
		if ((Config.showgobpath && gob.isHuman()) || (Config.showothergobpath && !gob.isHuman())) {
			if(gob.sc == null){continue;}
			m = gob.getattr(Moving.class);
			if((m!=null)&&(m instanceof LinMove)){
			    lm = (LinMove) m;
				
				if(gob.isHuman() && Config.kinLines){
					KinInfo kin = gob.getattr(KinInfo.class);
					if(kin == null) g.chcolor(Color.WHITE);
					else g.chcolor(BuddyWnd.gc[kin.group]);
				}
				
			    g.line(gob.sc, m2s(lm.t).add(oc), 2);
			}
		}
	    }
	}
	g.chcolor();
    }
	
    public boolean drop(Coord cc, Coord ul) {
	wdgmsg("drop", ui.modflags());
	return(true);
    }
	
    public boolean iteminteract(Coord cc, Coord ul) {
	Coord cc0 = cc;
	cc = new Coord((int) (cc.x/getScale()), (int)(cc.y/getScale()));
	Gob hit = gobatpos(cc);
	Coord mc = s2m(cc.add(viewoffset(sz, this.mc).inv()));
	if(hit == null)
	    wdgmsg("itemact", cc0, mc, ui.modflags());
	else
	    wdgmsg("itemact", cc0, mc, ui.modflags(), hit.id, hit.getc());
	return(true);
    }
    
    private Map<String, Console.Command> cmdmap = new TreeMap<String, Console.Command>();
    {
	cmdmap.put("cam", new Console.Command() {
		public void run(Console cons, String[] args) {
		    if(args.length >= 2) {
			Class<? extends Camera> ct = camtypes.get(args[1]);
			String[] cargs = new String[args.length - 2];
			System.arraycopy(args, 2, cargs, 0, cargs.length);
			if(ct != null) {
			    try {
				MapView.this.cam = makecam(ct, cargs);
				Utils.setpref("defcam", args[1]);
				Utils.setprefb("camargs", Utils.serialize(cargs));
			    } catch(ClassNotFoundException e) {
				throw(new RuntimeException("no such camera: " + args[1]));
			    }
			} else {
			    throw(new RuntimeException("no such camera: " + args[1]));
			}
		    }
		}
	    });
	cmdmap.put("plol", new Console.Command() {
		public void run(Console cons, String[] args) {
		    Indir<Resource> res = Resource.load(args[1]).indir();
		    Message sdt;
		    if(args.length > 2)
			sdt = new Message(0, Utils.hex2byte(args[2]));
		    else
			sdt = new Message(0);
		    Gob pl;
		    if((playergob != -1) && ((pl = glob.oc.getgob(playergob)) != null))
			pl.ols.add(new Gob.Overlay(-1, res, sdt));
		}
	    });
    }
    public Map<String, Console.Command> findcmds() {
	return(cmdmap);
    }
	
	public void drawTargetCross(GOut g){
		if(ui.fight == null || ui.fight.current == null || ui.fight.lsrel.size() <= 0)
			return;
		
		Coord oc = viewoffset(sz, mc);
		
		Gob gob = glob.oc.getgob(ui.fight.current.gobid );
		if(gob == null ) return;
		
		try{
			g.chcolor(255, 0, 0, 255);
			g.line(m2s(gob.getc().add(5,5) ).add(oc), m2s(gob.getc().add(-5,-5)).add(oc),3);
			g.line(m2s(gob.getc().add(-5,5) ).add(oc), m2s(gob.getc().add(5,-5)).add(oc),3);
			g.chcolor();
			}
		catch(Exception e){
			g.chcolor();
		};
	}
	
	private void drawRedCombatHalo(GOut g) {
	ArrayList<Sprite.Part> obsc = new ArrayList<Sprite.Part>();
	if(ui.fight == null || ui.fight.current == null || ui.fight.lsrel.size() <= 0)
		return;

	for(Sprite.Part p : clickable) {
	    Gob gob = (Gob)p.owner;
	    if(gob == null)
		continue;
		
		if(ui.fight.current.gobid == gob.id){
			g.chcolor(255, 0, 0, 255);
			p.drawol(g);
			g.chcolor();
			break;
		}
	}
	return;
    }
	
	void drawObjectHealth(GOut g, Gob gob){
		if(gob == null || gob.sc == null) return;
			GobHealth hlt = gob.getattr(GobHealth.class);
		int health;
		if(hlt != null && (health = (int)(hlt.asfloat() * 100)) < 100 ){
			g.atext(health + "%", gob.sc.add(-8, -15), 0, 1);
		}
	}
}
