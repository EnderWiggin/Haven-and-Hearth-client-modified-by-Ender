package haven;

import java.awt.Color;

public class Avaview extends Widget {
	public static final Coord dasz = new Coord(70, 70);
	private Coord asz;
	int avagob;
	public Color color = Color.WHITE;
	
	static {
		Widget.addtype("av", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Avaview(c, parent, (Integer)args[0]));
			}
		});
	}
	
	public Avaview(Coord c, Widget parent, int avagob, Coord asz) {
		super(c, asz.add(Window.wbox.bisz()), parent);
		this.avagob = avagob;
		this.asz = asz;
	}
	
	public Avaview(Coord c, Widget parent, int avagob) {
		this(c, parent, avagob, dasz);
	}
	
	public void draw(GOut g) {
		g.chcolor(color);
		Window.wbox.draw(g, Coord.z, asz.add(Window.wbox.bisz()));
		g.chcolor(Color.WHITE);
		Gob gob = ui.sess.glob.oc.getgob(avagob);
		if(gob == null)
			return;
		Avatar ava = gob.getattr(Avatar.class);
		if(ava == null)
			return;
		GOut g2 = g.reclip(Window.wbox.tloff(), asz);
		g2.image(Equipory.bg, new Coord(Equipory.bg.sz().x / 2 - asz.x / 2, 20).inv());
		Tex at = ava.tex();
		Coord tsz = new Coord((at.sz().x * asz.x) / dasz.x, (at.sz().y * asz.y) / dasz.y);
		int yo = (20 * asz.y) / dasz.y;
		g2.image(at, new Coord(tsz.x / 2 - asz.x / 2, yo).inv(), tsz);
	}
	
	public boolean mousedown(Coord c, int button) {
		wdgmsg("click", button);
		return(true);
	}
}
