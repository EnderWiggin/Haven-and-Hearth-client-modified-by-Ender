package haven;

import java.awt.image.BufferedImage;

public class Sprite {
	BufferedImage img;
	Coord cc;
	int prio;
	boolean isgay;
	
	public Sprite(BufferedImage img, Coord cc, int prio) {
		this.img = img;
		this.cc = cc;
		this.prio = prio;
		this.isgay = Resource.detectgay(img);
	}
}
