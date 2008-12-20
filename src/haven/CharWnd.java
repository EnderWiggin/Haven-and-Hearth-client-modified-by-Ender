package haven;

import java.awt.Color;
import java.util.*;

public class CharWnd extends Window {
    Widget cattr;
    Label cost;
    Label explbl;
    int exp;
    Map<String, Attr> attrs = new TreeMap<String, Attr>();
    public static final Color debuff = new Color(255, 128, 128);
    public static final Color buff = new Color(128, 255, 128);
    
    static {
	Widget.addtype("chr", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new CharWnd(c, parent));
		}
	    });
    }
    
    class Attr implements Observer {
	String nm;
	Label lbl;
	Glob.CAttr attr;
	
	Attr(String nm, int x, int y) {
	    this.nm = nm;
	    attr = ui.sess.glob.cattr.get(nm);
	    this.lbl = new Label(new Coord(x, y), cattr, "0");
	    attrs.put(nm, this);
	    update();
	    attr.addObserver(this);
	}
	
	public void update() {
	    lbl.settext(Integer.toString(attr.comp));
	    if(attr.comp < attr.base)
		lbl.setcolor(debuff);
	    else if(attr.comp > attr.base)
		lbl.setcolor(buff);
	    else
		lbl.setcolor(Color.WHITE);
	}
	
	public void update(Observable attrslen, Object uudata) {
	    Glob.CAttr attr = (Glob.CAttr)attrslen;
	    update();
	}
	
	private void destroy() {
	    attr.deleteObserver(this);
	}
    }
    
    private void updexp() {
	int cost = 0;
	for(Attr attr : attrs.values()) {
	    if(attr instanceof SAttr)
		cost += ((SAttr)attr).cost;
	}
	this.cost.settext(Integer.toString(cost));
	this.explbl.settext(Integer.toString(exp));
	if(cost > exp)
	    this.cost.setcolor(new Color(255, 128, 128));
	else
	    this.cost.setcolor(new Color(255, 255, 255));
    }

    class SAttr extends Attr {
	IButton minus, plus;
	int tvalb, tvalc;
	int cost;
	
	SAttr(String nm, int x, int y) {
	    super(nm, x, y);
	    tvalb = attr.base;
	    tvalc = attr.comp;
	    minus = new IButton(new Coord(x + 30, y), cattr, Resource.loadimg("gfx/hud/charsh/minusup"), Resource.loadimg("gfx/hud/charsh/minusdown")) {
		    public void click() {
			dec();
			upd();
		    }
		};
	    plus = new IButton(new Coord(x + 45, y), cattr, Resource.loadimg("gfx/hud/charsh/plusup"), Resource.loadimg("gfx/hud/charsh/plusdown")) {
		    public void click() {
			inc();
			upd();
		    }
		};
	}
	
	void upd() {
	    lbl.settext(Integer.toString(tvalc));
	    if(tvalb > attr.base)
		lbl.setcolor(new Color(128, 128, 255));
	    else
		lbl.setcolor(new Color(255, 255, 255));
	    updexp();
	}

	boolean inc() {
	    tvalb++; tvalc++;
	    cost += tvalb;
	    return(true);
	}
	
	boolean dec() {
	    if(tvalb > attr.base) {
		cost -= tvalb;
		tvalb--; tvalc--;
		return(true);
	    }
	    return(false);
	}
	
	public void update() {
	    super.update();
	    tvalb = attr.base;
	    tvalc = attr.comp;
	}
    }
    
    private void buysattrs() {
	ArrayList<Object> args = new ArrayList<Object>();
	for(Attr attr : attrs.values()) {
	    if(attr instanceof SAttr) {
		SAttr sa = (SAttr)attr;
		args.add(sa.nm);
		args.add(sa.tvalb);
	    }
	}
	wdgmsg("sattr", args.toArray());
    }

    public CharWnd(Coord c, Widget parent) {
	super(c, new Coord(400, 300), parent, "Character Sheet");
	cattr = new Widget(Coord.z, new Coord(400, 275), this);
	new Label(new Coord(10, 10), cattr, "Base Attributes:");
	new Img(new Coord(10, 40), Resource.loadtex("gfx/hud/charsh/str"), cattr);
	new Img(new Coord(10, 55), Resource.loadtex("gfx/hud/charsh/agil"), cattr);
	new Img(new Coord(10, 70), Resource.loadtex("gfx/hud/charsh/intel"), cattr);
	new Img(new Coord(10, 85), Resource.loadtex("gfx/hud/charsh/cons"), cattr);
	new Label(new Coord(30, 40), cattr, "Strength:");
	new Label(new Coord(30, 55), cattr, "Agility:");
	new Label(new Coord(30, 70), cattr, "Intelligence:");
	new Label(new Coord(30, 85), cattr, "Constitution:");
	new Attr("str", 100, 40);
	new Attr("agil", 100, 55);
	new Attr("intel", 100, 70);
	new Attr("cons", 100, 85);

	new Label(new Coord(210, 10), cattr, "Skill Values:");
	new Label(new Coord(210, 40), cattr, "Unarmed Combat:");
	new Label(new Coord(210, 55), cattr, "Melee Combat:");
	new SAttr("unarmed", 300, 40);
	new SAttr("melee", 300, 55);
	new Label(new Coord(210, 85), cattr, "Cost:");
	cost = new Label(new Coord(300, 85), cattr, "0");
	new Label(new Coord(210, 100), cattr, "Learning Points:");
	explbl = new Label(new Coord(300, 100), cattr, "0");
	new Button(new Coord(210, 115), 75, cattr, "Buy") {
	    public void click() {
		buysattrs();
	    }
	};
    }

    public void uimsg(String msg, Object... args) {
	if(msg == "exp") {
	    exp = (Integer)args[0];
	    updexp();
	}
    }
    
    public void destroy() {
	for(Attr attr : attrs.values())
	    attr.destroy();
	super.destroy();
    }
}
