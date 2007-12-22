package haven;

import java.awt.image.BufferedImage;
import java.awt.Transparency;
import java.awt.Graphics;

public class SSWidget extends Widget {
	BufferedImage surf;
	private boolean t;
	
	public SSWidget(Coord c, Coord sz, Widget parent, boolean t) {
		super(c, sz, parent);
		this.t = t;
		clear();
	}
	
	public void draw(Graphics g) {
		g.drawImage(surf, 0, 0, null);
	}
	
	public void clear() {
		surf = gc.createCompatibleImage(sz.x, sz.y, t?Transparency.TRANSLUCENT:Transparency.BITMASK);	
	}
}
