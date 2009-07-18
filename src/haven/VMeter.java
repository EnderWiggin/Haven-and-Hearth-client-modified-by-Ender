package haven;

import java.awt.Color;

public class VMeter extends Widget {
    static Tex bg = Resource.loadtex("gfx/hud/vm-frame");
    static Tex fg = Resource.loadtex("gfx/hud/vm-tex");
    Color cl;
    int amount;
	
    static {
	Widget.addtype("vm", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    Color cl;
		    if(args.length > 4) {
			cl = new Color((Integer)args[1],
				       (Integer)args[2],
				       (Integer)args[3],
				       (Integer)args[4]);
		    } else {
			cl = new Color((Integer)args[1],
				       (Integer)args[2],
				       (Integer)args[3]);
		    }
		    return(new VMeter(c, parent, (Integer)args[0], cl));
		}
	    });
    }
	
    public VMeter(Coord c, Widget parent, int amount, Color cl) {
	super(c, bg.sz(), parent);
	this.amount = amount;
	this.cl = cl;
    }
	
    public void draw(GOut g) {
	g.image(bg, Coord.z);
	g.chcolor(cl);
	int h = (sz.y - 6);
	h = (h * amount) / 100;
	g.image(fg, new Coord(0, 0), new Coord(0, sz.y - 3 - h), sz.add(0, h));
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "set") {
	    amount = (Integer)args[0];
	} else {
	    super.uimsg(msg, args);
	}
    }
}
