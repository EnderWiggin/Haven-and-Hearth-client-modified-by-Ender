package haven;

import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;

public class RootWidget extends Widget {
	Graphical backer;
	Logout logout = null;
	Profile gprof;
	
	public RootWidget(UI ui, Coord sz, Graphical backer) {
		super(ui, new Coord(0, 0), sz);
		this.backer = backer;
		setfocusctl(true);
	}
	
	public boolean globtype(char key, KeyEvent ev) {
		if(!super.globtype(key, ev)) {
			if(key == 27) {
				if(logout == null) {
					if(ui.sess != null)
						logout = new Logout(new Coord(338, 275), this) {
							public void destroy() { 
								super.destroy();
								logout = null;
							}
						};
				} else {
					ui.destroy(logout);
					logout = null;
				}
			} else if(key == '`') {
			    new Profwnd(findchild(SlenHud.class), findchild(MapView.class).prof, "MV prof");
			} else if(key == '~') {
			    new Profwnd(findchild(SlenHud.class), gprof, "Glob prof");
			} else {
				wdgmsg("gk", (int)key);
			}
		}
		return(true);
	}
	
	public GraphicsConfiguration getconf() {
		return(backer.getconf());
	}
}
