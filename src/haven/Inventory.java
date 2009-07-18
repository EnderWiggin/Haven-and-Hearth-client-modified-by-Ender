package haven;

public class Inventory extends Widget implements DTarget {
    public static Tex invsq = Resource.loadtex("gfx/hud/invsq");
    Coord isz;

    static {
	Widget.addtype("inv", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Inventory(c, (Coord)args[0], parent));
		}
	    });
    }

    public void draw(GOut g) {
	Coord c = new Coord();
	Coord sz = invsq.sz().add(new Coord(-1, -1));
	for(c.y = 0; c.y < isz.y; c.y++) {
	    for(c.x = 0; c.x < isz.x; c.x++) {
		g.image(invsq, c.mul(sz));
	    }
	}
	super.draw(g);
    }
	
    public Inventory(Coord c, Coord sz, Widget parent) {
	super(c, invsq.sz().add(new Coord(-1, -1)).mul(sz).add(new Coord(1, 1)), parent);
	isz = sz;
    }
	
    public boolean drop(Coord cc, Coord ul) {
	wdgmsg("drop", ul.add(new Coord(15, 15)).div(invsq.sz()));
	return(true);
    }
	
    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "sz") {
	    isz = (Coord)args[0];
	    sz = invsq.sz().add(new Coord(-1, -1)).mul(isz).add(new Coord(1, 1));
	}
    }
}
