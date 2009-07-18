package haven;

public class Chatwindow extends Window {
    TextEntry in;
    Textlog out;
	
    static {
	Widget.addtype("chat", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Chatwindow(c, (Coord)args[0], parent));
		}
	    });
    }
	
    public Chatwindow(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent, "Chat");
	in = new TextEntry(new Coord(0, sz.y - 20), new Coord(sz.x, 20), this, "");
	in.canactivate = true;
	out = new Textlog(Coord.z, new Coord(sz.x, sz.y - 20), this);
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "log") {
	    out.append((String)args[0]);
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
