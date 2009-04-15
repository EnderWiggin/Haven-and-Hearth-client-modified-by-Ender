package haven;

import java.awt.Color;
import java.util.*;

public class BuddyWnd extends Window {
    private List<Buddy> buddies = new ArrayList<Buddy>();
    private BuddyList bl;
    public static final Tex online = Resource.loadtex("gfx/hud/online");
    public static final Tex offline = Resource.loadtex("gfx/hud/offline");
    private Comparator<Buddy> bcmp = new Comparator<Buddy>() {
	public int compare(Buddy a, Buddy b) {
	    return(a.name.compareTo(b.name));
	}
    };
    
    static {
	Widget.addtype("buddy", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new BuddyWnd(c, parent));
		}
	    });
    }
    
    private class Buddy {
	String name;
	boolean online;
    }

    private class BuddyInfo extends Widget {
	public BuddyInfo(Coord c, Coord sz, Widget parent) {
	    super(c, sz, parent);
	}
	
	public void draw(GOut g) {
	    g.chcolor(Color.BLACK);
	    g.frect(Coord.z, sz);
	    g.chcolor();
	}
    }

    private class BuddyList extends Widget {
	Scrollbar sb;
	int h;
	Buddy sel;
	
	public BuddyList(Coord c, Coord sz, Widget parent) {
	    super(c, sz, parent);
	    h = sz.y / 20;
	    sel = null;
	    sb = new Scrollbar(new Coord(sz.x, 0), sz.y, this, 0, 4);
	}
	
	public void draw(GOut g) {
	    g.chcolor(Color.BLACK);
	    g.frect(Coord.z, sz);
	    g.chcolor();
	    synchronized(buddies) {
		if(buddies.size() == 0) {
		    g.atext("You are alone in the world", sz.div(2), 0.5, 0.5);
		} else {
		    for(int i = 0; i < h; i++) {
			if(i + sb.val >= buddies.size())
			    continue;
			Buddy b = buddies.get(i);
			if(b == sel) {
			    g.chcolor(255, 255, 0, 128);
			    g.frect(new Coord(0, i * 20), new Coord(sz.x, 20));
			    g.chcolor();
			}
			if(b.online)
			    g.image(online, new Coord(0, i * 20));
			else
			    g.image(offline, new Coord(0, i * 20));
			g.atext(b.name.substring(b.name.indexOf('/') + 1), new Coord(25, i * 20 + 10), 0, 0.5);
		    }
		}
	    }
	    super.draw(g);
	}
	
	public void repop() {
	    sb.val = 0;
	    synchronized(buddies) {
		sb.max = buddies.size() - h;
	    }
	}

	public boolean mousewheel(Coord c, int amount) {
	    sb.ch(amount);
	    return(true);
	}

	public boolean mousedown(Coord c, int button) {
	    if(super.mousedown(c, button))
		return(true);
	    synchronized(buddies) {
		if(button == 1) {
		    int sel = (c.y / 20) + sb.val;
		    if(sel >= buddies.size())
			sel = -1;
		    if(sel < 0)
			this.sel = null;
		    else
			this.sel = buddies.get(sel);
		    return(true);
		}
	    }
	    return(false);
	}
    }

    public BuddyWnd(Coord c, Widget parent) {
	super(c, new Coord(400, 300), parent, "Kin");
	bl = new BuddyList(new Coord(10, 10), new Coord(180, 280), this);
	new BuddyInfo(new Coord(210, 10), new Coord(180, 280), this);
	bl.repop();
    }
    
    public void uimsg(String msg, Object... args) {
	if(msg == "add") {
	    Buddy b = new Buddy();
	    b.name = ((String)args[0]).intern();
	    b.online = ((Integer)args[1]) != 0;
	    synchronized(buddies) {
		buddies.add(b);
		Collections.sort(buddies, bcmp);
	    }
	    bl.repop();
	} else if(msg == "ch") {
	    String name = ((String)args[0]).intern();
	    boolean online = ((Integer)args[1]) != 0;
	    synchronized(buddies) {
		for(Buddy b : buddies) {
		    if(b.name == name)
			b.online = online;
		}
	    }
	} else {
	    super.uimsg(msg, args);
	}
    }
}
