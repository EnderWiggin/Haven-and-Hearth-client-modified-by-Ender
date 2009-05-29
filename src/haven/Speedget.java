package haven;

public class Speedget extends Widget {
    public static final Tex imgs[][];
    public static final Coord tsz;
    private int cur, max;
    
    static {
	imgs = new Tex[4][3];
	String[] names = {"crawl", "walk", "run", "sprint"};
	String[] vars = {"dis", "off", "on"};
	int w = 0;
	for(int i = 0; i < 4; i++) {
	    for(int o = 0; o < 3; o++)
		imgs[i][o] = Resource.loadtex("gfx/hud/meter/rmeter/" + names[i] + "-" + vars[o]);
	    w += imgs[i][0].sz().x;
	}
	tsz = new Coord(w, imgs[0][0].sz().y);
	
	Widget.addtype("speedget", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    int cur = (Integer)args[0];
		    int max = (Integer)args[1];
		    return(new Speedget(c, parent, cur, max));
		}
	    });
    }

    public Speedget(Coord c, Widget parent, int cur, int max) {
	super(c, tsz, parent);
	this.cur = cur;
	this.max = max;
    }
    
    public void draw(GOut g) {
	int x = 0;
	for(int i = 0; i < 4; i++) {
	    Tex t;
	    if(i == cur)
		t = imgs[i][2];
	    else if(i > max)
		t = imgs[i][0];
	    else
		t = imgs[i][1];
	    g.image(t, new Coord(x, 0));
	    x += t.sz().x;
	}
    }
    
    public void uimsg(String msg, Object... args) {
	if(msg == "cur")
	    cur = (Integer)args[0];
	else if(msg == "max")
	    max = (Integer)args[0];
    }
    
    public boolean mousedown(Coord c, int button) {
	int x = 0;
	for(int i = 0; i < 4; i++) {
	    x += imgs[i][0].sz().x;
	    if(c.x < x) {
		wdgmsg("set", i);
		break;
	    }
	}
	return(true);
    }
    
    public boolean mousewheel(Coord c, int amount) {
	if(max >= 0)
	    wdgmsg("set", (cur + max + 1 + amount) % (max + 1));
	return(true);
    }
}
