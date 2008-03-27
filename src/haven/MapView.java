package haven;

import java.awt.Color;
import java.util.*;

public class MapView extends Widget implements DTarget {
	List<TileSet> sets = new LinkedList<TileSet>();
	Map<Coord, Grid> req = new TreeMap<Coord, Grid>();
	Map<Coord, Grid> grids = new TreeMap<Coord, Grid>();
	Coord mc;
	List<Gob> clickable = null;
	int visol = 0;
	Color[] olc = {new Color(255, 0, 128), new Color(0, 255, 0)};
	Grabber grab = null;
	Set<Overlay> ols = new HashSet<Overlay>();
	public static final Coord tilesz = new Coord(11, 11);
	public static final Coord cmaps = new Coord(100, 100);
	
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
	
	private class Loading extends RuntimeException {}
	
	public class Overlay {
		Coord c1, c2;
		int mask;
		
		public Overlay(Coord c1, Coord c2, int mask) {
			this.c1 = c1;
			this.c2 = c2;
			this.mask = mask;
			ols.add(this);
		}
		
		public void destroy() {
			ols.remove(this);
		}
		
		public void update(Coord c1, Coord c2) {
			this.c1 = c1;
			this.c2 = c2;
		}
	}
	
	private class TileSet {
		TileList tiles = new TileList();
		List<TileList> bt = new ArrayList<TileList>();
		List<TileList> ct = new ArrayList<TileList>();
		{
			for(int i = 0; i < 15; i++)
				bt.add(null);
			for(int i = 0; i < 15; i++)
				ct.add(null);
		}
		FlavorList flavors = new FlavorList();
		int fp = -1;
		Random gen = new Random();
		
		private class FlavorList extends WeigthList<FSprite> {}
		private class TileList extends WeigthList<Tile> {}
		
		private class WeigthList<T extends Weigthed> extends ArrayList<T> {
			int tw = 0;
			
			public boolean add(T t) {
				super.add(t);
				tw += t.w();
				return(true);
			}
			
			public T get(Coord c) {
				int r = randoom(c, tw) + 1;
				for(T t : this) {
					if((r -= t.w()) <= 0)
						return(t);
				}
				throw(new RuntimeException("Barda"));
			}
		}
		
		public int randoom(Coord c, int r) {
			int ret;
			
			if(c != null) {
				gen.setSeed(c.x);
				gen.setSeed(gen.nextInt() * c.y);
			}
			ret = Math.abs(gen.nextInt()) % r;
			ret %= r;
			return(ret);
		}
		
		private void loadflv(String dir) {
			int w = 1, cur = 1;
			Scanner s = new Scanner(Resource.gettext(dir + "/info"));
			try {
				while(true) {
					String cmd = s.next().intern();
					if(cmd == "w") {
						w = s.nextInt();
					} else if(cmd == "load") {
						for(int i = s.nextInt(); i > 0; i--)
							flavors.add(new FSprite(Resource.loadsprite(String.format("%s/%02d.spr", dir, cur++)), w));
					}
				}
			} catch(NoSuchElementException e) {}
		}
		
		private void loadtrans(String dir, List<TileList> trans) {
			int w = 1, cnum = 1;
			Scanner s = new Scanner(Resource.gettext(dir + "/info"));
			TileList cur = null;
			try {
				while(true) {
					String cmd = s.next().intern();
					if(cmd == "w") {
						w = s.nextInt();
					} else if(cmd == "load") {
						for(int i = s.nextInt(); i > 0; i--)
							cur.add(new Tile(Resource.loadtex(String.format("%s/01-%02d-%02d.gif", dir, cnum, i)), w));
					} else if(cmd == "trans") {
						cur = new TileList();
						cnum = s.nextInt();
						trans.set(cnum - 1, cur);
					}
				}
			} catch(NoSuchElementException e) {}
		}
		
		public TileSet(String name) {
			int w = 1, n = 1;
			boolean loadtrn = true;
			boolean loadflv = false;
			Scanner s = new Scanner(Resource.gettext(String.format("gfx/tiles/%s/info", name)));
			try {
				while(true) {
					String cmd = s.next().intern();
					if(cmd == "w") {
						w = s.nextInt();
					} else if(cmd == "load") {
						for(int i = s.nextInt(); i > 0; i--)
							tiles.add(new Tile(Resource.loadtex(String.format("gfx/tiles/%s/%02d.gif", name, n++)), w));
					} else if(cmd == "notrans") {
						loadtrn = false;
					} else if(cmd == "flavor") {
						loadflv = true;
						fp = s.nextInt();
					}
				}
			} catch(NoSuchElementException e) {}
			if(loadtrn) {
				loadtrans(String.format("gfx/tiles/%s/mtrans", name), bt);
				loadtrans(String.format("gfx/tiles/%s/ctrans", name), ct);
			}
			if(loadflv)
				loadflv(String.format("gfx/tiles/%s/flavobjs", name));
		}
	}

	private interface Weigthed {
		int w();
	}
	
	private class FSprite implements Weigthed {
		Sprite spr;
		int w;
		
		FSprite(Sprite spr, int w) {
			this.spr = spr;
			this.w = w;
		}
		
		public int w() {
			return(w);
		}
	}
	
	private class Tile implements Weigthed {
		Tex img;
		int w;
		
		Tile(Tex img, int w) {
			this.img = img;
			this.w = w;
		}
		
		public int w() {
			return(w);
		}
	}
	
	private class GridTile {
		public int tile = -1;
		public int ol = -1;
	}
	
	private class Grid {
		public GridTile tiles[][];
		Collection<Gob> fo = new LinkedList<Gob>();
		public long lastreq = 0;
		Coord gc;
		
		public Grid(Coord gc) {
			this.gc = gc;
			tiles = new GridTile[cmaps.x][cmaps.y];
			for(int y = 0; y < cmaps.x; y++) {
				for(int x = 0; x < cmaps.y; x++)
					tiles[x][y] = new GridTile();
			}
		}
		
		public GridTile gettile(Coord tc) {
			return(tiles[tc.x][tc.y]);
		}
		
		public void remove() {
			Session.current.oc.lrem(fo);
		}
		
		public void makeflavor() {
			Coord c = new Coord(0, 0);
			Coord tc = gc.mul(cmaps);
			for(c.y = 0; c.y < cmaps.x; c.y++) {
				for(c.x = 0; c.x < cmaps.y; c.x++) {
					TileSet set = sets.get(tiles[c.x][c.y].tile);
					if((set.fp != -1) && (set.randoom(c.add(tc), set.fp) == 0)) {
						FSprite f = set.flavors.get(null);
						Gob g = new Gob(c.add(tc).mul(tilesz), 0, 0); 
						g.setattr(new SimpleSprite(g, f.spr));
						fo.add(g);
					}
				}
			}
			Session.current.oc.ladd(fo);
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
	
	private GridTile gettile(Coord tc) {
		Grid g;
		synchronized(grids) {
			g = grids.get(tc.div(cmaps));
		}
		if(g == null)
			throw(new Loading());
		return(g.gettile(tc.mod(cmaps)));
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
		for(Iterator<Map.Entry<Coord, Grid>> i = grids.entrySet().iterator(); i.hasNext();) {
			Map.Entry<Coord, Grid> e = i.next();
			Coord gc = e.getKey();
			Grid g = e.getValue();
			if((Math.abs(gc.x - cc.x) > 1) || (Math.abs(gc.y - cc.y) > 1)) {
				i.remove();
				g.remove();
			}
		}
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
	
	private void drawtile(GOut g, Coord tc, Coord sc) {
		Tile t;
		
		t = sets.get(gettile(tc).tile).tiles.get(tc);
		g.image(t.img, sc);
		//g.setColor(FlowerMenu.pink);
		//Utils.drawtext(g, Integer.toString(t.i), sc);
		int tr[][] = new int[3][3];
		for(int y = -1; y <= 1; y++) {
			for(int x = -1; x <= 1; x++) {
				if((x == 0) && (y == 0))
					continue;
				tr[x + 1][y + 1] = gettile(tc.add(new Coord(x, y))).tile;
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
		for(int i = gettile(tc).tile - 1; i >= 0; i--) {
			int bm = 0, cm = 0;
			for(int o = 0; o < 4; o++) {
				if(tr[bx[o]][by[o]] == i)
					bm |= 1 << o;
				if(tr[cx[o]][cy[o]] == i)
					cm |= 1 << o;
			}
			if(bm != 0)
				g.image(sets.get(i).bt.get(bm - 1).get(tc).img, sc);
			if(cm != 0)
				g.image(sets.get(i).ct.get(cm - 1).get(tc).img, sc);
		}
	}
	
	private int getol(Coord tc) {
		int ol = gettile(tc).ol;
		for(Overlay lol : ols) {
			if(tc.isect(lol.c1, lol.c2.add(lol.c1.inv()).add(new Coord(1, 1))))
				ol |= lol.mask;
		}
		return(ol);
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
	
	public void invalidate(Coord cc) {
		synchronized(req) {
			if(req.get(cc) == null)
				req.put(cc, new Grid(cc));
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
		
		ArrayList<Gob> shadows = new ArrayList<Gob>();
		ArrayList<Gob> sprites = new ArrayList<Gob>();
		ArrayList<Gob> clickable = new ArrayList<Gob>();
		ArrayList<Gob> speaking = new ArrayList<Gob>();
		synchronized(Session.current.oc) {
			for(Gob gob : Session.current.oc) {
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
				g.image(s.tex, dc);
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
		int l = 0, t = 0;
		synchronized(req) {
			synchronized(grids) {
				if(req.containsKey(c)) {
					Grid g = req.get(c);
					for(int y = 0; y < cmaps.y; y++) {
						for(int x = 0; x < cmaps.x; x++) {
							if(l < 1) {
								l = msg.uint16();
								t = msg.uint8();
							}
							g.tiles[x][y].tile = t;
							l--;
						}
					}
					for(int y = 0; y < cmaps.y; y++) {
						for(int x = 0; x < cmaps.x; x++) {
							if(l < 1) {
								l = msg.uint16();
								t = msg.uint8();
								//System.out.println(l + ", " + t);
							}
							g.tiles[x][y].ol = t;
							l--;
						}
					}
					req.remove(c);
					g.makeflavor();
					grids.put(c, g);
				}
			}
		}
	}
	
	public void draw(GOut g) {
		Coord gc = mc.div(tilesz).div(cmaps);
		for(int y = -1; y <= 1; y++) {
			for(int x = -1; x <= 1; x++) {
				Coord cgc = gc.add(new Coord(x, y));
				if((grids.get(cgc) == null) && (req.get(cgc) == null))
					req.put(cgc, new Grid(cgc));
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
			g.chcolor(Color.WHITE);
			g.text(mc.toString(), new Coord(0, 20));
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
