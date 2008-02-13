package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.Transparency;
import java.util.*;

public class MapView extends Widget implements DTarget {
	List<TileSet> sets = new LinkedList<TileSet>();
	Map<Coord, Grid> req = new TreeMap<Coord, Grid>();
	Map<Coord, Grid> grids = new TreeMap<Coord, Grid>();
	Coord mc;
	List<Gob> clickable = null;
	public static final Coord tilesz = new Coord(8, 8);
	public static final Coord cmaps = new Coord(100, 100);
	
	static {
		Widget.addtype("mapview", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new MapView(c, (Coord)args[0], parent, (Coord)args[1]));
			}
		});
	}
	
	private class Loading extends RuntimeException {}
	
	private class TileSet {
		List<Tile> tiles = new ArrayList<Tile>();
		CPImage[] bt = new CPImage[15];
		CPImage[] ct = new CPImage[15];
		int tw = 0;
		Random gen = new Random();
		
		public TileSet(String name) {
			int w = 1, n = 1;
			boolean loadtrn = true;
			Scanner s = new Scanner(Resource.gettext(String.format("gfx/tiles/%s/info", name)));
			try {
				while(true) {
					String cmd = s.next().intern();
					if(cmd == "w") {
						w = s.nextInt();
					} else if(cmd == "load") {
						for(int i = s.nextInt(); i > 0; i--) {
							tiles.add(new Tile(new CPImage(Resource.loadimg(String.format("gfx/tiles/%s/%02d.gif", name, n++)), MapView.this), w));
							tw += w;
						}
					} else if(cmd == "notrans") {
						loadtrn = false;
					}
				}
			} catch(NoSuchElementException e) {}
			if(loadtrn) {
				for(int i = 0; i < 15; i++) {
					bt[i] = new CPImage(Resource.loadimg(String.format("gfx/tiles/%s/transitions/00-%02d.gif", name, i)), MapView.this);
					ct[i] = new CPImage(Resource.loadimg(String.format("gfx/tiles/%s/transitions/01-%02d.gif", name, i)), MapView.this);
				}
			}
		}
		
		public Tile get(Coord c) {
			int r = 1;
			gen.setSeed(c.x);
			gen.setSeed(gen.nextInt() * c.y);
			r = gen.nextInt();
			r %= tw;
			for(Tile t : tiles) {
				if((r -= t.w) <= 0)
					return(t);
			}
			throw(new RuntimeException("Barda"));
		}
	}
	
	private class Tile {
		CPImage img;
		int w;
		
		Tile(CPImage img, int w) {
			this.img = img;
			this.w = w;
		}
	}
	
	private class Grid {
		public int tiles[][];
		public long lastreq = 0;
		
		public Grid() {
			tiles = new int[cmaps.x][cmaps.y];
			for(int y = 0; y < 50; y++) {
				for(int x = 0; x < 50; x++)
					tiles[x][y] = -1;
			}
		}
		
		public int gettile(Coord tc) {
			return(tiles[tc.x][tc.y]);
		}
	}
	
	public MapView(Coord c, Coord sz, Widget parent, Coord mc) {
		super(c, sz, parent);
		Scanner s = new Scanner(Resource.gettext("gfx/tiles/tilesets"));
		try {
			while(true)
				sets.add(new TileSet(s.nextLine()));
		} catch(NoSuchElementException e) {
		}
		s.close();
		this.mc = mc;
		Session.current.mapdispatch = this;
		setcanfocus(true);
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
	
	private int gettile(Coord tc) {
		Grid g;
		synchronized(grids) {
			g = grids.get(tc.div(cmaps));
		}
		if(g == null)
			throw(new Loading());
		int t = g.gettile(tc.mod(cmaps));
		t %= sets.size();
		return(t);
	}
	
	public boolean mousedown(Coord c, int button) {
		setfocus(this);
		Drawable hit = null;
		for(Gob g : clickable) {
			Drawable d = g.getattr(Drawable.class);
			if(d == null)
				continue;
			Coord ulc = g.sc.add(d.getoffset().inv());
			if(c.isect(ulc, d.getsize())) {
				if(d.checkhit(c.add(ulc.inv()))) {
					hit = d;
					break;
				}
			}
		}
		Coord mc = s2m(c.add(viewoffset(sz, this.mc).inv()));
		if(hit == null)
			wdgmsg("click", c, mc, button);
		else
			wdgmsg("click", c, mc, button, hit.gob.id, hit.gob.getc());
		return(true);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "move") {
			mc = (Coord)args[0];
		} else {
			super.uimsg(msg, args);
		}
	}
	
	private void drawtile(Graphics g, Coord tc, Coord sc) {
		Tile t;
		
		t = sets.get(gettile(tc)).get(tc);
		t.img.draw(g, sc);
		int tr[][] = new int[3][3];
		for(int y = -1; y <= 1; y++) {
			for(int x = -1; x <= 1; x++) {
				if((x == 0) && (y == 0))
					continue;
				tr[x + 1][y + 1] = gettile(tc.add(new Coord(x, y)));
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
		for(int i = gettile(tc) - 1; i >= 0; i--) {
			int bm = 0, cm = 0;
			for(int o = 0; o < 4; o++) {
				if(tr[bx[o]][by[o]] == i)
					bm |= 1 << o;
				if(tr[cx[o]][cy[o]] == i)
					cm |= 1 << o;
			}
			if(bm != 0)
				sets.get(i).bt[bm - 1].draw(g, sc);
			if(cm != 0)
				sets.get(i).ct[cm - 1].draw(g, sc);
		}
	}
	
	public void drawmap(Graphics g) {
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
					sc.x -= (tilesz.x - 1) * 2;
					drawtile(g, ctc, sc);
				}
			}
		}
		
		ArrayList<Gob> shadows = new ArrayList<Gob>();
		ArrayList<Gob> sprites = new ArrayList<Gob>();
		ArrayList<Gob> clickable = new ArrayList<Gob>();
		ArrayList<Gob> speaking = new ArrayList<Gob>();
		synchronized(Session.current.oc) {
			for(Map.Entry<Integer, Gob> e : Session.current.oc.objs.entrySet()) {
				Gob gob = e.getValue();
				/* int id = e.getKey(); */
				Coord dc = m2s(gob.getc()).add(oc);
				gob.sc = dc;
				Drawable d = gob.getattr(Drawable.class);
				Sprite sdw = null;
				if(d != null) {
					sdw = d.shadow();
					Coord ulc = dc.add(d.getoffset().inv());
					Coord lrc = ulc.add(d.getsize());
					if((lrc.x > 0) && (lrc.y > 0) && (ulc.x <= sz.x) && (ulc.y <= sz.y)) {
						sprites.add(gob);
						clickable.add(gob);
					}
				}
				if(sdw != null) {
					Coord ulc = dc.add(sdw.cc.inv());
					Coord lrc = ulc.add(sdw.sz);
					if((lrc.x > 0) && (lrc.y > 0) && (ulc.x <= sz.x) && (ulc.y <= sz.y))
						shadows.add(gob);
				}
				Speaking s = gob.getattr(Speaking.class);
				if(s != null)
					speaking.add(gob);
			}
			Collections.sort(clickable, new Comparator<Gob>() {
					public int compare(Gob a, Gob b) {
						if(a.clprio != b.clprio)
							return(a.clprio - b.clprio);
						return(b.sc.y - a.sc.y);
					}
				});
			this.clickable = clickable;
			Collections.sort(sprites, new Comparator<Gob>() {
					public int compare(Gob a, Gob b) {
						return(a.sc.y - b.sc.y);
					}
				});
			for(Gob gob : shadows) {
				Drawable d = gob.getattr(Drawable.class);
				Sprite s = d.shadow();
				Coord dc = gob.sc;
				dc = dc.add(s.cc.inv());
				//g.drawImage(s.img, dc.x, dc.y, null);
			}
			for(Gob gob : sprites) {
				Drawable d = gob.getattr(Drawable.class);
				Coord dc = gob.sc;
				DrawOffset dro = gob.getattr(DrawOffset.class);
				if(dro != null)
					dc = dc.add(dro.off);
				d.draw(g, dc);
				/*
				  g.setColor(Color.WHITE);
				  g.drawString(Integer.toString(d.id), d.sc.x, d.sc.y);
				*/
			}
			for(Gob gob : speaking) {
				Speaking s = gob.getattr(Speaking.class);
				s.draw(g, gob.sc.add(s.off));
			}
		}
	}
	
	public void mapdata(Message msg) {
		Coord c = msg.coord();
		synchronized(req) {
			synchronized(grids) {
				if(req.containsKey(c)) {
					Grid g = req.get(c);
					for(int y = 0; y < cmaps.y; y++) {
						for(int x = 0; x < cmaps.x; x++)
							g.tiles[x][y] = msg.uint8();
					}
					req.remove(c);
					grids.put(c, g);
				}
			}
		}
	}
	
	public void draw(Graphics g) {
		Coord gc = mc.div(tilesz).div(cmaps);
		for(int y = -1; y <= 1; y++) {
			for(int x = -1; x <= 1; x++) {
				Coord cgc = gc.add(new Coord(x, y));
				if((grids.get(cgc) == null) && (req.get(cgc) == null))
					req.put(cgc, new Grid());
			}
		}
		long now = System.currentTimeMillis();
		synchronized(req) {
			for(Map.Entry<Coord, Grid> e : req.entrySet()) {
				Coord c = e.getKey();
				Grid gr = e.getValue();
				if(now - gr.lastreq > 1000) {
					gr.lastreq = now;
					Message msg = new Message(Session.MSG_MAPREQ);
					msg.addcoord(c);
					Session.current.sendmsg(msg);
				}
			}
		}
		try {
			drawmap(g);
			g.setColor(java.awt.Color.WHITE);
			Utils.drawtext(g, mc.toString(), new Coord(0, 20));
		} catch(Loading l) {
			String text = "Loading...";
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, sz.x, sz.y);
			g.setColor(Color.WHITE);
			FontMetrics m = g.getFontMetrics();
			Rectangle2D b = m.getStringBounds(text, g);
			g.drawString(text, sz.x / 2 - (int)b.getWidth() / 2, sz.y / 2 - m.getAscent());
		}
		super.draw(g);
	}
	
	public void drop(Coord cc, Coord ul) {
		wdgmsg("drop");
	}
}
