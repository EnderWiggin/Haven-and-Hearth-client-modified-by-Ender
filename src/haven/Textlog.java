package haven;

import java.awt.Graphics;
import java.util.*;
import java.awt.Color;
import java.awt.FontMetrics;

public class Textlog extends SSWidget {
	List<String> lines;
	int lastline;
	
	static {
		Widget.addtype("log", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Textlog(c, (Coord)args[0], parent));
			}
		});
	}
	
	void render() {
		Graphics g = graphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, sz.x, sz.y);
		g.setColor(Color.WHITE);
		Utils.AA(g);
		FontMetrics m = g.getFontMetrics();
		int y = sz.y - m.getHeight();
		int l = lastline;
		while((y > 0) && (l < lines.size()) && (l >= 0)) {
			Utils.drawtext(g, lines.get(l), new Coord(0, y));
			y -= m.getHeight();
			l--;
		}
	}
	
	public Textlog(Coord c, Coord sz, Widget parent) {
		super(c, sz, parent, false);
		lines = new LinkedList<String>();
		lastline = -0;
		render();
	}
	
	public void append(String line) {
		lines.add((String)line);
		if(lastline == lines.size() - 2)
			lastline = lines.size() - 1;
		render();
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "apnd") {
			append((String)args[0]);
		}
	}
}
