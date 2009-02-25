package haven;

public class Logwindow extends HWindow {
    Textlog log;
	
    static {
	Widget.addtype("slenlog", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    String t = (String)args[0];
		    boolean cl = false;
		    if(args.length > 1)
			cl = (Integer)args[1] != 0;
		    return(new Logwindow(parent, t, cl));
		}
	    });
    }
	
    public Logwindow(Widget parent, String title, boolean closable) {
	super(parent, title, closable);
	log = new Textlog(Coord.z, sz, this);
    }
	
    public void uimsg(String name, Object... args) {
	if(name == "log") {
	    log.append((String)args[0]);
	} else {
	    super.uimsg(name, args);
	}
    }
}
