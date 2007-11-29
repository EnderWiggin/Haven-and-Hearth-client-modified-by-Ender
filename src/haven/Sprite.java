package haven;

import java.awt.image.BufferedImage;

public class Sprite {
	BufferedImage img;
	Coord cc;
	
	public Sprite(BufferedImage img, Coord cc) {
		this.img = img;
		this.cc = cc;
	}
}
