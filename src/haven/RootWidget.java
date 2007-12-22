package haven;

import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;

public class RootWidget extends Widget {
	public RootWidget(Coord sz, GraphicsConfiguration gc) {
		super(null, new Coord(0, 0), sz);
		this.gc = gc;
	}
	
	public void setui(UI ui) {
		this.ui = ui;
	}
	
	public boolean type(char key, KeyEvent ev) {
		if(!super.type(key, ev)) {
			wdgmsg("gk", (int)key);
		}
		return(true);
	}
}
