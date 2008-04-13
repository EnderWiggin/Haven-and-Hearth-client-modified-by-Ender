package haven;

import static haven.MCache.cmaps;
import static haven.MCache.tilesz;
import haven.Resource.Tile;
import haven.Resource.Tileset;
import java.awt.Color;
import java.util.*;

public class MapView extends Widget implements DTarget {
	Coord mc;
	List<Drawable> clickable = null;
	int visol = 0;
	Color[] olc = {new Color(255, 0, 128), new Color(0, 255, 0)};
	Grabber grab = null;
	ILM mask;
	final MCache map;
	final Glob glob;
	
	static {
		Widget.addtype("mapview", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new MapView(c, (Coord)args[0], parent, (Coord)args[1]));
			}
		});
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
	
	static Coord m2s(Coord c) {
		return(new Coord((c.x * 2) - (c.y * 2), c.x + c.y));
	}
	
	static Coord s2m(Coord c) {
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
		if(grab != null) {
			grab.mmousemove(mc);
		}
	}
	
	public void move(Coord mc) {
		this.mc = mc;
		Coord cc = mc.div(cmaps.mul(tilesz));
		map.trim(cc);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "move") {
			move((Coord)args[0]);
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
	
	private Tileset gettile(Coord tc) {
		Tileset r = map.gettile(tc);
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
		
		t = gettile(tc).ground.pick(map.randoom(tc));
		g.image(t.tex(), sc);
		//g.setColor(FlowerMenu.pink);
		//Utils.drawtext(g, Integer.toString(t.i), sc);
		int tr[][] = new int[3][3];
		for(int y = -1; y <= 1; y++) {
			for(int x = -1; x <= 1; x++) {
				if((x == 0) && (y == 0))
					continue;
				tr[x + 1][y + 1] = gettilen(tc.add(new Coord(x, y)));
			}
		}
		if(tr[0][0] >= tr[1][0]) tr[0][0] = -1;
		if(tr[0][0] >= tr[0][1]) tr[0][0] = -1;
		if(tr[2][0] >= tr[1][0]) tr[2][0] = -1;
		if(tr[2][0] >= tr[2][1]) tr[2][0] = -1;
		if(tr[0][2] >= tr[0][1]) tr[0][2] = -1;
		if(tr[0][2] >= tr[1][2]) tr[0][2] = -1;
		if(tr[2][2] >= tr[2][1]) tr[2][2] = -1;
		if(tr[2][2] >= tr[1][2]) tr[2][2] = -1;
		int bx[] = {0, 1, 2, 1};
		int by[] = {1, 0, 1, 2};
		int cx[] = {0, 2, 2, 0};
		int cy[] = {0, 0, 2, 2};
		for(int i = gettilen(tc) - 1; i >= 0; i--) {
			int bm = 0, cm = 0;
			for(int o = 0; o < 4; o++) {
				if(tr[bx[o]][by[o]] == i)
					bm |= 1 << o;
				if(tr[cx[o]][cy[o]] == i)
					cm |= 1 << o;
			}
			if(bm != 0)
				g.image(map.sets.get(i).btrans[bm - 1].pick(map.randoom(tc)).tex(), sc);
			if(cm != 0)
				g.image(map.sets.get(i).ctrans[cm - 1].pick(map.randoom(tc)).tex(), sc);
		}
	}
	
	private void drawol(GOut g, Coord tc, Coord sc) {
		int ol;
		int i;
		double w = 2;
		
		ol = getol(tc) & visol;
		if(ol == 0)
			return;
		for(i = 0; i < olc.length; i++) {
			if(((ol & ~getol(tc.add(new Coord(-1, 0)))) & (1 << i)) != 0) {
				g.chcolor(olc[i]);
				g.line(sc.add(m2s(new Coord(0, tilesz.y))), sc, w);
			}
			if(((ol & ~getol(tc.add(new Coord(0, -1)))) & (1 << i)) != 0) {
				g.chcolor(olc[i]);
				g.line(sc.add(new Coord(1, 0)), sc.add(m2s(new Coord(tilesz.x, 0))).add(new Coord(1, 0)), w);
			}
			if(((ol & ~getol(tc.add(new Coord(1, 0)))) & (1 << i)) != 0) {
				g.chcolor(olc[i]);
				g.line(sc.add(m2s(new Coord(tilesz.x, 0))).add(new Coord(1, 0)), sc.add(m2s(new Coord(tilesz.x, tilesz.y))).add(new Coord(1, 0)), w);
			}
			if(((ol & ~getol(tc.add(new Coord(0, 1)))) & (1 << i)) != 0) {
				g.chcolor(olc[i]);
				g.line(sc.add(m2s(new Coord(tilesz.x, tilesz.y))), sc.add(m2s(new Coord(0, tilesz.y))), w);
			}
		}
	}
	
	public void drawmap(GOut g) {
		int x, y, i;
		int stw, sth;
		Coord oc, tc, ctc, sc;
		
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
				DrawOffset dro = gob.getattr(DrawOffset.class);
				Drawable d = gob.getattr(Drawable.class);
				if(d != null) {
					Coord ulc = dc.add(d.getoffset().inv());
					if(dro != null)
						ulc = ulc.add(dro.off);
					Coord lrc = ulc.add(d.getsize());
					if((lrc.x > 0) && (lrc.y > 0) && (ulc.x <= sz.x) && (ulc.y <= sz.y)) {
						d.setup(drawer, dc, ulc);
						clickable.add(d);
					}
				}
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
			for(Sprite.Part part : sprites)
				part.draw(g);
			mask.redraw(lumin);
			g.image(mask, Coord.z);
			for(Speaking s : speaking) {
				s.draw(g, s.gob.sc.add(s.off));
			}
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
		double p2 = Math.PI * 2;
		double sa = -Math.cos(a.dt * p2);
		double la = anorm(-Math.cos(a.mp * p2));
		double hs = Math.pow(Math.sin(a.dt * p2), 2);
		double nl = clip(-sa * 2, 0, 1);
		hs = clip((hs - 0.5) * 2, 0, 1);
		double ml = 0.1 + la * 0.2;
		sa = anorm(clip(sa * 1.5, -1, 1));
		double ll = ml + ((1 - ml) * sa);
		mask.amb = mkc(hs * 0.4, hs * 0.2, nl * 0.25 * ll, 1 - ll);
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
			g.chcolor(Color.WHITE);
			g.atext(mc.toString(), new Coord(10, 590), 0, 1);
			g.atext("Hunger meter: " + glob.glut / (glob.fcap / 100) + "%", new Coord(10, 560), 0, 1);
			g.atext("Stamina meter: " + glob.stamina / (glob.stamcap / 100) + "%", new Coord(10, 575), 0, 1);
		} catch(Loading l) {
			String text = "Loading...";
			g.chcolor(Color.BLACK);
			g.frect(Coord.z, sz);
			g.chcolor(Color.WHITE);
			g.atext(text, sz.div(2), 0.5, 0.5);
		}
		super.draw(g);
	}
	
	public void drop(Coord cc, Coord ul) {
		wdgmsg("drop");
	}
}
