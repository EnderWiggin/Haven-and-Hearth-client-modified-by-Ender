package haven;

import java.util.*;

public class Gob {
	Coord rc, sc;
	int clprio = 0;
	public int id, frame;
	public final Glob glob;
	Map<Class<? extends GAttrib>, GAttrib> attr = new HashMap<Class<? extends GAttrib>, GAttrib>();
	
	public Gob(Glob glob, Coord c, int id, int frame) {
		this.glob = glob;
		this.rc = c;
		this.id = id;
		this.frame = frame;
	}
	
	public Gob(Glob glob, Coord c) {
		this(glob, c, 0, 0);
	}
	
	public void ctick(int dt) {
		for(GAttrib a : attr.values())
			a.ctick(dt);
	}
	
	public void tick() {
		for(GAttrib a : attr.values())
			a.tick();
	}
	
	public void move(Coord c) {
		this.rc = c;
	}
	
	public Coord getc() {
		Moving m = getattr(Moving.class);
		if(m != null)
			return(m.getc());
		else
			return(rc);
	}
	
	public void setattr(GAttrib a) {
		Class<? extends GAttrib> ac = a.getClass();
		while(true) {
			Class<?> p = ac.getSuperclass();
			if(p == GAttrib.class)
				break;
			ac = p.asSubclass(GAttrib.class);
		}
		attr.put(ac, a);
	}
	
	public <C extends GAttrib> C getattr(Class<C> c) {
		return((C)attr.get(c));
	}
	
	public void delattr(Class<? extends GAttrib> c) {
		attr.remove(c);
	}
}
