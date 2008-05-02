package haven;

import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;

public class RootWidget extends Widget {
	Graphical backer;
	
	public RootWidget(UI ui, Coord sz, Graphical backer) {
		super(ui, new Coord(0, 0), sz);
		this.backer = backer;
		setfocusctl(true);
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
