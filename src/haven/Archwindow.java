package haven;

import java.util.*;

public class Archwindow extends Window implements MapView.Grabber {
	Collection<Gob> vob = new LinkedList<Gob>();
	Coord sc;
	
	static {
		Widget.addtype("arch", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Archwindow(c, parent));
			}
		});
	}
	
	public Archwindow(Coord c, Widget parent) {
		super(c, new Coord(150, 100), parent);
		Session.current.mapdispatch.grab(this);
		Session.current.oc.ladd(vob);
	}
	
	public void destroy() {
		Session.current.mapdispatch.release(this);
		Session.current.oc.lrem(vob);
	}
	
	void makevob(Coord mc) {
		vob.clear();
		Coord wc = sc.mul(MapView.tilesz);
		if(Math.abs(wc.x - mc.x) > Math.abs(wc.y - mc.y)) {
			 Coord ec = mc.div(MapView.tilesz);
			 ec.y = sc.y;
			 int s;
			 if(ec.x < sc.x)
				 s = -1;
			 else
				 s = 1;
			 wc = sc.add(Coord.z);
			 while(true) {
				 Gob g = new Gob(wc.mul(MapView.tilesz), 0, 0);
				 g.setattr(new SimpleSprite(g, "gfx/arch/walls/wood-we.spr"));
				 vob.add(g);
				 if(wc.x == ec.x)
					 break;
				 wc.x += s;
			 }
		} else {
			 Coord ec = mc.div(MapView.tilesz);
			 ec.x = sc.x;
			 int s;
			 if(ec.y < sc.y)
				 s = -1;
			 else
				 s = 1;
			 wc = sc.add(Coord.z);
			 while(true) {
				 Gob g = new Gob(wc.mul(MapView.tilesz), 0, 0);
				 g.setattr(new SimpleSprite(g, "gfx/arch/walls/wood-ns.spr"));
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
		sc = c.div(MapView.tilesz);
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
