package haven;

public class Logwindow extends HWindow {
	Textlog log;
	
	static {
		Widget.addtype("slenlog", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Logwindow(parent, (String)args[0]));
			}
		});
	}
	
	public Logwindow(Widget parent, String title) {
		super(parent, title);
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
