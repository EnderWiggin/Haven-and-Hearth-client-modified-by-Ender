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
import haven.Resource.Tile;
import java.awt.Color;
import java.util.*;

public class MapView extends Widget implements DTarget {
    public Coord mc, mousepos, pmousepos;
    Camera cam;
    Sprite.Part[] clickable = {};
    List<Sprite.Part> obscured = Collections.emptyList();
    private int[] visol = new int[31];
    private long olftimer = 0;
    private int olflash = 0;
    static Color[] olc = new Color[31];
    public boolean authdraw = Utils.getpref("authdraw", "on").equals("on");
    Grabber grab = null;
    ILM mask;
    final MCache map;
    final Glob glob;
    Collection<Gob> plob = null;
    boolean plontile;
    int plrad = 0;
    int playergob = -1;
    public Profile prof = new Profile(300);
    private Profile.Frame curf;
    Coord plfpos = null;
    long lastmove = 0;
    Sprite.Part obscpart = null;
    Gob obscgob = null;
    
    public static final Comparator<Sprite.Part> clickcmp = new Comparator<Sprite.Part>() {
	public int compare(Sprite.Part a, Sprite.Part b) {
	    return(-Sprite.partidcmp.compare(a, b));
	}
    };
    
    static {
	Widget.addtype("mapview", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    Coord sz = (Coord)args[0];
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
    }
    
    private static abstract class DragCam extends Camera {
	Coord o, mo;
	boolean dragging = false;
	
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

    static class BorderCam extends DragCam {
	public final Coord border = new Coord(250, 150);

	public void setpos(MapView mv, Gob player, Coord sz) {
	    borderize(mv, player, sz, border);
	}
    }
    
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
    }
    
    static class FixedCam extends DragCam {
	public final Coord border = new Coord(250, 150);
	private Coord off = Coord.z;
	private boolean setoff = false;
	
	public void setpos(MapView mv, Gob player, Coord sz) {
	    if(setoff) {
		borderize(mv, player, sz, border);
		off = mv.mc.add(player.getc().inv());
		setoff = false;
	    }
	    mv.mc = player.getc().add(off);
	}
	
	public void moved(MapView mv) {
	    setoff = true;
	}
    }
    
    private class Loading extends RuntimeException {}
    
    public MapView(Coord c, Coord sz, Widget parent, Coord mc, int playergob) {
	super(c, sz, parent);
	this.mc = mc;
	this.playergob = playergob;
	cam = new BorderCam();
	setcanfocus(true);
	glob = ui.sess.glob;
	map = glob.map;
	mask = new ILM(sz, glob.oc);
    }
	
    public static Coord m2s(Coord c) {
	return(new Coord((c.x * 2) - (c.y * 2), c.x + c.y));
    }
	
    public static Coord s2m(Coord c) {
	return(new Coord((c.x / 4) + (c.y / 2), (c.y / 2) - (c.x / 4)));
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

    public boolean mousedown(Coord c, int button) {
	setfocus(this);
	Gob hit = gobatpos(c);
	Coord mc = s2m(c.add(viewoffset(sz, this.mc).inv()));
	if(grab != null) {
	    grab.mmousedown(mc, button);
	} else if((cam != null) && cam.click(this, c, mc, button)) {
	    /* Nothing */
	} else if(plob != null) {
	    Gob gob = null;
	    for(Gob g : plob)
		gob = g;
	    wdgmsg("place", gob.rc, button, ui.modflags());
	} else {
	    if(hit == null)
		wdgmsg("click", c, mc, button, ui.modflags());
	    else
		wdgmsg("click", c, mc, button, ui.modflags(), hit.id, hit.getc());
	}
	return(true);
    }
	
    public boolean mouseup(Coord c, int button) {
	Coord mc = s2m(c.add(viewoffset(sz, this.mc).inv()));
	if(grab != null) {
	    grab.mmouseup(mc, button);
	    return(true);
	} else if((cam != null) && cam.release(this, c, mc, button)) {
	    return(true);
	} else {
	    return(true);
	}
    }
	
    public void mousemove(Coord c) {
	this.pmousepos = c;
	Coord mc = s2m(c.add(viewoffset(sz, this.mc).inv()));
	this.mousepos = mc;
	Collection<Gob> plob = this.plob;
	if(cam != null)
	    cam.move(this, c, mc);
	if(grab != null) {
	    grab.mmousemove(mc);
	} else if(plob != null) {
	    Gob gob = null;
	    for(Gob g : plob)
		gob = g;
	    boolean plontile = this.plontile ^ ui.modshift;
	    gob.move(plontile?tilify(mc):mc);
	}
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
	    if(cam != null)
		cam.moved(this);
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
	    if(args.length > 3)
		plrad = (Integer)args[3];
	    this.plob = plob;
	} else if(msg == "unplace") {
	    if(plob != null)
		glob.oc.lrem(plob);
	    plob = null;
	    plrad = 0;
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
	
    private int gettilen(Coord tc) {
	int r = map.gettilen(tc);
	if(r == -1)
	    throw(new Loading());
	return(r);
    }
	
    private Tile getground(Coord tc) {
	Tile r = map.getground(tc);
	if(r == null)
	    throw(new Loading());
	return(r);
    }
	
    private Tile[] gettrans(Coord tc) {
	Tile[] r = map.gettrans(tc);
	if(r == null)
	    throw(new Loading());
	return(r);
    }

    private int getol(Coord tc) {
	int ol = map.getol(tc);
	if(ol == -1)
	    throw(new Loading());
	return(ol);
    }
	
    private void drawtile(GOut g, Coord tc, Coord sc) {
	Tile t;
		
	t = getground(tc);
	//t = gettile(tc).ground.pick(0);
	g.image(t.tex(), sc);
	//g.setColor(FlowerMenu.pink);
	//Utils.drawtext(g, Integer.toString(t.i), sc);
	for(Tile tt : gettrans(tc)) {
	    g.image(tt.tex(), sc);
	}
    }
	
    private void drawol(GOut g, Coord tc, Coord sc) {
	int ol;
	int i;
	double w = 2;
		
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
	    g.chcolor(0, 255, 0, 32);
	    g.fellipse(gob.sc, new Coord((int)(plrad * 4 * Math.sqrt(0.5)), (int)(plrad * 2 * Math.sqrt(0.5))));
	    g.chcolor();
	}
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
		}
	    }
	}
	for(y = 0; y < (sz.y / sth) + 2; y++) {
	    for(x = 0; x < (sz.x / stw) + 3; x++) {
		for(i = 0; i < 2; i++) {
		    ctc = tc.add(new Coord(x + y, -x + y + i));
		    sc = m2s(ctc.mul(tilesz)).add(oc);
		    drawol(g, ctc, sc);
		}
	    }
	}
	if(curf != null)
	    curf.tick("map");

	drawplobeffect(g);
	if(curf != null)
	    curf.tick("plobeff");
		
	final List<Sprite.Part> sprites = new ArrayList<Sprite.Part>();
	ArrayList<Speaking> speaking = new ArrayList<Speaking>();
	ArrayList<KinInfo> kin = new ArrayList<KinInfo>();
	class GobMapper implements Sprite.Drawer {
	    Gob cur = null;
	    Sprite.Part.Effect fx = null;
	    
	    public void chcur(Gob cur) {
		this.cur = cur;
		GobHealth hlt = cur.getattr(GobHealth.class);
		fx = null;
		if(hlt != null)
		    fx = hlt.getfx();
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
	    }
	}
	GobMapper drawer = new GobMapper();
	synchronized(glob.oc) {
	    for(Gob gob : glob.oc) {
		drawer.chcur(gob);
		Coord dc = m2s(gob.getc()).add(oc);
		gob.sc = dc;
		gob.drawsetup(drawer, dc, sz);
		if(authdraw) {
		    Authority a = gob.getattr(Authority.class);
		    if(a != null) {
			Sprite.Part p = a.mkpart();
			p.setup(dc, Coord.z);
			sprites.add(p);
		    }
		}
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
	    Gob onmouse = null;
	    if(pmousepos != null)
		onmouse = gobatpos(pmousepos);
	    obscured = findobsc();
	    if(curf != null)
		curf.tick("obsc");
	    for(Sprite.Part part : sprites) {
		if(part.effect != null)
		    part.draw(part.effect.apply(g));
		else
		    part.draw(g);
	    }
	    for(Sprite.Part part : obscured)
		part.drawol(g);
	    
	    if(Config.bounddb && ui.modshift) {
		g.chcolor(255, 0, 0, 128);
		synchronized(glob.oc) {
		    for(Gob gob : glob.oc) {
			Drawable d = gob.getattr(Drawable.class);
			Resource.Neg neg;
			if(d instanceof ResDrawable) {
			    ResDrawable rd = (ResDrawable)d;
			    if(rd.spr == null)
				continue;
			    if(rd.spr.res == null)
				continue;
			    neg = rd.spr.res.layer(Resource.negc);
			} else if(d instanceof Layered) {
			    Layered lay = (Layered)d;
			    if(lay.base.get() == null)
				continue;
			    neg = lay.base.get().layer(Resource.negc);
			} else {
			    continue;
			}
			if((neg.bs.x > 0) && (neg.bs.y > 0)) {
			    Coord c1 = gob.getc().add(neg.bc);
			    Coord c2 = gob.getc().add(neg.bc).add(neg.bs);
			    g.frect(m2s(c1).add(oc),
				    m2s(new Coord(c2.x, c1.y)).add(oc),
				    m2s(c2).add(oc),
				    m2s(new Coord(c1.x, c2.y)).add(oc));
			}
		    }
		}
		g.chcolor();
	    }
	    
	    if(curf != null)
		curf.tick("draw");
	    g.image(mask, Coord.z);
	    long now = System.currentTimeMillis();
	    for(KinInfo k : kin) {
		Tex t = k.rendered();
		Coord gc = k.gob.sc;
		if(gc.isect(Coord.z, sz)) {
		    if(k.seen == 0)
			k.seen = now;
		    int tm = (int)(now - k.seen);
		    if(k.gob == onmouse) {
			g.image(t, gc.add(-t.sz().x / 2, -40 - t.sz().y));
		    } else if(tm < 7500) {
			g.chcolor(255, 255, 255, 255 - ((255 * tm) / 7500));
			g.image(t, gc.add(-t.sz().x / 2, -40 - t.sz().y));
			g.chcolor();
		    }
		} else {
		    k.seen = 0;
		}
	    }
	    for(Speaking s : speaking) {
		s.draw(g, s.gob.sc.add(s.off));
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
	    if(m.getc() == null)
		continue;
	    Coord sc = m2s(m.getc()).add(oc);
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
	if((playergob >= 0) && ((pl = glob.oc.getgob(playergob)) != null) && (pl.sc != null)) {
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
	SlenHud slen = ui.root.findchild(SlenHud.class);
	if(slen != null)
	    sz = sz.add(0, -slen.foldheight());
	Gob player = glob.oc.getgob(playergob);
	if(player != null)
	    cam.setpos(this, player, sz);
    }

    public void draw(GOut g) {
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
	if((olftimer != 0) && (olftimer < System.currentTimeMillis()))
	    unflashol();
	map.sendreqs();
	checkplmove();
	try {
	    if((mask.amb = glob.amblight) == null)
		mask.amb = new Color(0, 0, 0, 0);
	    drawmap(g);
	    drawarrows(g);
	    g.chcolor(Color.WHITE);
	    if(Config.dbtext)
		g.atext(mc.toString(), new Coord(10, 560), 0, 1);
	} catch(Loading l) {
	    String text = "Loading...";
	    g.chcolor(Color.BLACK);
	    g.frect(Coord.z, sz);
	    g.chcolor(Color.WHITE);
	    g.atext(text, sz.div(2), 0.5, 0.5);
	}
	super.draw(g);
    }
	
    public boolean drop(Coord cc, Coord ul) {
	wdgmsg("drop", ui.modflags());
	return(true);
    }
	
    public boolean iteminteract(Coord cc, Coord ul) {
	Gob hit = gobatpos(cc);
	Coord mc = s2m(cc.add(viewoffset(sz, this.mc).inv()));
	if(hit == null)
	    wdgmsg("itemact", cc, mc, ui.modflags());
	else
	    wdgmsg("itemact", cc, mc, ui.modflags(), hit.id, hit.getc());
	return(true);
    }
}
