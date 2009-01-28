package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.*;

public class Layered extends Drawable {
    List<Indir<Resource>> layers;
    Map<Indir<Resource>, Sprite> sprites = new TreeMap<Indir<Resource>, Sprite>();
    Map<Indir<Resource>, Integer> delays = new TreeMap<Indir<Resource>, Integer>();
    final Indir<Resource> base;
    boolean loading;
    int z = 0;
    static LayerCache cache = new LayerCache(1000);
	
    public static class Layer {
	BufferedImage img;
	Tex tex = null;
	Coord cc;
	
	public Layer(BufferedImage img, Coord cc) {
	    this.img = img;
	    this.cc = cc;
	}
	
	public Tex tex() {
	    if(tex != null)
		return(tex);
	    tex = new TexI(img);
	    return(tex);
	}
	
	public void dispose() {
	    if(tex != null)
		tex.dispose();
	}
    }

    public static class LayerCache {
	private int cachesz;
	private Map<Object[], Layer> cache = new IdentityHashMap<Object[], Layer>();
	private LinkedList<Object[]> recency = new LinkedList<Object[]>();
	private int cached;
		
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
		
	public synchronized int size() {
	    return(recency.size());
	}
		
	public synchronized int cached() {
	    return(cached);
	}
		
	public synchronized Layer get(Object[] id) {
	    Layer l = cache.get(id);
	    if(l != null)
		usecache(id);
	    return(l);
	}
	
	private synchronized void cleancache() {
	    while(recency.size() > cachesz) {
		Object[] id = recency.removeLast();
		cache.remove(id).dispose();
	    }
	}
		
	public synchronized void put(Object[] id, Layer l) {
	    cache.put(id, l);
	    recency.addFirst(id);
	    cleancache();
	    cached++;
	}
    }

    public Layered(Gob gob, Indir<Resource> base) {
	super(gob);
	this.base = base;
	layers = new ArrayList<Indir<Resource>>();
    }

    public synchronized void setlayers(List<Indir<Resource>> layers) {
	Collections.sort(layers);
	if(layers.equals(this.layers))
	    return;
	loading = true;
	this.layers = layers;
	delays = new TreeMap<Indir<Resource>, Integer>();
	sprites = new TreeMap<Indir<Resource>, Sprite>();
	for(Indir<Resource> r : layers) {
	    delays.put(r, 0);
	    sprites.put(r, null);
	}
    }
	
    public boolean checkhit(Coord c) {
	if(base.get() == null)
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
	if(base.get() == null)
	    return;
	if(loading) {
	    loading = false;
	    for(Indir<Resource> r : layers) {
		if(sprites.get(r) == null) {
		    if(r.get() == null)
			loading = true;
		    else
			sprites.put(r, Sprite.create(gob, r.get(), null));
		}
	    }
	}
	Sprite.Part me = makepart();
	me.setup(cc, off);
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

    private Layer redraw() {
	final ArrayList<Sprite.Part> parts = new ArrayList<Sprite.Part>();
	Sprite.Drawer drw = new Sprite.Drawer() {
		public void addpart(Sprite.Part p) {
		    parts.add(p);
		}
	    };
	for(Sprite spr : sprites.values()) {
	    if(spr != null)
		spr.setup(drw, Coord.z, Coord.z);
	}
	Collections.sort(parts);
	Coord ul = new Coord(0, 0);
	Coord lr = new Coord(0, 0);
	for(Sprite.Part part : parts) {
	    if(part.ul.x < ul.x)
		ul.x = part.ul.x;
	    if(part.ul.y < ul.y)
		ul.y = part.ul.y;
	    if(part.lr.x > lr.x)
		lr.x = part.lr.x;
	    if(part.lr.y > lr.y)
		lr.y = part.lr.y;
	}
	BufferedImage buf = TexI.mkbuf(lr.add(ul.inv()).add(1, 1));
	Graphics g = buf.getGraphics();
	/*
	g.setColor(java.awt.Color.RED);
	g.fillRect(0, 0, buf.getWidth(), buf.getHeight());
	*/
	g.translate(-ul.x, -ul.y);
	for(Sprite.Part part : parts)
	    part.draw(buf, g);
	g.dispose();
	return(new Layer(buf, ul.inv()));
    }

    private Sprite.Part makepart() {
	final Layer l;
	synchronized(Layered.this) {
	    Object[] id = stateid();
	    synchronized(cache) {
		Layer ll = cache.get(id);
		if(ll == null) {
		    ll = redraw();
		    cache.put(id, ll);
		}
		l = ll;
	    }
	}
	return(new Sprite.Part(z) {
		public void draw(BufferedImage buf, Graphics g) {
		    g.drawImage(l.img, -l.cc.x, -l.cc.y, null);
		}
				
		public void draw(GOut g) {
		    g.image(l.tex(), cc.add(l.cc.inv()).add(off));
		}
		
		public void setup(Coord cc, Coord off) {
		    super.setup(cc, off);
		    ul = cc.add(l.cc.inv());
		    lr = ul.add(l.tex().sz());
		}
	    });
    }

    public synchronized void ctick(int dt) {
	for(Map.Entry<Indir<Resource>, Sprite> e : sprites.entrySet()) {
	    Indir<Resource> r = e.getKey();
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
