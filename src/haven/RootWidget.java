package haven;

import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;

public class RootWidget extends Widget {
	Graphical backer;
	
	public RootWidget(Coord sz, Graphical backer) {
		super(null, new Coord(0, 0), sz);
		this.backer = backer;
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
	
	public GraphicsConfiguration getconf() {
		return(backer.getconf());
	}
}
