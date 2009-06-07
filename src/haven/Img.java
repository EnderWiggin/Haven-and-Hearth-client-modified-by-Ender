package haven;

public class Img extends Widget {
    private Tex img;
    public boolean hit = false;
	
    static {
	Widget.addtype("img", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    Tex tex;
		    if(args.length > 1) {
			Resource res = Resource.load((String)args[0], (Integer)args[1]);
			res.loadwait();
			tex = res.layer(Resource.imgc).tex();
		    } else {
			tex = Resource.loadtex((String)args[0]);
		    }
		    Img ret = new Img(c, tex, parent);
		    if(args.length > 2)
			ret.hit = (Integer)args[2] != 0;
		    return(ret);
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
    
    public boolean mousedown(Coord c, int button) {
	if(hit) {
	    wdgmsg("click", c, button, ui.modflags());
	    return(true);
	}
	return(false);
    }
}
