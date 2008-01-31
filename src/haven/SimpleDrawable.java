package haven;

import java.awt.image.BufferedImage;

public abstract class SimpleDrawable extends Drawable {
	String res;
	
	public SimpleDrawable(Gob gob, String res) {
		super(gob);
		this.res = res;
	}
	
	public abstract void draw2(BufferedImage target, Coord sc);
}
