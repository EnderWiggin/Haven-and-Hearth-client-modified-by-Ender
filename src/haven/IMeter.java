package haven;

import java.awt.Color;
import java.util.*;

public class IMeter extends Widget {
    static Coord off = new Coord(13, 7);
    static Coord fsz = new Coord(63, 18);
    static Coord msz = new Coord(49, 4);
    Resource bg;
    List<Meter> meters;
    
    static {
	Widget.addtype("im", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    Resource bg = Resource.load((String)args[0]);
		    List<Meter> meters = new LinkedList<Meter>();
		    for(int i = 1; i < args.length; i += 2)
			meters.add(new Meter((Color)args[i], (Integer)args[i + 1]));
		    return(new IMeter(c, parent, bg, meters));
		}
	    });
    }
    
    public IMeter(Coord c, Widget parent, Resource bg, List<Meter> meters) {
	super(c, fsz, parent);
	this.bg = bg;
	this.meters = meters;
    }
    
    public static class Meter {
	Color c;
	int a;
	
	public Meter(Color c, int a) {
	    this.c = c;
	    this.a = a;
	}
    }
    
    public void draw(GOut g) {
	if(!bg.loading) {
	    Tex bg = this.bg.layer(Resource.imgc).tex();
	    g.chcolor(0, 0, 0, 255);
	    g.frect(off, msz);
	    g.chcolor();
	    g.image(bg, Coord.z);
	    for(Meter m : meters) {
		int w = msz.x;
		w = (w * m.a) / 100;
		g.chcolor(m.c);
		g.frect(off, new Coord(w, msz.y));
	    }
	}
    }
    
    public void uimsg(String msg, Object... args) {
	if(msg == "set") {
	    List<Meter> meters = new LinkedList<Meter>();
	    for(int i = 0; i < args.length; i += 2)
		meters.add(new Meter((Color)args[i], (Integer)args[i + 1]));
	    this.meters = meters;
	} else if(msg == "tt") {
	    tooltip = args[0];
	} else {
	    super.uimsg(msg, args);
	}
    }
}
