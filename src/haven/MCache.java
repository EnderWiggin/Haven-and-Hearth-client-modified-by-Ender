package haven;

import java.util.*;

public class MCache {
	List<TileSet> sets = new LinkedList<TileSet>();
	java.util.Map<Coord, Grid> req = new TreeMap<Coord, Grid>();
	java.util.Map<Coord, Grid> grids = new TreeMap<Coord, Grid>();
	Session sess;
	Set<Overlay> ols = new HashSet<Overlay>();
	public static final Coord tilesz = new Coord(11, 11);
	public static final Coord cmaps = new Coord(100, 100);
	
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
	
	public class TileSet {
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
		
		@SuppressWarnings("serial")
		public class FlavorList extends WeigthList<FSprite> {}
		@SuppressWarnings("serial")
		public class TileList extends WeigthList<Tile> {}
		
		@SuppressWarnings("serial")
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

	public interface Weigthed {
		int w();
	}
	
	public class FSprite implements Weigthed {
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
	
	public class Tile implements Weigthed {
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
	
	public class GridTile {
		public int tile = -1;
		public int ol = -1;
	}
	
	public class Grid {
		public GridTile tiles[][];
		Collection<Gob> fo = new LinkedList<Gob>();
		public long lastreq = 0;
		Coord gc;
		OCache oc = sess.glob.oc;
		
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
			oc.lrem(fo);
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
			oc.ladd(fo);
		}
	}

	public MCache(Session sess) {
		this.sess = sess;
		Scanner s = new Scanner(Resource.gettext("gfx/tiles/tilesets"));
		try {
			while(true)
				sets.add(new TileSet(s.nextLine()));
		} catch(NoSuchElementException e) {}
		s.close();
	}

	public void invalidate(Coord cc) {
		synchronized(req) {
			if(req.get(cc) == null)
				req.put(cc, new Grid(cc));
		}
	}
	
	public GridTile gettile(Coord tc) {
		Grid g;
		synchronized(grids) {
			g = grids.get(tc.div(cmaps));
		}
		if(g == null)
			return(null);
		return(g.gettile(tc.mod(cmaps)));
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
					try {
						g.makeflavor();
					} catch(Exception e) {
						e.printStackTrace();
					}
					grids.put(c, g);
				}
			}
		}
	}
	
	public void trim(Coord cc) {
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
	
	public void request(Coord gc) {
		synchronized(req) {
			if(!req.containsKey(gc))
				req.put(gc, new Grid(gc));
		}
	}
	
	public void sendreqs() {
		long now = System.currentTimeMillis();
		synchronized(req) {
			for(Map.Entry<Coord, Grid> e : req.entrySet()) {
				Coord c = e.getKey();
				Grid gr = e.getValue();
				if(now - gr.lastreq > 1000) {
					gr.lastreq = now;
					Message msg = new Message(Session.MSG_MAPREQ);
					msg.addcoord(c);
					sess.sendmsg(msg);
				}
			}
		}
	}
}
