package haven;

import static haven.Resource.pagina;
import haven.Resource.Pagina;

public class Tome extends Widget {
	private final static Tex bg = Resource.loadtex("gfx/hud/tome/tome");
	private Coord dragc = null;
	final static Coord ul1 = new Coord(31,6);
	final static Coord ul2 = new Coord(276, 6);
	final static Coord psz = new Coord(244, 341);

	static {
		Widget.addtype("tome", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Tome(c, parent));
			}
		});
	}

	public Tome(Coord c, Widget parent) {
		super(c, bg.sz(), parent);
		for(Resource res : ui.sess.glob.paginae) {
			Pagina p = res.layer(pagina);
			System.out.println(p.text);
		}
	}
	
	public void draw(GOut g) {
		g.image(bg, Coord.z);
	}
	
	public boolean mousedown(Coord c, int button) {
		if(button == 1) {
			dragc = c.inv();
			ui.grabmouse(this);
		}
		return(true);
	}
	
	public boolean mouseup(Coord c, int button) {
		if(button == 1) {
			dragc = null;
			ui.grabmouse(null);
		}
		return(true);
	}
	
	public void mousemove(Coord c) {
		if(dragc != null)
			this.c = this.c.add(c).add(dragc);
	}
}
