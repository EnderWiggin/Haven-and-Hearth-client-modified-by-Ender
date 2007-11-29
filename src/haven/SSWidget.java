package haven;

import java.awt.image.BufferedImage;
import java.awt.Transparency;
import java.awt.Graphics;

public class SSWidget extends Widget {
	BufferedImage surf;
	
	public SSWidget(Coord c, Coord sz, Widget parent) {
		super(c, sz, parent);
		surf = gc.createCompatibleImage(sz.x, sz.y, Transparency.TRANSLUCENT);
	}
	
	public void draw(Graphics g) {
		g.drawImage(surf, 0, 0, null);
	}
}
