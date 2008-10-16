package haven;

import java.awt.Color;

public class IMeter extends Widget {
    static Coord off = new Coord(13, 7);
    static Coord fsz = new Coord(63, 18);
    static Coord msz = new Coord(49, 4);
    Resource bg;
    int amount;
	
    static {
	Widget.addtype("im", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    Resource bg = Resource.load((String)args[1]);
		    return(new IMeter(c, parent, (Integer)args[0], bg));
		}
	    });
    }
	
    public IMeter(Coord c, Widget parent, int amount, Resource bg) {
	super(c, fsz, parent);
	this.amount = amount;
	this.bg = bg;
    }
	
    public void draw(GOut g) {
	if(!bg.loading) {
	    Tex bg = this.bg.layer(Resource.imgc).tex();
	    g.chcolor(0, 0, 0, 255);
	    g.frect(off, msz);
	    g.chcolor();
	    g.image(bg, Coord.z);
	    int w = msz.x;
	    w = (w * amount) / 100;
	    g.chcolor(255, 0, 0, 255);
	    g.frect(off, new Coord(w, msz.y));
	}
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "set") {
	    amount = (Integer)args[0];
	} else {
	    super.uimsg(msg, args);
	}
    }
}
