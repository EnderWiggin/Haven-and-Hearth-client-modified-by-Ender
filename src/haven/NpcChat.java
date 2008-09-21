package haven;

import java.util.*;
import java.awt.Color;

public class NpcChat extends Window {
	Textlog out;
        List<Button> btns = null;
	
	static {
		Widget.addtype("npc", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new NpcChat(c, (Coord)args[0], parent, (String)args[1]));
			}
		});
	}
	
	public NpcChat(Coord c, Coord sz, Widget parent, String title) {
		super(c, sz, parent, title);
		out = new Textlog(Coord.z, new Coord(sz.x, sz.y), this);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "log") {
                        Color col = null;
                        if(args.length > 1)
                                col = (Color)args[1];
			out.append((String)args[0], col);
                } else if(msg == "btns") {
                        if(btns != null) {
                                for(Button b : btns)
                                        ui.destroy(b);
                                btns = null;
                        }
                        if(args.length > 0) {
                            int y = out.sz.y + 3;
                            btns = new LinkedList<Button>();
                            for(Object text : args) {
                                    Button b = Button.wrapped(new Coord(0, y), out.sz.x, this, (String)text);
                                    btns.add(b);
                                    y += b.sz.y + 3;
                            }
                        }
                        pack();
		} else {
			super.uimsg(msg, args);
		}
	}
	
	public void wdgmsg(Widget sender, String msg, Object... args) {
                if((btns != null) && (btns.contains(sender))) {
                    wdgmsg("btn", btns.indexOf(sender));
                    return;
                }
		super.wdgmsg(sender, msg, args);
	}
}
