package haven;

import static haven.Resource.imgc;
import static haven.Resource.negc;
import static haven.Resource.animc;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.util.*;

public class Sprite {
    public final Resource res;
    protected Frame[] frames;
    public final Gob gob;
    int fno = 0, de = 0;
    public Coord cc, sz;
    public int loops = 0;
	
    public interface Drawer {
	public void addpart(Part p);
    }
	
    public static abstract class Part implements Comparable<Part> {
	public Coord poff = Coord.z;
	Coord cc, off;
	int z, subz;
		
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
		
	public abstract void draw(BufferedImage buf, Graphics g);
	public abstract void draw(GOut g);
    }
	
    protected abstract class SpritePart extends Part {
	public abstract boolean checkhit(Coord c);
		
	protected Coord sc() {
	    return(cc.add(Sprite.this.cc.inv()).add(off).add(poff));
	}
		
	public SpritePart(int z) {
	    super(z);
	}

	public SpritePart(int z, int subz) {
	    super(z, subz);
	}
    }
	
    private class ImagePart extends SpritePart {
	Resource.Image img;
		
	public ImagePart(Resource.Image img) {
	    super(img.z, img.subz);
	    this.img = img;
	}
		
	public void draw(BufferedImage b, Graphics g) {
	    Coord sc = sc().add(img.o);
	    if(img.gayp()) {
		Utils.drawgay(b, img.img, sc);
	    } else {
		g.drawImage(img.img, sc.x, sc.y, null);
	    }
	}
		
	public void draw(GOut g) {
	    g.image(img.tex(), sc().add(img.o));
	}
		
	public boolean checkhit(Coord c) {
	    if((c.x < img.o.x) || (c.y < img.o.y) || (c.x >= img.o.x + img.sz.x) || (c.y >= img.o.y + img.sz.y))
		return(false);
	    int cl = img.img.getRGB(c.x - img.o.x, c.y - img.o.y);
	    return(Utils.rgbm.getAlpha(cl) >= 128);
	}
    }

    private class TexPart extends SpritePart {
	Tex img;
	Coord doff;
	Coord mcc;
		
	public TexPart(Tex img, int z, Coord doff, Coord mcc) {
	    super(z);
	    this.img = img;
	    this.doff = doff;
	    this.mcc = mcc;
	}
		
	public void draw(BufferedImage b, Graphics g) {
	}
		
	public void draw(GOut g) {
	    Coord sc = cc.add(mcc.inv()).add(off);
	    g.image(img, sc);
	}
		
	public boolean checkhit(Coord c) {
	    if(!(this.img instanceof TexI))
		return(false);
	    c = c.add(Sprite.this.cc.inv()).add(doff.inv()).add(mcc);
	    TexI img = (TexI)this.img;
	    if((c.x < 0) || (c.y < 0) || (c.x >= img.sz().x) || (c.y >= img.sz().y))
		return(false);
	    int cl = img.getRGB(c);
	    return(Utils.rgbm.getAlpha(cl) >= 128);
	}
    }

    private class J2dPart extends SpritePart {
	BufferedImage img;
	Tex tex;
	Coord sz;
		
	public J2dPart(BufferedImage img, int z) {
	    super(z);
	    this.img = img;
	    this.tex = new TexI(img);
	    this.sz = Utils.imgsz(img);
	}
		
	public void draw(BufferedImage b, Graphics g) {
	    g.drawImage(img, sc().x, sc().y, null);
	}
		
	public void draw(GOut g) {
	    g.image(tex, sc());
	}
		
	public boolean checkhit(Coord c) {
	    if((c.x < 0) || (c.y < 0) || (c.x >= sz.x) || (c.y >= sz.y))
		return(false);
	    int cl = img.getRGB(c.x, c.y);
	    return(Utils.rgbm.getAlpha(cl) >= 128);
	}
    }

    protected class Frame {
	Collection<SpritePart> parts = new LinkedList<SpritePart>();
	int dur = 1000;
	protected Object id = new Object();
		
	public Frame() {}
		
	public Part add(Resource.Image img) {
	    SpritePart r = new ImagePart(img);
	    parts.add(r);
	    return(r);
	}
		
	public Part add(BufferedImage img, int z) {
	    SpritePart r = new J2dPart(img, z);
	    parts.add(r);
	    return(r);
	}
		
	public Part add(Tex img, int z, Coord off, Coord mcc) {
	    SpritePart r = new TexPart(img, z, off, mcc);
	    parts.add(r);
	    return(r);
	}
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

    private Sprite(Gob gob, Resource res, Resource neg, int frames) {
	this.res = res;
	this.frames = new Frame[frames];
	this.gob = gob;
	initneg(neg);
    }

    protected Sprite(Gob gob, Resource res, int frames) {
	this(gob, res, res, frames);
    }
	
    private static Sprite mksprite(Gob gob, Resource res, Resource neg, boolean layered) {
	Sprite spr = new Sprite(gob, res, neg, 1);
	Frame f = spr.new Frame();
	f.id = (res.name + ":" + neg.name).intern();
	for(Resource.Image img : res.layers(imgc)) {
	    if(img.l == layered)
		f.add(img);
	}
	spr.frames[0] = f;
	return(spr);
    }

    private static Sprite mkanim(Gob gob, Resource res, Resource neg, boolean layered, Resource.Anim ad) {
	Sprite spr = new Sprite(gob, res, neg, ad.f.length);
	for(int i = 0; i < ad.f.length; i++) {
	    Frame f = spr.new Frame();
	    f.id = (res.name + ":" + neg.name + ":" + i).intern();
	    f.dur = ad.d;
	    for(int o = 0; o < ad.f[i].length; o++) {
		if(ad.f[i][o].l == layered)
		    f.add(ad.f[i][o]);
	    }
	    spr.frames[i] = f;
	}
	return(spr);
    }

    private static Sprite mkdyn(Gob gob, Resource res, Resource neg, Resource.CodeEntry sc, Message sdt) {
	try {
	    try {
		Method m = sc.spr.getDeclaredMethod("create", Gob.class, Resource.class);
		return((Sprite)m.invoke(null, gob, res));
	    } catch(NoSuchMethodException e) {}
	    try {
		Method m = sc.spr.getDeclaredMethod("create", Gob.class, Resource.class, Message.class);
		return((Sprite)m.invoke(null, gob, res, sdt));
	    } catch(NoSuchMethodException e) {}
	    try {
		Constructor<? extends Sprite> m = sc.spr.getConstructor(Gob.class, Resource.class);
		return(m.newInstance(gob, res));
	    } catch(NoSuchMethodException e) {}
	    try {
		Constructor<? extends Sprite> m = sc.spr.getConstructor(Gob.class, Resource.class, Message.class);
		return(m.newInstance(gob, res, sdt));
	    } catch(NoSuchMethodException e) {}
	    throw(new ResourceException("Cannot call sprite code of dynamic resource", res));
	} catch(IllegalAccessException e) {
	    throw(new ResourceException("Cannot call sprite code of dynamic resource", res));
	} catch(java.lang.reflect.InvocationTargetException e) {
	    throw(new ResourceException("Sprite code of dynamic resource threw an exception", e.getCause(), res));
	} catch(InstantiationException e) {
	    throw(new ResourceException("Cannot call sprite code of dynamic resource", e.getCause(), res));
	}
    }

    private static Sprite create(Gob gob,Resource res, Resource neg, boolean layered, Message sdt) {
	Resource.Anim ad = res.layer(animc);
	Resource.CodeEntry sc = res.layer(Resource.CodeEntry.class);
	if((sc != null) && (sc.spr != null))
	    return(mkdyn(gob, res, neg, sc, sdt));
	else if(ad != null)
	    return(mkanim(gob, res, neg, layered, ad));
	else
	    return(mksprite(gob, res, neg, layered));
    }

    public static Sprite create(Gob gob, Resource res, Resource neg, Message sdt) {
	if(res.loading || neg.loading)
	    throw(new RuntimeException("Attempted to create sprite on still loading resource"));
	return(create(gob, res, neg, true, sdt));
    }
	
    public static Sprite create(Gob gob, Resource res, Message sdt) {
	if(res.loading)
	    throw(new RuntimeException("Attempted to create sprite on still loading resource"));
	return(create(gob, res, res, false, sdt));
    }

    private void initneg(Resource negres) {
	Resource.Neg neg = negres.layer(Resource.negc);
	if(neg != null) {
	    cc = neg.cc;
	    sz = neg.sz;
	    return;
	}
	throw(new ResourceException("Does not know how to draw resource " + negres.name, negres));
    }
	
    public boolean checkhit(Coord c) {
	Frame f = frames[fno];
	synchronized(f.parts) {
	    for(SpritePart p : f.parts) {
		if(p.checkhit(c))
		    return(true);
	    }
	}
	return(false);
    }
	
    public void setup(Drawer d, Coord cc, Coord off) {
	Frame f = frames[fno];
	synchronized(f.parts) {
	    for(Part p : f.parts) {
		p.cc = cc;
		p.off = off;
		if(p instanceof TexPart)
		    p.cc = p.cc.add(((TexPart)p).doff);
		d.addpart(p);
	    }
	}
    }
	
    public void tick(int dt) {
	de += dt;
	while(de > frames[fno].dur) {
	    de -= frames[fno].dur;
	    if(++fno >= frames.length) {
		fno = 0;
		loops++;
	    }
	}
    }
	
    public Object stateid() {
	return(frames[fno].id);
    }
}
