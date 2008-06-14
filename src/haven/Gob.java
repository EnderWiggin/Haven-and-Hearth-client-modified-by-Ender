package haven;

import java.util.*;

public class Gob {
	public Coord rc, sc;
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
		Moving m = getattr(Moving.class);
		if(m != null)
			m.move(c);
		this.rc = c;
	}
	
	public Coord getc() {
		Moving m = getattr(Moving.class);
		if(m != null)
			return(m.getc());
		else
			return(rc);
	}
	
	private Class<? extends GAttrib> attrclass(Class<? extends GAttrib> cl) {
		while(true) {
			Class<?> p = cl.getSuperclass();
			if(p == GAttrib.class)
				return(cl);
			cl = p.asSubclass(GAttrib.class);
		}
	}

	public void setattr(GAttrib a) {
		Class<? extends GAttrib> ac = attrclass(a.getClass());
		attr.put(ac, a);
	}
	
	public <C extends GAttrib> C getattr(Class<C> c) {
		GAttrib attr = this.attr.get(attrclass(c));
		if(!c.isInstance(attr))
			return(null);
		return(c.cast(attr));
	}
	
	public void delattr(Class<? extends GAttrib> c) {
		attr.remove(attrclass(c));
	}
	
	public Coord drawoff() {
		Coord ret = Coord.z;
		DrawOffset dro = getattr(DrawOffset.class);
		if(dro != null)
			ret = ret.add(dro.off);
		Following flw = getattr(Following.class);
		if(flw != null)
			ret = ret.add(flw.doff);
		return(ret);
	}
}
