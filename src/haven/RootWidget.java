package haven;

import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;

public class RootWidget extends Widget {
	Graphical backer;
	Logout logout = null;
	Profile gprof;
	boolean afk = false;
	
	public RootWidget(UI ui, Coord sz, Graphical backer) {
		super(ui, new Coord(0, 0), sz);
		this.backer = backer;
		setfocusctl(true);
		cursor = Resource.load("gfx/hud/curs/arw");
	}
	
	public boolean globtype(char key, KeyEvent ev) {
		if(!super.globtype(key, ev)) {
			/*
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
			} else */
			if(key == '`') {
			    new Profwnd(findchild(SlenHud.class), findchild(MapView.class).prof, "MV prof");
			} else if(key == '~') {
			    new Profwnd(findchild(SlenHud.class), gprof, "Glob prof");
			} else if(key != 0) {
				wdgmsg("gk", (int)key);
			}
		}
		return(true);
	}

	public GraphicsConfiguration getconf() {
		return(backer.getconf());
	}
	
	public void draw(GOut g) {
		super.draw(g);
		if(!afk && (System.currentTimeMillis() - ui.lastevent > 300000)) {
			afk = true;
			Widget slen = findchild(SlenHud.class);
			if(slen != null)
				slen.wdgmsg("afk");
		} else if(afk && (System.currentTimeMillis() - ui.lastevent < 300000)) {
			afk = false;
		}
	}
}
