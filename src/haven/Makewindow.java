package haven;

import java.util.*;
import java.awt.image.BufferedImage;

public class Makewindow {
	static {
		Widget.addtype("make", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				final Coord boff = new Coord(7, 9);
				final List<Listbox.Option> opts = new LinkedList<Listbox.Option>();
				for(int i = 0; i < args.length; i += 2)
					opts.add(new Listbox.Option((String)args[i], (String)args[i + 1]));
				final BufferedImage bup = Resource.loadimg("gfx/hud/btn-mk-up.gif");
				final Widget wnd = new Window(c, new Coord(300, 200), parent, new Coord(0, 0), boff) {
					Widget list, btn;
					String cr = opts.get(0).name;
					
					{
						list = new Listbox(new Coord(0, 0), new Coord(100, 200), this, opts);
						btn = new IButton(new Coord(305, 205).add(Utils.imgsz(bup).inv()).add(boff), this, bup, Resource.loadimg("gfx/hud/btn-mk-dn.gif"));							
					}

					public void wdgmsg(Widget sender, String msg, Object... args) {
						if(sender == this) {
							super.wdgmsg(sender, msg, args);
							return;
						}
						if(sender == btn) {
							if(msg == "activate")
								wdgmsg("make", cr);
						} else if(sender == list) {
							if(msg == "chose")
								cr = (String)args[0];
						}
					}
				};
				return(wnd);
			}
		});
	}
}
