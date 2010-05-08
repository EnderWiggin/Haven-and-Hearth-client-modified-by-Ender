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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.*;

public class Layered extends Drawable {
    List<Indir<Resource>> layers;
    Map<Indir<Resource>, Sprite> sprites = new TreeMap<Indir<Resource>, Sprite>();
    Map<Indir<Resource>, Integer> delays = new TreeMap<Indir<Resource>, Integer>();
    final Indir<Resource> base;
    boolean loading;
    static LayerCache cache = new LayerCache(1000);
    Map<Layer, Sprite.Part> pcache = new WeakHashMap<Layer, Sprite.Part>();
	
    public static class Layer {
	BufferedImage img;
	Tex tex = null;
	Coord cc;
	Tex ol = null;
	
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
	
	public Tex ol() {
	    if(ol == null)
		ol = new TexI(Utils.outline(img, java.awt.Color.YELLOW));
	    return(ol);
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
	
    public synchronized boolean checkhit(Coord c) {
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
	/* XXX: Fix this to construct parts dynamically depending on
	 * which layers exist. */
	Sprite.Part me;
	me = makepart(0);
	me.setup(cc, off);
	drw.addpart(me);
	me = makepart(-10);
	me.setup(cc, off);
	drw.addpart(me);
    }
	
    private synchronized Object[] stateid(Object... extra) {
	Object[] ret = new Object[layers.size() + extra.length];
	for(int i = 0; i < layers.size(); i++) {
	    Sprite spr = sprites.get(layers.get(i));
	    if(spr == null)
		ret[i] = null;
	    else
		ret[i] = spr.stateid();
	}
	for(int i = 0; i < extra.length; i++)
	    ret[i + layers.size()] = extra[i];
	return(ArrayIdentity.intern(ret));
    }

    private Layer redraw(final int z) {
	final ArrayList<Sprite.Part> parts = new ArrayList<Sprite.Part>();
	Sprite.Drawer drw = new Sprite.Drawer() {
		public void addpart(Sprite.Part p) {
		    if(p.z == z)
			parts.add(p);
		}
	    };
	for(Sprite spr : sprites.values()) {
	    if(spr != null)
		spr.setup(drw, Coord.z, Coord.z);
	}
	Collections.sort(parts, Sprite.partcmp);
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
	for(Sprite.Part part : parts) {
	    part.cc = part.cc.add(ul.inv());
	    part.draw(buf, g);
	}
	g.dispose();
	return(new Layer(buf, ul.inv()));
    }

    private Sprite.Part makepart(int z) {
	final Layer l;
	synchronized(Layered.this) {
	    Object[] id = stateid(z);
	    synchronized(cache) {
		Layer ll = cache.get(id);
		if(ll == null) {
		    ll = redraw(z);
		    cache.put(id, ll);
		}
		l = ll;
	    }
	}
	synchronized(pcache) {
	    Sprite.Part p = pcache.get(l);
	    if(p == null) {
		p = new Sprite.Part(z) {
			public void draw(BufferedImage buf, Graphics g) {
			    g.drawImage(l.img, -l.cc.x, -l.cc.y, null);
			}
				
			public void draw(GOut g) {
			    g.image(l.tex(), cc.add(l.cc.inv()).add(off));
			}
		
			public void drawol(GOut g) {
			    g.image(l.ol(), cc.add(l.cc.inv()).add(off).add(-1, -1));
			}
		
			public void setup(Coord cc, Coord off) {
			    super.setup(cc, off);
			    ul = cc.add(l.cc.inv());
			    lr = ul.add(l.tex().sz());
			}
		
			public boolean checkhit(Coord c) {
			    return(Layered.this.checkhit(c));
			}
		    };
		pcache.put(l, p);
	    }
	    return(p);
	}
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
