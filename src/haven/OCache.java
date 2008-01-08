package haven;

import java.util.Map;
import java.util.TreeMap;

public class OCache {
	Map<Integer, Gob> objs = new TreeMap<Integer, Gob>();
	Map<Integer, Integer> deleted = new TreeMap<Integer, Integer>();
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
	
	public synchronized void cres(int id, int frame, int type, String res) {
		Gob g = getgob(id, frame);
		if(g == null)
			return;
		Drawable d = g.getattr(Drawable.class);
		if((d == null) || !d.res.equals(res)) {
			Drawable nd;
			if(type == 0)
				nd = new SimpleDrawable(g, res);
			else if(type == 2)
				nd = new SimpleAnim(g, res);
			else
				throw(new RuntimeException("Unknown resource type: " + type));
			g.setattr(nd);
		}
	}
}
