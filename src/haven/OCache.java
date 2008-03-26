package haven;

import java.util.*;

public class OCache implements Iterable<Gob> {
	/* XXX: Use weak refs */
	private Collection<Collection<Gob>> local = new LinkedList<Collection<Gob>>();
	private Map<Integer, Gob> objs = new TreeMap<Integer, Gob>();
	private Map<Integer, Integer> deleted = new TreeMap<Integer, Integer>();
	long lastctick = 0;

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
				Gob g = new Gob(Coord.z, id, frame);
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
	
	static SimpleDrawable loaddrw(Gob g, int type, String name) {
		if(type == 0)
			return(new SimpleSprite(g, name));
		else if(type == 2)
			return(new SimpleAnim(g, name));
		throw(new RuntimeException("Unknown resource type: " + type));
	}
	
	public synchronized void cres(int id, int frame, int type, String res) {
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		SimpleDrawable d = (SimpleDrawable)g.getattr(Drawable.class);
		if((d == null) || !d.res.equals(res)) {
			g.setattr(loaddrw(g, type, res));
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
		if(l > lm.l)
			lm.l = l;
		if((l < 0) || (l >= lm.c))
			g.delattr(Moving.class);
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
	
	public synchronized void layers(int id, int frame, List<Integer> types, List<String> layers) {
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		 Layered lay = (Layered)g.getattr(Drawable.class);
		 if(lay == null) {
			 lay = new Layered(g);
			 g.setattr(lay);
		 }
		 List<SimpleDrawable> ll = new ArrayList<SimpleDrawable>();
		 for(int i = 0; i < types.size(); i++)
			 ll.add(loaddrw(g, types.get(i), layers.get(i)));
		 lay.setlayers(ll);
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
}
