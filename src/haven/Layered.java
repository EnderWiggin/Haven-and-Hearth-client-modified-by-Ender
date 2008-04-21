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
					draw(buf, g, cc, off);
				}
				
				public void draw(GOut g) {
					Coord sz = getsize();
					BufferedImage buf = TexI.mkbuf(sz);
					Graphics gr = buf.getGraphics();
					draw(buf, gr, getoffset(), Coord.z);
					g.image(buf, cc.add(getoffset().inv()).add(off));
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
