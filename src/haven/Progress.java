package haven;

public class Progress extends Widget {
    Text text;
	
    static {
	Widget.addtype("prog", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Progress(c, parent, (Integer)args[0]));
		}
	    });
    }
	
    public Progress(Coord c, Widget parent, int p) {
	super(c, new Coord(75, 20), parent);
	text = Text.renderf(FlowerMenu.pink, "%d%%", p);
    }
	
    public void draw(GOut g) {
	g.image(text.tex(), new Coord(sz.x / 2 - text.tex().sz().x / 2, 0));
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "p") {
	    text = Text.renderf(FlowerMenu.pink, "%d%%", (Integer)args[0]);
	} else {
	    super.uimsg(msg, args);
	}
    }
}
