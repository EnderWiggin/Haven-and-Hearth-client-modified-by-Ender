package haven;

public class HWindow extends Widget {
    public String title;
    SlenHud shp;
	
    static {
	Widget.addtype("hwnd", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new HWindow(parent, (String)args[0]));
		}
	    });
    }
	
    public HWindow(Widget parent, String title) {
	super(new Coord(234, 29), new Coord(430, 100), parent);
	this.title = title;
	shp = (SlenHud)parent;
	shp.addwnd(this);
    }
	
    public void destroy() {
	super.destroy();
	shp.remwnd(this);
    }
}
