/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

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
    
    public static final Comparator<Part> partcmp = new Comparator<Part>() {
	public int compare(Part a, Part b) {
	    if(a.z != b.z)
		return(a.z - b.z);
	    if(a.cc.y != b.cc.y)
		return(a.cc.y - b.cc.y);
	    return((a.subz + a.szo) - (b.subz + b.szo));
	}
    };
    
    public static final Comparator<Part> partidcmp = new Comparator<Part>() {
	private int eid = 0;
	private Map<Part, Integer> emergency = null;
		    
	public int compare(Part a, Part b) {
	    int c = partcmp.compare(a, b);
	    if(c != 0)
		return(c);
	    c = System.identityHashCode(a) - System.identityHashCode(b);
	    if(c != 0)
		return(c);
	    if(a == b)
		return(0);
	    if(emergency == null) {
		System.err.println("Could not impose ordering on distinct sprite parts, invoking emergency protocol!");
		emergency = new IdentityHashMap<Part, Integer>();
	    }
	    int ai, bi;
	    if(emergency.containsKey(a))
		ai = emergency.get(a);
	    else
		emergency.put(a, ai = eid++);
	    if(emergency.containsKey(a))
		bi = emergency.get(a);
	    else
		emergency.put(b, bi = eid++);
	    return(ai - bi);
	}
    };
    
    public interface Drawer {
	public void addpart(Part p);
    }
    
    public interface Owner {
	public Random mkrandoom();
	public Resource.Neg getneg();
    }
    
    public static class FactMaker implements Resource.PublishedCode.Instancer {
	public FactMaker() {
	}
	
	public Factory make(Class<?> cl) throws InstantiationException, IllegalAccessException {
	    if(Factory.class.isAssignableFrom(cl))
		return(cl.asSubclass(Factory.class).newInstance());
	    if(Sprite.class.isAssignableFrom(cl))
		return(new DynFactory(cl.asSubclass(Sprite.class)));
	    return(null);
	}
    }

    @Resource.PublishedCode(name = "spr", instancer = FactMaker.class)
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
	
    public static abstract class Part {
	public Coord cc, off;
	public Coord ul = Coord.z, lr = Coord.z;
	public int z, subz, szo;
	public Effect effect;
	public Owner owner;
	
	public static interface Effect {
	    public GOut apply(GOut in);
	}
	
	public Part(int z) {
	    this.z = z;
	    this.subz = 0;
	}
	
	public Part(int z, int subz) {
	    this.z = z;
	    this.subz = subz;
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
	public void drawol(GOut g) {}
    }

    public static class ResourceException extends RuntimeException {
	public Resource res;
		
	public ResourceException(String msg, Resource res) {
	    super(msg + " (" + res + ", from " + res.source + ")");
	    this.res = res;
	}
		
	public ResourceException(String msg, Throwable cause, Resource res) {
	    super(msg + " (" + res + ", from " + res.source + ")", cause);
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
		return(e.get(Factory.class).create(owner, res, sdt));
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
    
    public static void setup(Collection<? extends Part> parts, Drawer d, Coord cc, Coord off) {
	for(Part p : parts) {
	    p.setup(cc, off);
	    d.addpart(p);
	}
    }
}
