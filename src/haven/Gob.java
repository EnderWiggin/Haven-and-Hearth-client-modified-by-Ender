package haven;

import java.util.*;

public class Gob {
	Coord rc, sc;
	int clprio = 0;
	int id, frame;
	Map<Class<? extends GAttrib>, GAttrib> attr = new HashMap<Class<? extends GAttrib>, GAttrib>();
	
	public Gob(Coord c, int id, int frame) {
		this.rc = c;
		this.id = id;
		this.frame = frame;
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
