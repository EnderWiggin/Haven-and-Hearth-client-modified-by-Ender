package haven;

import java.awt.image.BufferedImage;

public class Sprite {
	BufferedImage img;
	Coord sz;
	Coord cc;
	int prio;
	boolean isgay;
	Sprite shadow;
	CPImage cp;
	
	public Sprite(BufferedImage img, Coord cc, int prio) {
		this.img = img;
		this.sz = Utils.imgsz(img);
		this.cc = cc;
		this.prio = prio;
		this.isgay = Resource.detectgay(img);
		cp = new CPImage(img, null);
	}
}
