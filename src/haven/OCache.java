package haven;

import java.util.*;

public class OCache implements Iterable<Gob> {
	/* XXX: Use weak refs */
	private Collection<Collection<Gob>> local = new LinkedList<Collection<Gob>>();
	private Map<Integer, Gob> objs = new TreeMap<Integer, Gob>();
	private Map<Integer, Integer> deleted = new TreeMap<Integer, Integer>();
	private Glob glob;
	long lastctick = 0;
	
	public OCache(Glob glob) {
	    this.glob = glob;
	}
	
	public synchronized void remove(int id, int frame) {
		if(objs.containsKey(id)) {
			objs.remove(id);
			deleted.put(id, frame);
		}
	}
	
	public synchronized void tick() {
		for(Gob g : objs.values()) {
			g.tick();
		}
	}
	
	public void ctick() {
		long now;
		int dt;
		
		now = System.currentTimeMillis();
		if(lastctick == 0)
			dt = 0;
		else
			dt = (int)(System.currentTimeMillis() - lastctick);
		synchronized(this) {
			for(Gob g : objs.values())
				g.ctick(dt);
		}
		lastctick = now; 
	}
	
	public Iterator<Gob> iterator() {
		Collection<Iterator<Gob>> is = new LinkedList<Iterator<Gob>>();
		for(Collection<Gob> gc : local)
			is.add(gc.iterator());
		return(new I2<Gob>(objs.values().iterator(), new I2<Gob>(is)));
	}
	
	public synchronized void ladd(Collection<Gob> gob) {
		local.add(gob);
	}
	
	public synchronized void lrem(Collection<Gob> gob) {
		local.remove(gob);
	}
	
	public synchronized Gob getgob(int id) {
		return(objs.get(id));
	}
	
	public synchronized Gob getgob(int id, int frame) {
		if(!objs.containsKey(id)) {
			boolean r = false;
			if(deleted.containsKey(id)) {
				if(deleted.get(id) < frame)
					deleted.remove(id);
				else
					r = true;
			}
			if(r) {
				return(null);
			} else {
				Gob g = new Gob(glob, Coord.z, id, frame);
				objs.put(id, g);
				return(g);
			}
		} else {
			return(objs.get(id));
		}
		/* XXX: Clean up in deleted */
	}
	
	public synchronized void move(int id, int frame, Coord c) {
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		g.move(c);
	}
	
	public synchronized void cres(int id, int frame, String res, int ver) {
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		Resource rres = Resource.load(res, ver);
		ResDrawable d = (ResDrawable)g.getattr(Drawable.class);
		if((d == null) || (d.res != rres)) {
			g.setattr(new ResDrawable(g, rres));
		}
	}
	
	public synchronized void linbeg(int id, int frame, Coord s, Coord t, int c) {
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		LinMove lm = new LinMove(g, s, t, c);
		g.setattr(lm);
	}
	
	public synchronized void linstep(int id, int frame, int l) {
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		Moving m = g.getattr(Moving.class);
		if((m == null) || !(m instanceof LinMove))
			return;
		LinMove lm = (LinMove)m;
		if((l < 0) || (l >= lm.c))
			g.delattr(Moving.class);
		else
			lm.setl(l);
	}
	
	public synchronized void speak(int id, int frame, Coord off, String text) {
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		if(text.length() < 1) {
			g.delattr(Speaking.class);
		} else {
			Speaking m = g.getattr(Speaking.class);
			if(m == null) {
				g.setattr(new Speaking(g, off, text));
			} else {
				m.off = off;
				m.update(text);
			}
		}
	}
	
	public synchronized void layers(int id, int frame, String baseres, int basever, List<String> layers, List<Integer> vers) {
		Resource base = Resource.load(baseres, basever);
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		 Layered lay = (Layered)g.getattr(Drawable.class);
		 if((lay == null) || (lay.base != base)) {
			 lay = new Layered(g, base);
			 g.setattr(lay);
		 }
		 List<Resource> ll = new ArrayList<Resource>();
		 for(int i = 0; i < layers.size(); i++)
			 ll.add(Resource.load(layers.get(i), vers.get(i)));
		 lay.setlayers(ll);
	}
	
	public synchronized void avatar(int id, int frame, List<String> layers, List<Integer> vers) {
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		 Avatar ava = g.getattr(Avatar.class);
		 if(ava == null) {
			 ava = new Avatar(g);
			 g.setattr(ava);
		 }
		 List<Resource> ll = new ArrayList<Resource>();
		 for(int i = 0; i < layers.size(); i++)
			 ll.add(Resource.load(layers.get(i), vers.get(i)));
		 ava.setlayers(ll);
	}
	
	public synchronized void drawoff(int id, int frame, Coord off) {
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		if((off.x == 0) && (off.y == 0)) {
			g.delattr(DrawOffset.class);
		} else {
			DrawOffset dro = g.getattr(DrawOffset.class);
			if(dro == null) {
				dro = new DrawOffset(g, off);
				g.setattr(dro);
			} else {
				dro.off = off;
			}
		}
	}
	
	public synchronized void lumin(int id, int frame, Coord off, int sz, int str) {
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		g.setattr(new Lumin(g, off, sz, str));
	}
}
