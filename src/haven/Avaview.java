package haven;

public class Avaview extends Widget {
	public static final Coord asz = new Coord(70, 70);
	int avagob;
	
	static {
		Widget.addtype("av", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Avaview(c, parent, (Integer)args[0]));
			}
		});
	}
	
	public Avaview(Coord c, Widget parent, int avagob) {
		super(c, asz.add(Window.wbox.bisz()), parent);
		this.avagob = avagob;
	}
	
	public void draw(GOut g) {
		Window.wbox.draw(g, Coord.z, asz.add(Window.wbox.bisz()));
		Gob gob = ui.sess.glob.oc.getgob(avagob);
		if(gob == null)
			return;
		Avatar ava = gob.getattr(Avatar.class);
		if(ava == null)
			return;
		GOut g2 = g.reclip(Window.wbox.tloff(), asz);
		g2.image(Equipory.bg, new Coord(Equipory.bg.sz().x / 2 - asz.x / 2, 20).inv());
		Tex at = ava.tex();
		g2.image(at, new Coord(at.sz().x / 2 - asz.x / 2, 20).inv());
	}
}
