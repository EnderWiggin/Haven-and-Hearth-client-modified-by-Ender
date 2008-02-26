package haven;

public class Landwindow extends Window implements MapView.Grabber {
	boolean dm = false;
	Coord sc;
	MapView.Overlay ol;
	
	static {
		Widget.addtype("land", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Landwindow(c, parent));
			}
		});
	}
	
	public Landwindow(Coord c, Widget parent) {
		super(c, new Coord(200, 100), parent);
		Session.current.mapdispatch.enol(3);
		Session.current.mapdispatch.grab(this);
	}
	
	public void destroy() {
		Session.current.mapdispatch.disol(3);
		Session.current.mapdispatch.release(this);
		if(ol != null)
			ol.destroy();
	}
	
	public void mmousedown(Coord mc, int button) {
		Coord tc = mc.div(MapView.tilesz);
		if(ol != null)
			ol.destroy();
		ol = Session.current.mapdispatch.new Overlay(tc, tc, 2);
		sc = tc;
		dm = true;
	}
	
	public void mmouseup(Coord mc, int button) {
		dm = false;
	}
	
	public void mmousemove(Coord mc) {
		if(!dm)
			return;
		Coord tc = mc.div(MapView.tilesz);
		Coord c1 = new Coord(0, 0), c2 = new Coord(0, 0);
		if(tc.x < sc.x) {
			c1.x = tc.x;
			c2.x = sc.x;
		} else {
			c1.x = sc.x;
			c2.x = tc.x;			
		}
		if(tc.y < sc.y) {
			c1.y = tc.y;
			c2.y = sc.y;
		} else {
			c1.y = sc.y;
			c2.y = tc.y;			
		}
		ol.update(c1, c2);
	}
}
