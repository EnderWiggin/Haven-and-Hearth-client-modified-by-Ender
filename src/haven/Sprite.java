package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.util.*;
import java.lang.reflect.Constructor;

public abstract class Sprite {
    public final Resource res;
    public final Owner owner;
    public static List<Factory> factories = new LinkedList<Factory>();
    static {
	factories.add(AnimSprite.fact);
	factories.add(StaticSprite.fact);
    }
	
    public interface Drawer {
	public void addpart(Part p);
    }
    
    public interface Owner {
	public Random mkrandoom();
	public Resource.Neg getneg();
    }
    
    public interface Factory {
	public Sprite create(Owner owner, Resource res, Message sdt);
    }
    
    public static class DynFactory implements Factory {
	private final Class<? extends Sprite> cl;
	
	public DynFactory(Class<? extends Sprite> cl) {
	    this.cl = cl;
	}
	
	public Sprite create(Owner owner, Resource res, Message sdt) {
	    try {
		try {
		    Constructor<? extends Sprite> m = cl.getConstructor(Owner.class, Resource.class);
		    return(m.newInstance(owner, res));
		} catch(NoSuchMethodException e) {}
		try {
		    Constructor<? extends Sprite> m = cl.getConstructor(Owner.class, Resource.class, Message.class);
		    return(m.newInstance(owner, res, sdt));
		} catch(NoSuchMethodException e) {}
		throw(new ResourceException("Cannot call sprite code of dynamic resource", res));
	    } catch(IllegalAccessException e) {
		throw(new ResourceException("Cannot call sprite code of dynamic resource", e, res));
	    } catch(java.lang.reflect.InvocationTargetException e) {
		throw(new ResourceException("Sprite code of dynamic resource threw an exception", e.getCause(), res));
	    } catch(InstantiationException e) {
		throw(new ResourceException("Cannot call sprite code of dynamic resource", e, res));
	    }
	}
    }
	
    public static abstract class Part implements Comparable<Part> {
	public Coord cc, off;
	public Coord ul = Coord.z, lr = Coord.z;
	public int z, subz;
	
	public Part(int z) {
	    this.z = z;
	    this.subz = 0;
	}
	
	public Part(int z, int subz) {
	    this.z = z;
	    this.subz = subz;
	}
	
	public int compareTo(Part other) {
	    if(z != other.z)
		return(z - other.z);
	    if(cc.y != other.cc.y)
		return(cc.y - other.cc.y);
	    return(subz - other.subz);
	}
	
	public Coord sc() {
	    return(cc.add(off));
	}
	
	public void setup(Coord cc, Coord off) {
	    ul = lr = this.cc = cc;
	    this.off = off;
	}
	
	public boolean checkhit(Coord c) {
	    return(false);
	}

	public abstract void draw(BufferedImage buf, Graphics g);
	public abstract void draw(GOut g);
    }

    public static class ResourceException extends RuntimeException {
	public Resource res;
		
	public ResourceException(String msg, Resource res) {
	    super(msg + " (" + res + ")");
	    this.res = res;
	}
		
	public ResourceException(String msg, Throwable cause, Resource res) {
	    super(msg + " (" + res + ")", cause);
	    this.res = res;
	}
    }

    protected Sprite(Owner owner, Resource res) {
	this.res = res;
	this.owner = owner;
    }

    public static Sprite create(Owner owner, Resource res, Message sdt) {
	if(res.loading)
	    throw(new RuntimeException("Attempted to create sprite on still loading resource"));
	Resource.CodeEntry e = res.layer(Resource.CodeEntry.class);
	if(e != null) {
	    try {
		return(e.spr().create(owner, res, sdt));
	    } catch(RuntimeException exc) {
		throw(new ResourceException("Error in sprite creation routine for " + res, exc, res));
	    }
	}
	for(Factory f : factories) {
	    Sprite ret = f.create(owner, res, sdt);
	    if(ret != null)
		return(ret);
	}
	throw(new ResourceException("Does not know how to draw resource " + res.name, res));
    }

    public abstract boolean checkhit(Coord c);
    
    public abstract void setup(Drawer d, Coord cc, Coord off);

    public boolean tick(int dt) {
	return(false);
    }

    public abstract Object stateid();
    
    public static void setup(Collection<Part> parts, Drawer d, Coord cc, Coord off) {
	for(Part p : parts) {
	    p.setup(cc, off);
	    d.addpart(p);
	}
    }
}
