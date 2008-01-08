package haven;

import java.awt.Graphics;

public abstract class Drawable {
	Coord c, v;
	Coord sc;
	String res;
	int clickprio;
	int lastframe, id;
	
	public Drawable(Coord c, Coord v, String res) {
		this.c = c;
		this.v = v;
		this.res = res;
		this.clickprio = 0;
	}
	
	public static Drawable load(Coord c, String res) {
		try {
			return(new SimpleDrawable(c, Coord.z, res));
		} catch(FormatException e) {}
		try {
			return(new SimpleAnim(c, Coord.z, res));
		} catch(FormatException e) {}
		throw(new FormatException("No viable format", res));
	}
	
	public void move(Coord c) {
		this.c = c;
	}
	
	public void ctick(long dt) {
	}
	
	public abstract Coord getsize();
	public abstract Coord getoffset();
	public abstract boolean checkhit(Coord c);
	public abstract void draw(Graphics g, Coord sc);
}
