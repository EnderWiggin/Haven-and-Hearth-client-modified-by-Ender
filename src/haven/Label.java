package haven;

import java.awt.Graphics;
import java.awt.Color;

public class Label extends Widget {
	String text;
	Color c = Color.BLACK;
	
	public void draw(Graphics g) {
		sz = Utils.textsz(g, text);
		Utils.AA(g);
		g.setColor(c);
		Utils.drawtext(g, text, Coord.z);
	}
	
	public Label(Coord c, Widget parent, String text) {
		super(c, Coord.z, parent);
		this.text = text;
	}
	
	public void settext(String text) {
		this.text = text;
	}
	
	public void setcolor(Color color) {
		c = color;
	}
}
