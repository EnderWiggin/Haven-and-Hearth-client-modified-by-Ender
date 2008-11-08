package haven;

import java.util.*;

public class FreeSprite extends Sprite {
	Collection<Part> parts = new LinkedList<Part>();
	
	protected FreeSprite(Gob gob, Resource res) {
		super(gob, res, 1);
	}

	public abstract class SPart extends Part {
		public Coord doff = Coord.z;
		
		public SPart(int z) {
			super(z);
		}
		
		public SPart(int z, int subz) {
			super(z, subz);
		}
		
		protected Coord doff() {
			return(doff);
		}
		
		protected Coord sc() {
			return(cc.add(FreeSprite.this.cc.inv()).add(off).add(poff));
		}
		
		public void draw(java.awt.image.BufferedImage img, java.awt.Graphics g) {}
	}
	
	protected synchronized void add(Part p) {
		parts.add(p);
	}
	
	public boolean checkhit(Coord c) {
		return(false);
	}
	
	public synchronized void setup(Drawer d, Coord cc, Coord off) {
		for(Part p : parts) {
			p.cc = cc;
			if(p instanceof SPart)
				p.cc = p.cc.add(((SPart)p).doff());
			p.off = off;
			d.addpart(p);
		}
	}

	public void tick(int dt) {
	}
	
	public Object stateid() {
		return(this);
	}
}
