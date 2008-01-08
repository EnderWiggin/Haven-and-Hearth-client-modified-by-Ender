package haven;

import java.util.Map;
import java.util.TreeMap;

public class OCache {
	Map<Integer, Drawable> objs = new TreeMap<Integer, Drawable>();
	Map<Integer, Integer> deleted = new TreeMap<Integer, Integer>();
	long lastctick = 0;

	public void remove(int id, int frame) {
		synchronized(objs) {
			if(objs.containsKey(id)) {
				objs.remove(id);
				deleted.put(id, frame);
			}
		}
	}
	
	public void tick() {
		synchronized(objs) {
			for(Drawable d : objs.values()) {
				d.c = d.c.add(d.v);
			}
		}
	}
	
	public void ctick() {
		long dt, now;
		
		now = System.currentTimeMillis();
		if(lastctick == 0)
			dt = 0;
		else
			dt = System.currentTimeMillis() - lastctick;
		synchronized(objs) {
			for(Drawable d : objs.values())
				d.ctick(dt);
		}
		lastctick = now; 
	}
	
	public void move(int id, int frame, Coord c, String res) {
		move(id, frame, c, new Coord(0, 0), res);
	}
	
	public void move(int id, int frame, Coord c, Coord v, String res) {
		synchronized(objs) {
			if(!objs.containsKey(id)) {
				boolean r = false;
				if(deleted.containsKey(id)) {
					int df = deleted.get(id);
					if(df < frame)
						deleted.remove(id);
					else
						r = true;
				}
				if(!r) {
					Drawable d = Drawable.load(c, res);
					objs.put(id, d);
					d.lastframe = frame;
				}
			} else {
				Drawable d = objs.get(id);
				if(d.lastframe < frame) {
					if(!d.res.equals(res)) {
						objs.remove(id);
						objs.put(id, d = Drawable.load(c, res));
					} else {
						d.move(c);
					}
					d.lastframe = frame;
				}
				/* XXX: Clean up in deleted */
			}
		}
	}
}
