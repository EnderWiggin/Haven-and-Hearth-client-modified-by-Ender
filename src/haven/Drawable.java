package haven;

import java.awt.Graphics;

public abstract class Drawable extends GAttrib {
	String res;
	
	public Drawable(Gob gob, String res) {
		super(gob);
		this.res = res;
	}
	
	public abstract Coord getsize();
	public abstract Coord getoffset();
	public abstract boolean checkhit(Coord c);
	public abstract void draw(Graphics g, Coord sc);
}
