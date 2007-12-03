package haven;

import java.util.Map;
import java.util.TreeMap;

public class OCache {
	Map<Integer, Drawable> objs = new TreeMap<Integer, Drawable>();
	Map<Integer, Integer> deleted = new TreeMap<Integer, Integer>();

	public void remove(int id, int frame) {
		synchronized(objs) {
			if(objs.containsKey(id)) {
				objs.remove(id);
				System.out.println(id + " " + objs.containsKey(id));
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
					Drawable d = new SimpleDrawable(c, v, res);
					objs.put(id, d);
					d.lastframe = frame;
				}
			} else {
				Drawable d = objs.get(id);
				if(d.lastframe < frame) {
					if(d instanceof SimpleDrawable) {
						SimpleDrawable sd = (SimpleDrawable)d;
						sd.move(c, v, res);
					}
				}
				/* XXX: Clean up in deleted */
			}
		}
	}
}
