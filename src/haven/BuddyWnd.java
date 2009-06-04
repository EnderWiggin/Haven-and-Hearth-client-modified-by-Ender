package haven;

import java.awt.Color;
import java.util.*;

public class BuddyWnd extends Window {
    private List<Buddy> buddies = new ArrayList<Buddy>();
    private BuddyList bl;
    private BuddyInfo bi;
    public static final Tex online = Resource.loadtex("gfx/hud/online");
    public static final Tex offline = Resource.loadtex("gfx/hud/offline");
    public static final Text.Foundry nmfnd = new Text.Foundry("Serif", 16);
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
	private Avaview ava = null;
	private Text nm = null, atime = null;
	private String id = null;
	private Button rmb, invb, chatb;
	private CheckBox trustbox;
	
	public BuddyInfo(Coord c, Coord sz, Widget parent) {
	    super(c, sz, parent);
	}
	
	public void draw(GOut g) {
	    g.chcolor(Color.BLACK);
	    g.frect(Coord.z, sz);
	    g.chcolor();
	    super.draw(g);
	    if(nm != null)
		g.image(nm.tex(), new Coord(10, 100));
	    if(atime != null)
		g.image(atime.tex(), new Coord(10, 125));
	}
	
	public void clear() {
	    if(ava != null)
		ui.destroy(ava);
	    ava = null;
	    if(rmb != null)
		ui.destroy(rmb);
	    if(invb != null)
		ui.destroy(invb);
	    if(chatb != null)
		ui.destroy(chatb);
	    rmb = invb = chatb = null;
	    if(trustbox != null)
		ui.destroy(trustbox);
	    trustbox = null;
	    nm = null;
	    id = null;
	    atime = null;
	}
	
	private void setatime(int atime) {
	    int am;
	    String unit;
	    if(atime > (604800 * 2)) {
		am = atime / 604800;
		unit = "week";
	    } else if(atime > 86400) {
		am = atime / 86400;
		unit = "day";
	    } else if(atime > 3600) {
		am = atime / 3600;
		unit = "hour";
	    } else if(atime > 60) {
		am = atime / 60;
		unit = "minute";
	    } else {
		am = atime;
		unit = "second";
	    }
	    this.atime = Text.render("Last seen: " + am + " " + unit + ((am > 1)?"s":"") + " ago");
	}

	public void uimsg(String msg, Object... args) {
	    if(msg == "i-ava") {
		List<Indir<Resource>> rl = new LinkedList<Indir<Resource>>();
		for(Object o : args)
		    rl.add(ui.sess.getres((Integer)o));
		if(ava != null)
		    ui.destroy(ava);
		ava = new Avaview(new Coord((sz.x / 2) - 40, 10), this, rl);
	    } else if(msg == "i-nm") {
		nm = nmfnd.render((String)args[0]);
		id = ((String)args[1]).intern();
	    } else if(msg == "i-atime") {
		setatime((Integer)args[0]);
	    } else if(msg == "i-act") {
		if(rmb != null)
		    ui.destroy(rmb);
		if(invb != null)
		    ui.destroy(invb);
		if(chatb != null)
		    ui.destroy(chatb);
		rmb = invb = chatb = null;
		if(trustbox != null)
		    ui.destroy(trustbox);
		trustbox = null;
		int fl = (Integer)args[0];
		if((fl & 1) != 0)
		    chatb = new Button(new Coord(10, 140), sz.x - 20, this, "Private chat") {
			    public void click() {
				BuddyWnd.this.wdgmsg("chat", id);
			    }
			};
		if((fl & 2) != 0)
		    rmb = new Button(new Coord(10, 165), sz.x - 20, this, "End kinship") {
			    public void click() {
				BuddyWnd.this.wdgmsg("rm", id);
			    }
			};
		if((fl & 4) != 0)
		    invb = new Button(new Coord(10, 190), sz.x - 20, this, "Invite to party") {
			    public void click() {
				BuddyWnd.this.wdgmsg("inv", id);
			    }
			};
		if((fl & 8) != 0)
		    trustbox = new CheckBox(new Coord(10, 215), this, "Trusted") {
			    public void changed(boolean val) {
				BuddyWnd.this.wdgmsg("trust", id, val?1:0);
			    }
			};
	    }
	}

	public void wdgmsg(Widget sender, String msg, Object... args) {
	    return;
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
			Buddy b = buddies.get(i + sb.val);
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
		    changed(this.sel);
		    return(true);
		}
	    }
	    return(false);
	}
	
	public void changed(Buddy b) {
	}
    }

    public BuddyWnd(Coord c, Widget parent) {
	super(c, new Coord(400, 300), parent, "Kin");
	bl = new BuddyList(new Coord(10, 10), new Coord(180, 280), this) {
		public void changed(Buddy b) {
		    bi.clear();
		    if(b != null)
			BuddyWnd.this.wdgmsg("ch", b.name);
		}
	    };
	bi = new BuddyInfo(new Coord(210, 10), new Coord(180, 280), this);
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
	} else if(msg == "rm") {
	    String name = ((String)args[0]).intern();
	    Buddy b = null;
	    synchronized(buddies) {
		for(Buddy b2 : buddies) {
		    if(b2.name == name) {
			b = b2;
			break;
		    }
		}
	    }
	    if(b != null) {
		buddies.remove(b);
		bl.repop();
	    }
	    if(bi.id == name)
		bi.clear();
	} else if(msg == "ch") {
	    String name = ((String)args[0]).intern();
	    boolean online = ((Integer)args[1]) != 0;
	    synchronized(buddies) {
		for(Buddy b : buddies) {
		    if(b.name == name)
			b.online = online;
		}
	    }
	} else if(msg.substring(0, 2).equals("i-")) {
	    bi.uimsg(msg, args);
	} else {
	    super.uimsg(msg, args);
	}
    }
}
