package haven;

import java.awt.Graphics;

public class SimpleDrawable extends Drawable {
	String res;
	Sprite spr;
	
	public SimpleDrawable(Coord c, Coord v, String res) {
		super(c, v);
		this.res = res;
		spr = Resource.loadsprite(res);
	}
	
	public Coord getoffset() {
		return(spr.cc);
	}
	
	public void move(Coord c, Coord v, String resname) {
		if(!res.equals(resname)) {
			res = resname;
			spr = Resource.loadsprite(res);
		}
		this.c = c;
		this.v = v;
	}
	
	public boolean checkhit(Coord c) {
		int cl = spr.img.getRGB(c.x, c.y);
		return(Utils.rgbm.getAlpha(cl) >= 128);
	}
	
	public Coord getsize() {
		return(new Coord(spr.img.getWidth(), spr.img.getHeight()));
	}
	
	public void draw(Graphics g, Coord sc) {
		g.drawImage(spr.img, sc.x - spr.cc.x, sc.y - spr.cc.y, null);
	}
}
