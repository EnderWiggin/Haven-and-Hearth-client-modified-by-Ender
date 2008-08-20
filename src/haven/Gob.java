package haven;

import java.util.*;
import java.lang.reflect.*;

public class Gob {
	public Coord rc, sc;
	int clprio = 0;
	public int id, frame, initdelay = (int)(Math.random() * 3000);
	public final Glob glob;
	Map<Class<? extends GAttrib>, GAttrib> attr = new HashMap<Class<? extends GAttrib>, GAttrib>();
	Map<Integer, Indir<Resource>> olprep = new TreeMap<Integer, Indir<Resource>>();
	Collection<Sprite> ols = new LinkedList<Sprite>();
	private Map<Class<? extends GAttrib>, Set<ANotif<?>>> notifs = new HashMap<Class<? extends GAttrib>, Set<ANotif<?>>>();
	
	public Gob(Glob glob, Coord c, int id, int frame) {
		this.glob = glob;
		this.rc = c;
		this.id = id;
		this.frame = frame;
	}
	
	public Gob(Glob glob, Coord c) {
		this(glob, c, 0, 0);
	}
	
	public static interface ANotif<T extends GAttrib> {
		public void ch(T n);
	}
	
	public void ctick(int dt) {
		dt += initdelay;
		initdelay = 0;
		for(GAttrib a : attr.values())
			a.ctick(dt);
		for(Map.Entry<Integer, Indir<Resource>> e : olprep.entrySet()) {
			if(e.getValue() == null)
				continue;
			if(e.getValue().get() != null) {
				ols.add(Sprite.create(this, e.getValue().get()));
				e.setValue(null);
			}
		}
		for(Iterator<Sprite> i = ols.iterator(); i.hasNext();) {
			Sprite spr = i.next();
			spr.tick(dt);
			if(spr.loops > 0)
				i.remove();
		}
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
		Set<ANotif<?>> ns;
		synchronized(notifs) {
			ns = notifs.get(ac);
		}
		if(ns != null) {
			synchronized(ns) {
				for(ANotif<?> n : ns) {
					try {
						Method cpbardslen = n.getClass().getMethod("ch", GAttrib.class);
						cpbardslen.invoke(n, a);
					} catch(Exception e) {
						throw(new RuntimeException(e));
					}
				}
			}
		}
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
	
	public void drawsetup(Sprite.Drawer drawer, Coord dc, Coord sz) {
		Drawable d = getattr(Drawable.class);
		Coord dro = drawoff();
		Coord off = Coord.z;
		if(d != null) {
			Coord ulc = dc.add(d.getoffset().inv());
			if(dro != null) {
				ulc = ulc.add(dro);
				off = off.add(dro);
			}
			Coord lrc = ulc.add(d.getsize());
			if((lrc.x > 0) && (lrc.y > 0) && (ulc.x <= sz.x) && (ulc.y <= sz.y)) {
				for(Sprite spr : ols)
					spr.setup(drawer, dc, off);
				d.setup(drawer, dc, off);
			}
		}
	}
	
	public <T extends GAttrib> void notify(Class<T> c, ANotif<T> n) {
		Set<ANotif<?>> ns;
		synchronized(notifs) {
			ns = notifs.get(c);
			if(ns == null) {
				ns = new HashSet<ANotif<?>>();
				notifs.put(c, ns);
			}
		}
		synchronized(ns) {
			ns.add(n);
		}
	}
}
