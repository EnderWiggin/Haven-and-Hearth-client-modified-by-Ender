package haven;

public class Img extends Widget {
	private Tex img;
	
	static {
		Widget.addtype("img", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Img(c, Resource.loadtex((String)args[0]), parent));
			}
		});
	}
	
	public void draw(GOut g) {
		synchronized(img) {
			g.image(img, Coord.z);
		}
	}
	
	public Img(Coord c, Tex img, Widget parent) {
		super(c, img.sz(), parent);
		this.img = img;
	}
	
	public void uimsg(String name, Object... args) {
		if(name == "ch") {
			img = Resource.loadtex((String)args[0]);
		}
	}
}
