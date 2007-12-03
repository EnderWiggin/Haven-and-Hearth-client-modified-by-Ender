package haven;

import java.awt.Graphics;

public abstract class Drawable {
	Coord c, v;
	Coord sc;
	int lastframe, id;
	
	public Drawable(Coord c, Coord v) {
		this.c = c;
		this.v = v;
	}
	
	public abstract Coord getsize();
	public abstract Coord getoffset();
	public abstract void draw(Graphics g, Coord sc);
}
