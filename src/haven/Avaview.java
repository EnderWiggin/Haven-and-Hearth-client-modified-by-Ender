package haven;

import java.awt.Color;

public class Avaview extends Widget {
	public static final Coord dasz = new Coord(74, 74);
	private Coord asz;
	int avagob;
	public Color color = Color.WHITE;
        public static final Coord unborder = new Coord(2, 2);
        public static final Tex missing = Resource.loadtex("gfx/hud/equip/missing");
	
	static {
		Widget.addtype("av", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Avaview(c, parent, (Integer)args[0]));
			}
		});
	}
	
	public Avaview(Coord c, Widget parent, int avagob, Coord asz) {
		super(c, asz.add(Window.wbox.bisz()).add(unborder.mul(2).inv()), parent);
		this.avagob = avagob;
		this.asz = asz;
	}
	
	public Avaview(Coord c, Widget parent, int avagob) {
		this(c, parent, avagob, dasz);
	}
	
	public void draw(GOut g) {
		Gob gob = ui.sess.glob.oc.getgob(avagob);
                Avatar ava = null;
		if(gob != null)
        		ava = gob.getattr(Avatar.class);
                GOut g2 = g.reclip(Window.wbox.tloff().add(unborder.inv()), asz);
                Tex at;
                int yo;
		if(ava == null) {
                        at = missing;
                        yo = 0;
                } else {
                        g2.image(Equipory.bg, new Coord(Equipory.bg.sz().x / 2 - asz.x / 2, 20).inv());
                        at = ava.tex();
                        yo = (20 * asz.y) / dasz.y;
                }
                Coord tsz = new Coord((at.sz().x * asz.x) / dasz.x, (at.sz().y * asz.y) / dasz.y);
                g2.image(at, new Coord(tsz.x / 2 - asz.x / 2, yo).inv(), tsz);
		g.chcolor(color);
		Window.wbox.draw(g, Coord.z, asz.add(Window.wbox.bisz()).add(unborder.mul(2).inv()));
	}
	
	public boolean mousedown(Coord c, int button) {
		wdgmsg("click", button);
		return(true);
	}
}
