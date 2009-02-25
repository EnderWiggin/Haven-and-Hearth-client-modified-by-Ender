package haven;

import java.awt.image.BufferedImage;

public class HWindow extends Widget {
    public String title;
    public IButton cbtn;
    static BufferedImage[] cbtni = new BufferedImage[] {
	Resource.loadimg("gfx/hud/cbtn"),
	Resource.loadimg("gfx/hud/cbtnd"),
	Resource.loadimg("gfx/hud/cbtnh")}; 
    SlenHud shp;
	
    static {
	Widget.addtype("hwnd", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    String t = (String)args[0];
		    boolean cl = false;
		    if(args.length > 1)
			cl = (Integer)args[1] != 0;
		    return(new HWindow(parent, t, cl));
		}
	    });
    }
	
    public HWindow(Widget parent, String title, boolean closable) {
	super(new Coord(234, 29), new Coord(430, 100), parent);
	this.title = title;
	shp = (SlenHud)parent;
	shp.addwnd(this);
	if(closable)
	    cbtn = new IButton(new Coord(sz.x - cbtni[0].getWidth(), 0), this, cbtni[0], cbtni[1], cbtni[2]);
    }
	
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == cbtn) {
	    wdgmsg("close");
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
	
    public void destroy() {
	super.destroy();
	shp.remwnd(this);
    }
}
