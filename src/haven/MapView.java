package haven;

import static haven.MCache.cmaps;
import static haven.MCache.tilesz;
import haven.Resource.Tile;
import haven.Resource.Tileset;
import java.awt.Color;
import java.util.*;

public class MapView extends Widget implements DTarget {
	Coord mc;
	List<Drawable> clickable = new ArrayList<Drawable>();
	int visol = 0xC;
	static Color[] olc = new Color[31];
	Grabber grab = null;
	ILM mask;
	final MCache map;
	final Glob glob;
	Collection<Gob> plob = null;
	boolean plontile;
	public Profile prof = new Profile(300);
	private Profile.Frame curf;
	
	static {
		Widget.addtype("mapview", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new MapView(c, (Coord)args[0], parent, (Coord)args[1]));
			}
		});
		olc[0] = new Color(255, 0, 128);
		olc[1] = new Color(0, 0, 255);
		olc[2] = new Color(255, 0, 0);
		olc[3] = new Color(128, 0, 255);
		olc[16] = new Color(0, 255, 0);
	}
	
	public interface Grabber {
		void mmousedown(Coord mc, int button);
		void mmouseup(Coord mc, int button);
		void mmousemove(Coord mc);
	}
	
	@SuppressWarnings("serial")
	private class Loading extends RuntimeException {}
	
	public MapView(Coord c, Coord sz, Widget parent, Coord mc) {
		super(c, sz, parent);
		mask = new ILM(sz);
		this.mc = mc;
		setcanfocus(true);
		glob = ui.sess.glob;
		map = glob.map;
	}
	
	public static Coord m2s(Coord c) {
		return(new Coord((c.x * 2) - (c.y * 2), c.x + c.y));
	}
	
	public static Coord s2m(Coord c) {
		return(new Coord((c.x / 4) + (c.y / 2), (c.y / 2) - (c.x / 4)));
	}
	
	static Coord viewoffset(Coord sz, Coord vc) {
		return(m2s(vc).inv().add(new Coord(sz.x / 2, sz.y / 2)));
	}
	
	public void grab(Grabber grab) {
		this.grab = grab;
	}
	
	public void release(Grabber grab) {
		if(this.grab == grab)
			this.grab = null;
	}
	
	public boolean mousedown(Coord c, int button) {
		setfocus(this);
		Drawable hit = null;
		for(Drawable d : clickable) {
			Gob g = d.gob;
			Coord ulc = g.sc.add(d.getoffset().inv());
			if(c.isect(ulc, d.getsize())) {
				if(d.checkhit(c.add(ulc.inv()))) {
					hit = d;
					break;
				}
			}
		}
		Coord mc = s2m(c.add(viewoffset(sz, this.mc).inv()));
		if(grab != null) {
			grab.mmousedown(mc, button);
		} else if(plob != null) {
			Gob gob = null;
			for(Gob g : plob)
				gob = g;
			wdgmsg("place", gob.rc, button);
		} else {
			if(hit == null)
				wdgmsg("click", c, mc, button);
			else
				wdgmsg("click", c, mc, button, hit.gob.id, hit.gob.getc());
		}
		return(true);
	}
	
	public boolean mouseup(Coord c, int button) {
		Coord mc = s2m(c.add(viewoffset(sz, this.mc).inv()));
		if(grab != null) {
			grab.mmouseup(mc, button);
			return(true);
		} else {
			return(false);
		}
	}
	
	public void mousemove(Coord c) {
		Coord mc = s2m(c.add(viewoffset(sz, this.mc).inv()));
		Collection<Gob> plob = this.plob;
		if(grab != null) {
			grab.mmousemove(mc);
		} else if(plob != null) {
			synchronized(plob) {
				Gob gob = null;
				for(Gob g : plob)
					gob = g;
				boolean plontile = this.plontile ^ ui.modshift;
				gob.move(plontile?tilify(mc):mc);
			}
		}
	}
	
	public void move(Coord mc, boolean trimall) {
		this.mc = mc;
		Coord cc = mc.div(cmaps.mul(tilesz));
		if(trimall)
			map.trimall();
		else
			map.trim(cc);
	}
	
	private static Coord tilify(Coord c) {
		c = c.div(tilesz);
		c = c.mul(tilesz);
		c = c.add(tilesz.div(2));
		return(c);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "move") {
			move((Coord)args[0], (Integer)args[1] != 0);
		} else if(msg == "place") {
			if(plob != null)
				glob.oc.lrem(plob);
			plob = new LinkedList<Gob>();
			synchronized(plob) {
				plontile = (Integer)args[2] != 0;
				Gob gob = new Gob(glob, plontile?tilify(mc):mc);
				Resource res = Resource.load((String)args[0], (Integer)args[1]);
				gob.setattr(new ResDrawable(gob, res));
				plob.add(gob);
				glob.oc.ladd(plob);
			}
		} else if(msg == "unplace") {
			if(plob != null)
				glob.oc.lrem(plob);
			plob = null;
		} else {
			super.uimsg(msg, args);
		}
	}
	
	public void enol(int mask) {
		visol |= mask;
	}
	
	public void disol(int mask) {
		visol &= ~mask;
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
		
		ol = getol(tc) & visol;
		if(ol == 0)
			return;
		Coord c1 = sc;
		Coord c2 = sc.add(m2s(new Coord(0, tilesz.y)));
		Coord c3 = sc.add(m2s(new Coord(tilesz.x, tilesz.y)));
		Coord c4 = sc.add(m2s(new Coord(tilesz.x, 0)));
		for(i = 0; i < olc.length; i++) {
			if(olc[i] == null)
				continue;
			if((ol & (1 << i)) == 0)
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
	
	public void drawmap(GOut g) {
		int x, y, i;
		int stw, sth;
		Coord oc, tc, ctc, sc;
		
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
		curf.tick("map");
		
		final ArrayList<Sprite.Part> sprites = new ArrayList<Sprite.Part>();
		ArrayList<Drawable> clickable = new ArrayList<Drawable>();
		ArrayList<Speaking> speaking = new ArrayList<Speaking>();
		ArrayList<Lumin> lumin = new ArrayList<Lumin>();
		Sprite.Drawer drawer = new Sprite.Drawer() {
				public void addpart(Sprite.Part p) {
					sprites.add(p);
				}
			};
		synchronized(glob.oc) {
			for(Gob gob : glob.oc) {
				Coord dc = m2s(gob.getc()).add(oc);
				gob.sc = dc;
				gob.drawsetup(drawer, dc, sz);
				Drawable d = gob.getattr(Drawable.class);
				if(d != null)
					clickable.add(d);
				Speaking s = gob.getattr(Speaking.class);
				if(s != null)
					speaking.add(s);
				Lumin l = gob.getattr(Lumin.class);
				if(l != null)
					lumin.add(l);
			}
			Collections.sort(clickable, new Comparator<Drawable>() {
					public int compare(Drawable a, Drawable b) {
						if(a.gob.clprio != b.gob.clprio)
							return(a.gob.clprio - b.gob.clprio);
						return(b.gob.sc.y - a.gob.sc.y);
					}
				});
			this.clickable = clickable;
			Collections.sort(sprites);
			curf.tick("sort");
			for(Sprite.Part part : sprites)
				part.draw(g);
			curf.tick("draw");
			mask.redraw(lumin);
			g.image(mask, Coord.z);
			for(Speaking s : speaking) {
				s.draw(g, s.gob.sc.add(s.off));
			}
			curf.tick("aux");
			curf.fin();
			//System.out.println(curf);
		}
	}
	
	private double clip(double d, double min, double max) {
		if(d < min)
			return(min);
		if(d > max)
			return(max);
		return(d);
	}
	
	private Color mkc(double r, double g, double b, double a) {
		return(new Color((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)));
	}
	
	private double anorm(double d) {
		return((d + 1) / 2);
	}
	
	private void fixlight() {
		Astronomy a = glob.ast;
		if(a == null) {
		    mask.amb = new Color(0, 0, 0, 0);
		    return;
		}
		double p2 = Math.PI * 2;
		double sa = -Math.cos(a.dt * p2);
		double la = anorm(-Math.cos(a.mp * p2));
		double hs = Math.pow(Math.sin(a.dt * p2), 2);
		double nl = clip(-sa * 2, 0, 1);
		hs = clip((hs - 0.5) * 2, 0, 1);
		double ml = 0.2 + la * 0.2;
		sa = anorm(clip(sa * 1.5, -1, 1));
		double ll = ml + ((1 - ml) * sa);
		mask.amb = mkc(hs * 0.4, hs * 0.2, nl * 0.25 * ll, 1 - ll);
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
	
	public void draw(GOut g) {
		Coord gc = mc.div(tilesz).div(cmaps);
		for(int y = -1; y <= 1; y++) {
			for(int x = -1; x <= 1; x++) {
				Coord cgc = gc.add(new Coord(x, y));
				if(map.grids.get(cgc) == null)
					map.request(cgc);
			}
		}
		map.sendreqs();
		try {
			fixlight();
			drawmap(g);
			drawarrows(g);
			g.chcolor(Color.WHITE);
			if((glob.fcap != 0) &&  (glob.stamcap != 0)) {
			    g.atext("Hunger meter: " + glob.glut / (glob.fcap / 100) + "%", new Coord(10, 575), 0, 1);
			    g.atext("Stamina meter: " + glob.stamina / (glob.stamcap / 100) + "%", new Coord(10, 590), 0, 1);
			}
			if(System.getProperty("haven.dbtext", "off").equals("on"))
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
		wdgmsg("drop");
		return(true);
	}
	
	public boolean iteminteract(Coord cc, Coord ul) {
		Drawable hit = null;
		for(Drawable d : clickable) {
			Gob g = d.gob;
			Coord ulc = g.sc.add(d.getoffset().inv());
			if(cc.isect(ulc, d.getsize())) {
				if(d.checkhit(cc.add(ulc.inv()))) {
					hit = d;
					break;
				}
			}
		}
		Coord mc = s2m(cc.add(viewoffset(sz, this.mc).inv()));
		if(hit == null)
			wdgmsg("itemact", cc, mc);
		else
			wdgmsg("itemact", cc, mc, hit.gob.id, hit.gob.getc());
		return(true);
	}
}
