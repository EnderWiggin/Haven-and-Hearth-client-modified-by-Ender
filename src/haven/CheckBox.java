package haven;

public class CheckBox extends Widget {
	static Tex box, mark;
	boolean a = false;
	Text lbl;
	
	static {
		Widget.addtype("chk", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new CheckBox(c, parent, (String)args[0]));
			}
		});
		box = Resource.loadtex("gfx/hud/chkbox");
		mark = Resource.loadtex("gfx/hud/chkmark");
	}
	
	public CheckBox(Coord c, Widget parent, String lbl) {
		super(c, box.sz(), parent);
		this.lbl = Text.std.render(lbl, java.awt.Color.WHITE);
		sz = box.sz().add(this.lbl.sz());
	}
	
	public boolean mousedown(Coord c, int button) {
		if(button != 1)
			return(false);
		a = !a;
		wdgmsg("ch", a);
		return(true);
	}

	public void draw(GOut g) {
		g.image(lbl.tex(), new Coord(box.sz().x, box.sz().y - lbl.sz().y));
		g.image(box, Coord.z);
		if(a)
			g.image(mark, Coord.z);
		super.draw(g);
	}
}
