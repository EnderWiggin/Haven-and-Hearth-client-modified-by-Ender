package haven;

import java.awt.Graphics;
import java.awt.Color;

public class Button extends SSWidget {
	String text;
	boolean a = false;
	
	static {
		Widget.addtype("btn", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Button(c, (Coord)args[0], parent, (String)args[1]));
			}
		});
	}
	
	public Button(Coord c, Coord sz, Widget parent, String text) {
		super(c, sz, parent, false);
		this.text = text;
		render();
	}
	
	public void render() {
		Graphics g = surf.getGraphics();
		Utils.AA(g);
		g.setColor(FlowerMenu.pink);
		g.fillRect(1, 1, sz.x - 2, sz.y - 2);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, sz.x - 1, sz.y - 1);
		Utils.centertext(g, text, sz.div(new Coord(2, 2)));
	}
	
	public boolean mousedown(Coord c, int button) {
		if(button != 1)
			return(false);
		a = true;
		ui.grabmouse(this);
		return(true);
	}
	
	public boolean mouseup(Coord c, int button) {
		if(a && button == 1) {
			a = false;
			ui.grabmouse(null);
			if(c.isect(new Coord(0, 0), sz))
				wdgmsg("activate");
			return(true);
		}
		return(false);
	}
}
