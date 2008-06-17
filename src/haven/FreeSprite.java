package haven;

import java.util.*;

public class FreeSprite extends Sprite {
	Collection<Part> parts = new LinkedList<Part>();
	
	protected FreeSprite(Gob gob, Resource res) {
		super(gob, res, 1);
	}

	public abstract class SPart extends Part {
		public SPart(int z) {
			super(z);
		}
		
		protected Coord sc() {
			return(cc.add(FreeSprite.this.cc.inv()).add(off));
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
