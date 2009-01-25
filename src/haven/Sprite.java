package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.util.*;

public abstract class Sprite {
    public final Resource res;
    public final Owner owner;
    public static List<Factory> factories = new LinkedList<Factory>();
	
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
	    return(other.subz - subz);
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
	    super(msg);
	    this.res = res;
	}
		
	public ResourceException(String msg, Throwable cause, Resource res) {
	    super(msg, cause);
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
	for(Factory f : factories) {
	    Sprite ret = f.create(owner, res, sdt);
	    if(ret != null)
		return(ret);
	}
	throw(new ResourceException("Does not know how to draw resource " + res.name, res));
    }

    public abstract boolean checkhit(Coord c);
    
    public abstract void setup(Drawer d, Coord cc, Coord off);

    public abstract boolean tick(int dt);

    public abstract Object stateid();
}
