package haven;

import java.util.*;
import haven.Resource.Tileset;
import haven.Resource.Tile;

public class MCache {
	Tileset[] sets = null;
	Grid last = null;
	java.util.Map<Coord, Grid> req = new TreeMap<Coord, Grid>();
	java.util.Map<Coord, Grid> grids = new TreeMap<Coord, Grid>();
	Session sess;
	Set<Overlay> ols = new HashSet<Overlay>();
	public static final Coord tilesz = new Coord(11, 11);
	public static final Coord cmaps = new Coord(100, 100);
	Random gen;
	
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
	
	public class Grid {
		public int tiles[][];
		public Tile gcache[][];
		public Tile tcache[][][];
		public int ol[][];
		Collection<Gob> fo = new LinkedList<Gob>();
		boolean regged = false;
		public long lastreq = 0;
		Coord gc;
		OCache oc = sess.glob.oc;
		
		public Grid(Coord gc) {
			this.gc = gc;
			tiles = new int[cmaps.x][cmaps.y];
			ol = new int[cmaps.x][cmaps.y];
			gcache = new Tile[cmaps.x][cmaps.y];
		        tcache = new Tile[cmaps.x][cmaps.y][];
		}
		
		public int gettile(Coord tc) {
			return(tiles[tc.x][tc.y]);
		}
		
		public int getol(Coord tc) {
			return(ol[tc.x][tc.y]);
		}
		
		public void remove() {
			if(regged) {
				oc.lrem(fo);
				regged = false;
			}
		}
		
		public void makeflavor() {
			fo.clear();
			Coord c = new Coord(0, 0);
			Coord tc = gc.mul(cmaps);
			for(c.y = 0; c.y < cmaps.x; c.y++) {
				for(c.x = 0; c.x < cmaps.y; c.x++) {
					Tileset set = sets[tiles[c.x][c.y]];
					if(set.flavobjs.size() > 0) {
						Random rnd = mkrandoom(c);
						if(rnd.nextInt(set.flavprob) == 0) {
							Resource r = set.flavobjs.pick(rnd);
							Gob g = new Gob(sess.glob, c.add(tc).mul(tilesz), 0, 0); 
							g.setattr(new ResDrawable(g, r));
							fo.add(g);
						}
					}
				}
			}
			if(!regged) {
				oc.ladd(fo);
				regged = true;
			}
		}
		
		public int randoom(Coord c, int r) {
			return(MCache.this.randoom(c.add(gc.mul(cmaps)), r));
		}

		public Random mkrandoom(Coord c) {
			return(MCache.this.mkrandoom(c.add(gc.mul(cmaps))));
		}
	}
	
	private Tileset loadset(String name) {
		Resource res = Resource.load(name);
		res.loadwait();
		return(res.layer(Resource.tileset));
	}
	
	public MCache(Session sess) {
		this.sess = sess;
		sets = new Tileset[256];
		sets[0] = loadset("gfx/tiles/wald/wald");
		sets[1] = loadset("gfx/tiles/grass/grass");
		sets[2] = loadset("gfx/tiles/swamp/swamp");
		sets[3] = loadset("gfx/tiles/dirt/dirt");
		sets[4] = loadset("gfx/tiles/playa/playa");
		sets[5] = loadset("gfx/tiles/water/water");
		sets[6] = loadset("gfx/tiles/plowed/plowed");
		sets[7] = loadset("gfx/tiles/floor-wood/floor-wood");
		sets[8] = loadset("gfx/tiles/floor-mine/mine");
		sets[255] = loadset("gfx/tiles/nil/nil");
		gen = new Random();
	}

	private static void initrandoom(Random r, Coord c) {
		r.setSeed(c.x);
		r.setSeed(r.nextInt() ^ c.y);
	}

	public int randoom(Coord c) {
		int ret;
		
		synchronized(gen) {
			initrandoom(gen, c);
			ret = Math.abs(gen.nextInt());
			return(ret);
		}
	}
	
	public int randoom(Coord c, int r) {
		return(randoom(c) % r);
	}
	
	public static Random mkrandoom(Coord c) {
		Random ret = new Random();
		initrandoom(ret, c);
		return(ret);
	}

	private void replace(Grid g) {
		if(g == last)
			last = null;
	}

	public void invalidate(Coord cc) {
		synchronized(req) {
			if(req.get(cc) == null)
				req.put(cc, new Grid(cc));
		}
	}
	
	public Tile[] gettrans(Coord tc) {
		Grid g;
		synchronized(grids) {
			Coord gc = tc.div(cmaps);
			if((last != null) && last.gc.equals(gc))
				g = last;
			else
				last = g = grids.get(gc);
		}
		if(g == null)
			return(null);
		Coord gtc = tc.mod(cmaps);
		if(g.tcache[gtc.x][gtc.y] == null) {
			int tr[][] = new int[3][3];
			for(int y = -1; y <= 1; y++) {
				for(int x = -1; x <= 1; x++) {
					if((x == 0) && (y == 0))
						continue;
					int tn = gettilen(tc.add(new Coord(x, y)));
					if(tn < 0)
						return(null);
					tr[x + 1][y + 1] = tn;
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
			ArrayList<Tile> buf = new ArrayList<Tile>();
			for(int i = gettilen(tc) - 1; i >= 0; i--) {
				if((sets[i] == null) || (sets[i].btrans == null) || (sets[i].ctrans == null))
					continue;
				int bm = 0, cm = 0;
				for(int o = 0; o < 4; o++) {
					if(tr[bx[o]][by[o]] == i)
						bm |= 1 << o;
					if(tr[cx[o]][cy[o]] == i)
						cm |= 1 << o;
				}
				if(bm != 0)
					buf.add(sets[i].btrans[bm - 1].pick(randoom(tc)));
				if(cm != 0)
					buf.add(sets[i].ctrans[cm - 1].pick(randoom(tc)));
			}
			g.tcache[gtc.x][gtc.y] = buf.toArray(new Tile[0]);
		}
		return(g.tcache[gtc.x][gtc.y]);
	}

	public Tile getground(Coord tc) {
		Grid g;
		synchronized(grids) {
			Coord gc = tc.div(cmaps);
			if((last != null) && last.gc.equals(gc))
				g = last;
			else
				last = g = grids.get(gc);
		}
		if(g == null)
			return(null);
		Coord gtc = tc.mod(cmaps);
		if(g.gcache[gtc.x][gtc.y] == null) {
			Tileset ts = sets[g.gettile(gtc)];
			g.gcache[gtc.x][gtc.y] = ts.ground.pick(randoom(tc));
		}
		return(g.gcache[gtc.x][gtc.y]);
	}

	public int gettilen(Coord tc) {
		Grid g;
		synchronized(grids) {
			Coord gc = tc.div(cmaps);
			if((last != null) && last.gc.equals(gc))
				g = last;
			else
				last = g = grids.get(gc);
		}
		if(g == null)
			return(-1);
		return(g.gettile(tc.mod(cmaps)));
	}
	
	public Tileset gettile(Coord tc) {
		int tn = gettilen(tc);
		if(tn == -1)
			return(null);
		return(sets[tn]);
	}
	
	public int getol(Coord tc) {
		Grid g;
		synchronized(grids) {
			Coord gc = tc.div(cmaps);
			if((last != null) && last.gc.equals(gc))
				g = last;
			else
				last = g = grids.get(gc);
		}
		if(g == null)
			return(-1);
		int ol = g.getol(tc.mod(cmaps));
		for(Overlay lol : ols) {
			if(tc.isect(lol.c1, lol.c2.add(lol.c1.inv()).add(new Coord(1, 1))))
				ol |= lol.mask;
		}
		return(ol);
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
							g.tiles[x][y] = t;
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
							g.ol[x][y] = t;
							l--;
						}
					}
					req.remove(c);
					g.makeflavor();
					if(grids.containsKey(c)) {
						grids.get(c).remove();
						grids.remove(c);
					}
					replace(grids.put(c, g));
				}
			}
		}
	}
	
	public void trimall() {
		grids.clear();
		req.clear();
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
		for(Iterator<Map.Entry<Coord, Grid>> i = req.entrySet().iterator(); i.hasNext();) {
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
