package haven;

import java.awt.Graphics;

public class SimpleDrawable extends Drawable {
	Sprite spr;
	
	public SimpleDrawable(Gob gob, String res) {
		super(gob, res);
		this.res = res;
		spr = Resource.loadsprite(res);
	}
	
	public Coord getoffset() {
		return(spr.cc);
	}
	
	public boolean checkhit(Coord c) {
		int cl = spr.img.getRGB(c.x, c.y);
		return(Utils.rgbm.getAlpha(cl) >= 128);
	}
	
	public Coord getsize() {
		return(Utils.imgsz(spr.img));
	}
	
	public void draw(Graphics g, Coord sc) {
		g.drawImage(spr.img, sc.x - spr.cc.x, sc.y - spr.cc.y, null);
	}
}
