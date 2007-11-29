package haven;

import java.awt.GraphicsConfiguration;

public class RootWidget extends Widget {
	public RootWidget(Coord sz, GraphicsConfiguration gc) {
		super(null, new Coord(0, 0), sz);
		this.gc = gc;
	}
	
	public void setui(UI ui) {
		this.ui = ui;
	}
}
