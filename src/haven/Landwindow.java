package haven;

import java.awt.image.BufferedImage;
import java.awt.Color;

public class Landwindow extends Window implements MapView.Grabber {
	Widget btn;
	Label text;
	boolean dm = false;
	Coord sc, c1, c2;
	MCache.Overlay ol;
	MCache map;
	private static final String fmt = "Selected area: %d m" + ((char)0xB2);
	
	static {
		Widget.addtype("land", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Landwindow(c, parent));
			}
		});
	}
	
	public Landwindow(Coord c, Widget parent) {
		super(c, new Coord(200, 100), parent, "Land management");
		map = ui.sess.glob.map;
		ui.mainview.enol(0x10003);
		ui.mainview.grab(this);
		btn = new Button(asz.add(-50, -30), 40, this, "Claim");
		text = new Label(Coord.z, this, String.format(fmt, 0));
	}
	
	public void destroy() {
		ui.mainview.disol(0x10003);
		ui.mainview.release(this);
		if(ol != null)
			ol.destroy();
		super.destroy();
	}
	
	public void mmousedown(Coord mc, int button) {
		Coord tc = mc.div(MCache.tilesz);
		if(ol != null)
			ol.destroy();
		ol = map.new Overlay(tc, tc, 65536);
		sc = tc;
		dm = true;
		ui.grabmouse(ui.mainview);
	}
	
	public void mmouseup(Coord mc, int button) {
		dm = false;
		ui.grabmouse(null);
	}
	
	public void mmousemove(Coord mc) {
		if(!dm)
			return;
		Coord tc = mc.div(MCache.tilesz);
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
		this.c1 = c1;
		this.c2 = c2;
		int a = (c2.x - c1.x + 1) * (c2.y - c1.y + 1);
		text.settext(String.format(fmt, a));
		if(a > 25)
			text.setcolor(Color.RED);
		else
			text.setcolor(Color.WHITE);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "reset") {
			ol.destroy();
			ol = null;
			c1 = c2 = null;
		}
	}
	
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == btn) {
			if((c1 != null) && (c2 != null))
				wdgmsg("take", c1, c2);
			return;
		}
		super.wdgmsg(sender, msg, args);
	}
}
