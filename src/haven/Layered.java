package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.*;

public class Layered extends Drawable {
	List<Resource> layers;
	Map<Resource, Sprite> sprites = new TreeMap<Resource, Sprite>();
	Map<Resource, Integer> delays = new TreeMap<Resource, Integer>();
	final Resource base;
	boolean loading;
	int z = 0;
	Sprite.Part me;
	static LayerCache cache = new LayerCache(1000);
	
	public static class LayerCache {
		private int cachesz;
		private Map<Object[], Tex> cache = new IdentityHashMap<Object[], Tex>();
		private LinkedList<Object[]> recency = new LinkedList<Object[]>();
		
		public LayerCache(int cachesz) {
			this.cachesz = cachesz;
		}

		private synchronized void usecache(Object[] id) {
			for(Iterator i = recency.iterator(); i.hasNext();) {
				Object[] cid = (Object[])i.next();
				if(cid == id) {
					i.remove();
					recency.addFirst(id);
					return;
				}
			}
			throw(new RuntimeException("Used layered cache is not in recency list"));
		}
	
		public synchronized Tex get(Object[] id) {
			Tex t = cache.get(id);
			if(t != null)
				usecache(id);
			return(t);
		}
	
		private synchronized void cleancache() {
			while(recency.size() > cachesz) {
				Object[] id = recency.removeLast();
				cache.remove(id).dispose();
			}
		}
		
		public synchronized void put(Object[] id, Tex t) {
			cache.put(id, t);
			recency.addFirst(id);
			cleancache();
		}
	}

	public Layered(Gob gob, Resource base) {
		super(gob);
		this.base = base;
		layers = new ArrayList<Resource>();
		makepart();
	}

	public synchronized void setlayers(List<Resource> layers) {
		Collections.sort(layers);
		if(layers.equals(this.layers))
			return;
		loading = true;
		this.layers = layers;
		delays = new TreeMap<Resource, Integer>();
		sprites = new TreeMap<Resource, Sprite>();
		for(Resource r : layers) {
			delays.put(r, 0);
			sprites.put(r, null);
		}
	}
	
	public boolean checkhit(Coord c) {
		if(base.loading)
			return(false);
		for(Sprite spr : sprites.values()) {
			if(spr == null)
				continue;
			if(spr.checkhit(c))
				return(true);
		}
		return(false);
	}

	public synchronized void setup(Sprite.Drawer drw, final Coord cc, final Coord off) {
		if(base.loading)
			return;
		if(loading) {
			loading = false;
			for(Resource r : layers) {
				if(sprites.get(r) == null) {
					if(r.loading)
						loading = true;
					else
						sprites.put(r, Sprite.create(gob, r, base));
				}
			}
		}
		me.cc = cc;
		me.off = off;
		drw.addpart(me);
	}
	
	private synchronized Object[] stateid() {
		Object[] ret = new Object[layers.size()];
		for(int i = 0; i < layers.size(); i++) {
			Sprite spr = sprites.get(layers.get(i));
			if(spr == null)
				ret[i] = null;
			else
				ret[i] = spr.stateid();
		}
		return(ArrayIdentity.intern(ret));
	}

	private void makepart() {
		me = new Sprite.Part(z) {
				public void draw(BufferedImage buf, Graphics g, Coord cc, Coord off) {
					final ArrayList<Sprite.Part> parts = new ArrayList<Sprite.Part>();
					Sprite.Drawer drw = new Sprite.Drawer() {
							public void addpart(Sprite.Part p) {
								parts.add(p);
							}
						};
					for(Sprite spr : sprites.values()) {
						if(spr != null)
							spr.setup(drw, cc, off);
					}
					Collections.sort(parts);
					for(Sprite.Part part : parts)
						part.draw(buf, g);
				}
				
				public void draw(BufferedImage buf, Graphics g) {
					synchronized(Layered.this) {
						draw(buf, g, cc, off);
					}
				}
				
				public void draw(GOut g) {
					synchronized(Layered.this) {
						Object[] id = stateid();
						Tex t;
						synchronized(cache) {
							if((t = cache.get(id)) == null) {
								Coord sz = getsize();
								BufferedImage buf = TexI.mkbuf(sz);
								Graphics gr = buf.getGraphics();
								draw(buf, gr, getoffset(), Coord.z);
								t = new TexI(buf);
								cache.put(id, t);
							}
						}
						g.image(t, cc.add(getoffset().inv()).add(off));
					}
				}
			};
	}
	public Coord getoffset() {
		if(base.loading)
			return(Coord.z);
		return(base.layer(Resource.negc).cc);
	}

	public Coord getsize() {
		if(base.loading)
			return(Coord.z);
		return(base.layer(Resource.negc).sz);
	}

	public void ctick(int dt) {
		for(Map.Entry<Resource, Sprite> e : sprites.entrySet()) {
			Resource r = e.getKey();
			Sprite spr = e.getValue();
			if(spr != null) {
				int ldt = dt;
				if(delays.get(r) != null) {
					ldt += delays.get(r);
					delays.remove(r);
				}
				spr.tick(ldt);
			} else {
				delays.put(r, delays.get(r) + dt);
			}
		}
	}
}
