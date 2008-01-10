package haven;

import java.awt.*;

public class Speaking extends GAttrib {
	Coord off;
	String text;
	
	public Speaking(Gob gob, Coord off, String text) {
		super(gob);
		this.off = off;
		this.text = text;
	}
	
	public void draw(Graphics g, Coord c) {
		g.setColor(Color.WHITE);
		FontMetrics m = g.getFontMetrics();
		g.drawString(text, c.x, c.y);
	}
}
