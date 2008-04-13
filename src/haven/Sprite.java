package haven;

import static haven.Resource.imgc;
import static haven.Resource.negc;
import static haven.Resource.animc;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.util.*;

public class Sprite {
	Resource res;
	Frame[] frames;
	int fno = 0, de = 0;
	boolean layered;
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
	
	private class ImagePart extends Part {
		Resource.Image img;
		
		public ImagePart(Resource.Image img) {
			super(img.z);
			this.img = img;
		}
		
		public void draw(BufferedImage b, Graphics g) {
			Coord sc = this.sc.add(img.o);
			g.drawImage(img.img, sc.x, sc.y, null);
		}
		
		public void draw(GOut g) {
			Coord sc = this.sc.add(img.o);
			g.image(img.tex(), sc);
		}
	}

	private class Frame {
		Collection<Part> parts = new LinkedList<Part>();
		int dur = 1000;
	}
	
	public static class ResourceException extends RuntimeException {
		public Resource res;
		
		public ResourceException(String msg, Resource res) {
			super(msg);
			this.res = res;
		}
	}

	public Sprite(Resource res, Resource negres, boolean layered) {
		if(res.loading)
			throw(new RuntimeException("Attempted to create sprite on still loading resource"));
		this.res = res;
		this.layered = layered;
		initneg(negres);
		Resource.Anim ad = res.layer(animc);
		if(ad == null)
			initsprite();
		else
			initanim(ad);
	}
	
	public Sprite(Resource res, boolean layered) {
		this(res, res, layered);
	}
	
	public Sprite(Resource res) {
		this(res, false);
	}
	
	private void initneg(Resource negres) {
		Resource.Neg neg = negres.layer(Resource.negc);
		if(neg != null) {
			cc = neg.cc;
			sz = neg.sz;
			return;
		}
		throw(new ResourceException("Does not know how to draw resource " + res.name, res));
	}

	private void initsprite() {
		Frame f = new Frame();
		for(Resource.Image img : res.layers(imgc)) {
			if(img.l == layered)
				f.parts.add(new ImagePart(img));
		}
		frames = new Frame[1];
		frames[0] = f;
	}
	
	private void initanim(Resource.Anim ad) {
		frames = new Frame[ad.f.length];
		for(int i = 0; i < frames.length; i++) {
			Frame f = new Frame();
			f.dur = ad.d;
			for(int o = 0; o < ad.f[i].length; o++) {
				if(ad.f[i][o].l == layered)
					f.parts.add(new ImagePart(ad.f[i][o]));
			}
			frames[i] = f;
		}
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
