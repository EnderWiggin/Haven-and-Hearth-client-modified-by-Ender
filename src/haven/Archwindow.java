package haven;

import static haven.MCache.tilesz;
import java.util.*;

public class Archwindow extends Window implements MapView.Grabber {
	Collection<Gob> vob = new LinkedList<Gob>();
	Coord sc;
	OCache oc;
	
	static {
		Widget.addtype("arch", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Archwindow(c, parent));
			}
		});
	}
	
	public Archwindow(Coord c, Widget parent) {
		super(c, new Coord(150, 100), parent, "Architecture");
		oc = ui.sess.glob.oc;
		ui.mainview.grab(this);
		oc.ladd(vob);
	}
	
	public void destroy() {
		ui.mainview.release(this);
		oc.lrem(vob);
	}
	
	void makevob(Coord mc) {
		vob.clear();
		Coord wc = sc.mul(tilesz);
		if(Math.abs(wc.x - mc.x) > Math.abs(wc.y - mc.y)) {
			 Coord ec = mc.div(tilesz);
			 ec.y = sc.y;
			 int s;
			 if(ec.x < sc.x)
				 s = -1;
			 else
				 s = 1;
			 wc = sc.add(Coord.z);
			 while(true) {
				 Gob g = new Gob(ui.sess.glob, wc.mul(tilesz));
				 g.setattr(new ResDrawable(g, Resource.load("gfx/arch/walls/wood-we")));
				 vob.add(g);
				 if(wc.x == ec.x)
					 break;
				 wc.x += s;
			 }
		} else {
			 Coord ec = mc.div(tilesz);
			 ec.x = sc.x;
			 int s;
			 if(ec.y < sc.y)
				 s = -1;
			 else
				 s = 1;
			 wc = sc.add(Coord.z);
			 while(true) {
				 Gob g = new Gob(ui.sess.glob, wc.mul(tilesz), 0, 0);
				 g.setattr(new ResDrawable(g, Resource.load("gfx/arch/walls/wood-ns")));
				 vob.add(g);
				 if(wc.y == ec.y)
					 break;
				 wc.y += s;
			 }
		}
	}
	
	public void mmousedown(Coord c, int button) {
		if((sc != null) || (button != 1))
			return;
		sc = c.div(tilesz);
		makevob(c);
	}
	
	public void mmouseup(Coord c, int button) {
		sc = null;
	}
	
	public void mmousemove(Coord c) {
		if(sc != null)
			makevob(c);
	}
}
