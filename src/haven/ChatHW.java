package haven;

import java.awt.Color;

public class ChatHW extends HWindow {
    TextEntry in;
    Textlog out;
	
    static {
	Widget.addtype("slenchat", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    String t = (String)args[0];
		    boolean cl = false;
		    if(args.length > 1)
			cl = (Integer)args[1] != 0;
		    return(new ChatHW(parent, t, cl));
		}
	    });
    }
	
    public ChatHW(Widget parent, String title, boolean closable) {
	super(parent, title, closable);
	in = new TextEntry(new Coord(0, sz.y - 20), new Coord(sz.x, 20), this, "");
	in.canactivate = true;
	out = new Textlog(Coord.z, new Coord(sz.x, sz.y - 20), this);
	cbtn.raise();
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "log") {
	    Color col = null;
	    if(args.length > 1)
		col = (Color)args[1];
	    out.append((String)args[0], col);
	} else {
	    super.uimsg(msg, args);
	}
    }
	
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == in) {
	    if(msg == "activate") {
		wdgmsg("msg", args[0]);
		in.settext("");
		return;
	    }
	}
	super.wdgmsg(sender, msg, args);
    }
}
