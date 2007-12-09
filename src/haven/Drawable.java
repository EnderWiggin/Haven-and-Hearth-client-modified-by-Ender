package haven;

import java.awt.Graphics;

public abstract class Drawable {
	Coord c, v;
	Coord sc;
	int clickprio;
	int lastframe, id;
	
	public Drawable(Coord c, Coord v) {
		this.c = c;
		this.v = v;
		this.clickprio = 0;
	}
	
	public abstract Coord getsize();
	public abstract Coord getoffset();
	public abstract boolean checkhit(Coord c);
	public abstract void draw(Graphics g, Coord sc);
}
