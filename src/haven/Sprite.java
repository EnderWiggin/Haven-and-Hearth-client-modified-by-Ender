package haven;

import static haven.Resource.imgc;
import static haven.Resource.negc;
import static haven.Resource.animc;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.util.*;

public class Sprite {
	public final Resource res;
	protected Frame[] frames;
	public final Gob gob;
	int fno = 0, de = 0;
	Coord cc, sz;
	
	public interface Drawer {
		public void addpart(Part p);
	}
	
	public static abstract class Part implements Comparable<Part> {
		Coord cc, sc;
		int z;
		
		public Part(int z) {
			this.z = z;
		}
		
		public int compareTo(Part other) {
			if(z != other.z)
				return(z - other.z);
			return(cc.y - other.cc.y);
		}
		
		public abstract void draw(BufferedImage buf, Graphics g);
		public abstract void draw(GOut g);
	}
	
	private abstract class SpritePart extends Part {
		public abstract boolean checkhit(Coord c);
		
		public SpritePart(int z) {
			super(z);
		}
	}
	
	private class ImagePart extends SpritePart {
		Resource.Image img;
		
		public ImagePart(Resource.Image img) {
			super(img.z);
			this.img = img;
		}
		
		public void draw(BufferedImage b, Graphics g) {
			Coord sc = this.sc.add(img.o);
			if(img.gayp()) {
				Utils.drawgay(b, img.img, sc);
			} else {
				g.drawImage(img.img, sc.x, sc.y, null);
			}
		}
		
		public void draw(GOut g) {
			Coord sc = this.sc.add(img.o);
			g.image(img.tex(), sc);
		}
		
		public boolean checkhit(Coord c) {
			if((c.x < img.o.x) || (c.y < img.o.y) || (c.x >= img.o.x + img.sz.x) || (c.y >= img.o.y + img.sz.y))
				return(false);
			int cl = img.img.getRGB(c.x - img.o.x, c.y - img.o.y);
			return(Utils.rgbm.getAlpha(cl) >= 128);
		}
	}

	protected class Frame {
		Collection<SpritePart> parts = new LinkedList<SpritePart>();
		int dur = 1000;
		
		public void add(Resource.Image img) {
			parts.add(new ImagePart(img));
		}
	}
	
	public static class ResourceException extends RuntimeException {
		public Resource res;
		
		public ResourceException(String msg, Resource res) {
			super(msg);
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
			f.dur = ad.d;
			for(int o = 0; o < ad.f[i].length; o++) {
				if(ad.f[i][o].l == layered)
					f.add(ad.f[i][o]);
			}
			spr.frames[i] = f;
		}
		return(spr);
	}

	private static Sprite create(Gob gob,Resource res, Resource neg, boolean layered) {
		Resource.Anim ad = res.layer(animc);
		if(ad == null)
			return(mksprite(gob, res, neg, layered));
		else
			return(mkanim(gob, res, neg, layered, ad));
	}

	public static Sprite create(Gob gob, Resource res, Resource neg) {
		if(res.loading || neg.loading)
			throw(new RuntimeException("Attempted to create sprite on still loading resource"));
		return(create(gob, res, neg, true));
	}
	
	public static Sprite create(Gob gob, Resource res) {
		if(res.loading)
			throw(new RuntimeException("Attempted to create sprite on still loading resource"));
		return(create(gob, res, res, false));
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
		for(SpritePart p : frames[fno].parts) {
			if(p.checkhit(c))
				return(true);
		}
		return(false);
	}
	
	private void initanim(Resource.Anim ad) {
	}

	public void setup(Drawer d, Coord cc, Coord sc) {
		for(Part p : frames[fno].parts) {
			p.cc = cc;
			p.sc = sc;
			d.addpart(p);
		}
	}
	
	public void tick(int dt) {
		de += dt;
		while(de > frames[fno].dur) {
			de -= frames[fno].dur;
			fno = (fno + 1) % frames.length;
		}
	}
}
