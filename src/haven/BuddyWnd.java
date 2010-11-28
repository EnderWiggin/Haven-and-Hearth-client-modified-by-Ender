/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.awt.Color;
import java.util.*;
import java.text.Collator;

public class BuddyWnd extends Window {
    private List<Buddy> buddies = new ArrayList<Buddy>();
    private Map<Integer, Buddy> idmap = new HashMap<Integer, Buddy>();
    private BuddyList bl;
    private BuddyInfo bi;
    private Button sbalpha;
    private Button sbgroup;
    private Button sbstatus;
    private TextEntry charpass, opass;
    public static final Tex online = Resource.loadtex("gfx/hud/online");
    public static final Tex offline = Resource.loadtex("gfx/hud/offline");
    public static final Color[] gc = new Color[] {
	new Color(255, 255, 255),
	new Color(0, 255, 0),
	new Color(255, 0, 0),
	new Color(0, 0, 255),
	new Color(0, 255, 255),
	new Color(255, 255, 0),
	new Color(255, 0, 255),
	new Color(255, 0, 128),
    };
    private Comparator<Buddy> bcmp;
    private Comparator<Buddy> alphacmp = new Comparator<Buddy>() {
	private Collator c = Collator.getInstance();
	public int compare(Buddy a, Buddy b) {
	    return(c.compare(a.name.text, b.name.text));
	}
    };
    private Comparator<Buddy> groupcmp = new Comparator<Buddy>() {
	public int compare(Buddy a, Buddy b) {
	    if(a.group == b.group) return(alphacmp.compare(a, b));
	    else                   return(a.group - b.group);
	}
    };
    private Comparator<Buddy> statuscmp = new Comparator<Buddy>() {
	public int compare(Buddy a, Buddy b) {
	    if(a.online == b.online) return(alphacmp.compare(a, b));
	    else                     return(b.online - a.online);
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
	int id;
	Text name;
	int online;
	int group;
    }

    public static class GroupSelector extends Widget {
	public int group;
	
	public GroupSelector(Coord c, Widget parent, int group) {
	    super(c, new Coord((gc.length * 20) + 20, 20), parent);
	    this.group = group;
	}
	
	public void draw(GOut g) {
	    for(int i = 0; i < gc.length; i++) {
		if(i == group) {
		    g.chcolor();
		    g.frect(new Coord(i * 20, 0), new Coord(19, 19));
		}
		g.chcolor(gc[i]);
		g.frect(new Coord(2 + (i * 20), 2), new Coord(15, 15));
	    }
	    g.chcolor();
	}
	
	public boolean mousedown(Coord c, int button) {
	    if((c.y >= 2) && (c.y < 17)) {
		int g = (c.x - 2) / 20;
		if((g >= 0) && (g < gc.length) && (c.x >= 2 + (g * 20)) && (c.x < 17 + (g * 20))) {
		    changed(g);
		    return(true);
		}
	    }
	    return(super.mousedown(c, button));
	}
	
	protected void changed(int group) {
	    this.group = group;
	}
    }

    private class BuddyInfo extends Widget {
	private Avaview ava = null;
	private TextEntry name = null;
	private GroupSelector grp = null;
	private Text atime = null;
	private int id = -1;
	private Button rmb, invb, chatb, descb, exb;
	
	public BuddyInfo(Coord c, Coord sz, Widget parent) {
	    super(c, sz, parent);
	}
	
	public void draw(GOut g) {
	    g.chcolor(Color.BLACK);
	    g.frect(Coord.z, sz);
	    g.chcolor();
	    super.draw(g);
	    if(atime != null)
		g.image(atime.tex(), new Coord(10, 150));
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
	    if(name != null)
		ui.destroy(name);
	    if(grp != null)
		ui.destroy(grp);
	    name = null;
	    grp = null;
	    rmb = invb = chatb = null;
	    id = -1;
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
	    if(msg == "i-clear") {
		clear();
	    } if(msg == "i-ava") {
		List<Indir<Resource>> rl = new LinkedList<Indir<Resource>>();
		for(Object o : args)
		    rl.add(ui.sess.getres((Integer)o));
		if(ava != null)
		    ui.destroy(ava);
		ava = new Avaview(new Coord((sz.x / 2) - 40, 10), this, rl);
	    } else if(msg == "i-set") {
		id = (Integer)args[0];
		String name = (String)args[1];
		int group = (Integer)args[2];
		this.name = new TextEntry(new Coord(10, 100), new Coord(150, 20), this, name) {
			public boolean type(char c, java.awt.event.KeyEvent ev) {
			    if(c == 10) {
				BuddyWnd.this.wdgmsg("nick", id, text);
				return(true);
			    }
			    return(super.type(c, ev));
			}
		    };
		this.grp = new GroupSelector(new Coord(8, 128), this, group) {
			protected void changed(int group) {
			    BuddyWnd.this.wdgmsg("grp", id, group);
			}
		    };
	    } else if(msg == "i-atime") {
		setatime((Integer)args[0]);
	    } else if(msg == "i-act") {
		if(rmb != null)
		    ui.destroy(rmb);
		if(invb != null)
		    ui.destroy(invb);
		if(chatb != null)
		    ui.destroy(chatb);
		if(descb != null)
		    ui.destroy(descb);
		if(exb != null)
		    ui.destroy(exb);
		rmb = invb = chatb = null;
		int fl = (Integer)args[0];
		if((fl & 1) != 0)
		    rmb = new Button(new Coord(10, 188), sz.x - 20, this, "Forget") {
			    public void click() {
				BuddyWnd.this.wdgmsg("rm", id);
			    }
			};
		if((fl & 2) != 0)
		    chatb = new Button(new Coord(10, 165), sz.x - 20, this, "Private chat") {
			    public void click() {
				BuddyWnd.this.wdgmsg("chat", id);
			    }
			};
		if((fl & 4) != 0)
		    rmb = new Button(new Coord(10, 188), sz.x - 20, this, "End kinship") {
			    public void click() {
				BuddyWnd.this.wdgmsg("rm", id);
			    }
			};
		if((fl & 8) != 0)
		    invb = new Button(new Coord(10, 211), sz.x - 20, this, "Invite to party") {
			    public void click() {
				BuddyWnd.this.wdgmsg("inv", id);
			    }
			};
		if((fl & 16) != 0)
		    descb = new Button(new Coord(10, 234), sz.x - 20, this, "Describe to...") {
			    public void click() {
				BuddyWnd.this.wdgmsg("desc", id);
			    }
			};
		if((fl & 32) != 0)
		    exb = new Button(new Coord(10, 257), sz.x - 20, this, "Exile") {
			    public void click() {
				BuddyWnd.this.wdgmsg("exile", id);
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
			if(b.online == 1)
			    g.image(online, new Coord(0, i * 20));
			else if(b.online == 0)
			    g.image(offline, new Coord(0, i * 20));
			g.chcolor(gc[b.group]);
			g.aimage(b.name.tex(), new Coord(25, i * 20 + 10), 0, 0.5);
			g.chcolor();
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

	public void select(Buddy b) {
	    this.sel = b;
	    changed(this.sel);
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
			select(null);
		    else
			select(buddies.get(sel));
		    return(true);
		}
	    }
	    return(false);
	}
	
	public void changed(Buddy b) {
	}
    }

    public BuddyWnd(Coord c, Widget parent) {
	super(c, new Coord(400, 370), parent, "Kin");
	bl = new BuddyList(new Coord(10, 5), new Coord(180, 280), this) {
		public void changed(Buddy b) {
		    if(b != null)
			BuddyWnd.this.wdgmsg("ch", b.id);
		}
	    };
	bi = new BuddyInfo(new Coord(210, 5), new Coord(180, 280), this);
	sbstatus = new Button(new Coord(5,   290), 120, this, "Sort by status")      { public void click() { setcmp(statuscmp); } };
	sbgroup  = new Button(new Coord(140, 290), 120, this, "Sort by group")       { public void click() { setcmp(groupcmp); } };
	sbalpha  = new Button(new Coord(275, 290), 120, this, "Sort alphabetically") { public void click() { setcmp(alphacmp); } };
	String sort = Utils.getpref("buddysort", "");
	if(sort.equals("")) {
	    bcmp = statuscmp;
	} else {
	    if(sort.equals("alpha"))  bcmp = alphacmp;
	    if(sort.equals("group"))  bcmp = groupcmp;
	    if(sort.equals("status")) bcmp = statuscmp;
	}
	new Label(new Coord(0, 310), this, "My hearth secret:");
	new Label(new Coord(200, 310), this, "Make kin by hearth secret:");
	charpass = new TextEntry(new Coord(0, 325), new Coord(190, 20), this, "") {
		public void activate(String text) {
		    BuddyWnd.this.wdgmsg("pwd", text);
		}
	    };
	opass = new TextEntry(new Coord(200, 325), new Coord(190, 20), this, "") {
		public void activate(String text) {
		    BuddyWnd.this.wdgmsg("bypwd", text);
		    settext("");
		}
	    };
	new Button(new Coord(0  , 350), 50, this, "Set")    { public void click() {sendpwd(charpass.text);} };
	new Button(new Coord(60 , 350), 50, this, "Clear")  { public void click() {sendpwd("");} };
	new Button(new Coord(120, 350), 50, this, "Random") { public void click() {sendpwd(randpwd());} };
	new Button(new Coord(200, 350), 50, this, "Add kin") {
	    public void click() {
		BuddyWnd.this.wdgmsg("bypwd", opass.text);
		opass.settext("");
	    }
	};
	bl.repop();
    }
    
    private String randpwd() {
	String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	StringBuilder buf = new StringBuilder();
	for(int i = 0; i < 8; i++)
	    buf.append(charset.charAt((int)(Math.random() * charset.length())));
	return(buf.toString());
    }
    
    private void sendpwd(String pass) {
	wdgmsg("pwd", pass);
	charpass.settext(pass);
    }

    private void setcmp(Comparator<Buddy> cmp) {
	bcmp = cmp;
	String val = "";
	if(cmp == alphacmp)  val = "alpha";
	if(cmp == groupcmp)  val = "group";
	if(cmp == statuscmp) val = "status";
	Utils.setpref("buddysort", val);
	synchronized(buddies) {
	    Collections.sort(buddies, bcmp);
	}
    }
    
    public void uimsg(String msg, Object... args) {
	if(msg == "add") {
	    Buddy b = new Buddy();
	    b.id = (Integer)args[0];
	    b.name = Text.render(((String)args[1]));
	    b.online = (Integer)args[2];
	    b.group = (Integer)args[3];
	    synchronized(buddies) {
		buddies.add(b);
		idmap.put(b.id, b);
		Collections.sort(buddies, bcmp);
	    }
	    bl.repop();
	} else if(msg == "rm") {
	    int id = (Integer)args[0];
	    Buddy b;
	    synchronized(buddies) {
		b = idmap.get(id);
	    }
	    if(b != null) {
		buddies.remove(b);
		bl.repop();
	    }
	    if(bi.id == id)
		bi.clear();
	} else if(msg == "chst") {
	    int id = (Integer)args[0];
	    int online = (Integer)args[1];
	    synchronized(buddies) {
		idmap.get(id).online = online;
	    }
	} else if(msg == "chnm") {
	    int id = (Integer)args[0];
	    String name = (String)args[1];
	    synchronized(buddies) {
		idmap.get(id).name = Text.render(name);
	    }
	} else if(msg == "chgrp") {
	    int id = (Integer)args[0];
	    int group = (Integer)args[1];
	    synchronized(buddies) {
		idmap.get(id).group = group;
	    }
	} else if(msg == "sel") {
	    int id = (Integer)args[0];
	    Buddy tgt;
	    synchronized(buddies) {
		tgt = idmap.get(id);
	    }
	    bl.select(tgt);
	} else if(msg == "pwd") {
	    charpass.settext((String)args[0]);
	} else if(msg.substring(0, 2).equals("i-")) {
	    bi.uimsg(msg, args);
	} else {
	    super.uimsg(msg, args);
	}
    }
}
