import haven.Coord;
import haven.Label;
import haven.MCache;
import haven.MapView;
import haven.Widget;
import haven.WidgetFactory;
import haven.Window;

public class Landwindow extends Window
implements MapView.Grabber
{
    Label text;
    boolean dm = false;
    Coord sc;
    Coord c1;
    Coord c2;
    MCache.Overlay ol;
    MCache map;
    private static final String fmt = "Selected area: (%d x %d) = %d m²";

    public Landwindow(Coord c, Widget parent)
    {
	super(c, new Coord(200, 20), parent, "Land management");
	map = ui.sess.glob.map;
	ui.mainview.enol(new int[] { 0, 1, 16 });
	ui.mainview.grab(this);
	text = new Label(Coord.z, this, String.format(fmt, 0,0,0));
    }

    public void destroy() {
	ui.mainview.disol(new int[] { 0, 1, 16 });
	ui.mainview.release(this);
	if (ol != null)
	    ol.destroy();
	super.destroy();
    }

    public void mmousedown(Coord mc, int button) {
	if(button != 1){
	    throw new MapView.GrabberException();
	}
	Coord c = mc.div(MCache.tilesz);
	if (ol != null)
	    ol.destroy();
	ol = map.new Overlay(c, c, 65536);
	sc = c;
	dm = true;
	ui.grabmouse(this.ui.mainview);
    }

    public void mmouseup(Coord mc, int button) {
	dm = false;
	ui.grabmouse(null);
	if(button != 1){
	    throw new MapView.GrabberException();
	}
    }

    public void mmousemove(Coord mc) {
	if (!this.dm)
	    return;
	Coord c1 = mc.div(MCache.tilesz);
	Coord c2 = new Coord(0, 0); 
	Coord c3 = new Coord(0, 0);
	if (c1.x < this.sc.x) {
	    c2.x = c1.x;
	    c3.x = this.sc.x;
	} else {
	    c2.x = this.sc.x;
	    c3.x = c1.x;
	}
	if (c1.y < this.sc.y) {
	    c2.y = c1.y;
	    c3.y = this.sc.y;
	} else {
	    c2.y = this.sc.y;
	    c3.y = c1.y;
	}
	this.ol.update(c2, c3);
	this.c1 = c2;
	this.c2 = c3;
	
	c1.x = (c3.x - c2.x + 1);
	c1.y = (c3.y - c2.y + 1);
	int i = c1.x * c1.y;
	this.text.settext(String.format(fmt, c1.x, c1.y, i));
    }

    public void uimsg(String msg, Object... args) {
	if (msg == "reset") {
	    this.ol.destroy();
	    this.ol = null;
	    this.c1 = (this.c2 = null);
	}
    }

    public static class Maker
    implements WidgetFactory
    {
	public Widget create(Coord c, Widget parent, Object[] args)
	{
	    return new Landwindow(c, parent);
	}
    }
}