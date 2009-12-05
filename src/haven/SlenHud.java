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

import java.util.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import static haven.Inventory.invsq;

public class SlenHud extends Widget implements DTarget, DropTarget {
    public static final Tex bg = Resource.loadtex("gfx/hud/slen/low");
    public static final Tex flarps = Resource.loadtex("gfx/hud/slen/flarps");
    public static final Tex mbg = Resource.loadtex("gfx/hud/slen/mcircle");
    public static final Tex dispbg = Resource.loadtex("gfx/hud/slen/dispbg");
    public static final Tex uglow = Resource.loadtex("gfx/hud/slen/sbg");
    public static final Coord fc = new Coord(96, -29);
    public static final Coord mc = new Coord(316, -55);
    public static final Coord dispc = new Coord(0, 4 - dispbg.sz().y);
    public static final Coord bc1 = new Coord(147, -8);
    public static final Coord bc2 = new Coord(485, -8);
    public static final Coord sz;
    public static final Color urgcols[] = {
	null,
	new Color(0, 128, 255),
	new Color(255, 128, 0),
	new Color(255, 0, 0),
    };
    int woff = 0;
    List<HWindow> wnds = new ArrayList<HWindow>();
    HWindow awnd;
    Map<HWindow, Button> btns = new HashMap<HWindow, Button>();
    IButton hb, invb, equb, chrb, budb;
    FoldButton fb;
    Button sub, sdb;
    VC vc;
    String cmdline = null;
    static Text.Foundry errfoundry = new Text.Foundry(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14), new Color(192, 0, 0));
    static Text.Foundry cmdfoundry = new Text.Foundry(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12), new Color(245, 222, 179));
    Text cmdtext, lasterr;
    long errtime;
    @SuppressWarnings("unchecked")
    Indir<Resource>[] belt = new Indir[10];
	
    static {
	Widget.addtype("slen", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new SlenHud(c, parent));
		}
	    });
	int h = bg.sz().y;
	sz = new Coord(800, h);
	sz.y = (h - fc.y > sz.y)?(h - fc.y):sz.y;
	sz.y = (h - mc.y > sz.y)?(h - mc.y):sz.y;
    }
    
    static class FoldButton extends IButton {
	int urgency;
	
	public FoldButton(Coord c, Widget parent) {
	    super(c, parent, Resource.loadimg("gfx/hud/slen/sbu"), Resource.loadimg("gfx/hud/slen/sbd"));
	}
	
	public void draw(GOut g) {
	    super.draw(g);
	    if(urgcols[urgency] != null) {
		g.chcolor(urgcols[urgency]);
		g.image(uglow, Coord.z);
		g.chcolor();
	    }
	}
    }
    
    static class VC {
	static final long ms = 500;
	SlenHud m;
	IButton sb;
	long st;
	boolean w, c;
		
	VC(SlenHud m, IButton sb) {
	    this.m = m;
	    this.sb = sb;
	    w = c = true;
	}
		
	void hide() {
	    st = System.currentTimeMillis();
	    w = false;
	}
		
	void show() {
	    st = System.currentTimeMillis();
	    w = true;
	}
		
	void toggle() {
	    st = System.currentTimeMillis();
	    w = !w;
	    c = !w;
	}
		
	void tick() {
	    long ct = System.currentTimeMillis() - st;
	    double ca;
	    if(ct >= ms) {
		ca = 1;
	    } else {
		ca = (double)ct / (double)ms;
	    }
	    if(!w && c) {
		if(ca < 0.6) {
		    m.c.y = 600 - (int)(sz.y * (1 - (ca / 0.6)));
		} else {
		    m.c.y = 600;
		    sb.c.y = 600 - (int)(sb.sz.y * ((ca - 0.6) / 0.4));
		}
	    }
	    if(w && !c) {
		if(ca < 0.6) {
		    m.c.y = 600 - (int)(sz.y * (ca / 0.6));
		    sb.c.y = 600 - (int)(sb.sz.y * (1 - (ca / 0.6)));
		} else {
		    m.c.y = 600 - sz.y;
		    sb.c.y = 600;
		}
	    }
	    if(ct >= ms) {
		c = w;
		if(c && (m.awnd != null))
		    m.updurgency(m.awnd, -1);
	    }
	}
    }

    public SlenHud(Coord c, Widget parent) {
	super(new Coord(800, 600).add(sz.inv()), sz, parent);
	new Img(fc, flarps, this);
	new Img(mc, mbg, this);
	new Img(dispc, dispbg, this);
	hb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/hbu"), Resource.loadimg("gfx/hud/slen/hbd"));
	invb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/invu"), Resource.loadimg("gfx/hud/slen/invd"));
	equb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/equu"), Resource.loadimg("gfx/hud/slen/equd"));
	chrb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/chru"), Resource.loadimg("gfx/hud/slen/chrd"));
	budb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/budu"), Resource.loadimg("gfx/hud/slen/budd"));
	{
	    new IButton(dispc, this, Resource.loadimg("gfx/hud/slen/dispauth"), Resource.loadimg("gfx/hud/slen/dispauthd")) {
		public void click() {
		    MapView mv = ui.root.findchild(MapView.class);
		    mv.authdraw = !mv.authdraw;
		    Utils.setpref("authdraw", mv.authdraw?"on":"off");
		}
	    };
	}
	{
	    new IButton(dispc, this, Resource.loadimg("gfx/hud/slen/dispclaim"), Resource.loadimg("gfx/hud/slen/dispclaimd")) {
		private boolean v = false;
		
		public void click() {
		    MapView mv = ui.root.findchild(MapView.class);
		    if(v) {
			mv.disol(0, 1);
			v = false;
		    } else {
			mv.enol(0, 1);
			v = true;
		    }
		}
	    };
	}
	vc = new VC(this, fb = new FoldButton(new Coord(380, 600), parent) {
		public void click() {
		    vc.show();
		}
	    });
	sub = new Button(new Coord(134, 29), 100, this, Resource.loadimg("gfx/hud/slen/sau")) {
		public void click() {
		    sup();
		}
	    };
	sdb = new Button(new Coord(134, 109), 100, this, Resource.loadimg("gfx/hud/slen/sad")) {
		public void click() {
		    sdn();
		}
	    };
	new MiniMap(new Coord(5, 5), new Coord(125, 125), this, ui.mainview);
	sub.visible = sdb.visible = false;
    }
	
    public Coord xlate(Coord c, boolean in) {
	Coord bgc = sz.add(bg.sz().inv());
	if(in)
	    return(c.add(bgc));
	else
	    return(c.add(bgc.inv()));
    }
	
    public void runcmd(String[] argv) {
	String cmd = argv[0].intern();
	boolean die = false;
	try {
	    if(cmd == "q") {
		Utils.tg().interrupt();
	    } else if(cmd == "lo") {
		ui.sess.close();
	    } else if(cmd == "afk") {
		wdgmsg("afk");
	    } else if(cmd == "fs") {
		if((argv.length >= 2) && (ui.fsm != null)) {
		    if(Utils.atoi(argv[1]) != 0)
			ui.fsm.setfs();
		    else
			ui.fsm.setwnd();
		}
	    } else if(cmd == "sfx") {
		Audio.play(Resource.load(argv[1]));
	    } else if(cmd == "bgm") {
		int i = 1;
		String opt;
		boolean loop = false;
		if(i < argv.length) {
		    while((opt = argv[i]).charAt(0) == '-') {
			i++;
			if(opt.equals("-l"))
			    loop = true;
		    }
		    String resnm = argv[i++];
		    int ver = -1;
		    if(i < argv.length)
			ver = Integer.parseInt(argv[i++]);
		    Music.play(Resource.load(resnm, ver), loop);
		} else {
		    Music.play(null, false);
		}		
	    } else if(cmd == "texdis") {
		TexGL.disableall = (Integer.parseInt(argv[1]) != 0);
	    } else if(cmd == "die") {
		die = true;
	    } else if(cmd == "browse") {
		if(WebBrowser.self != null) {
		    WebBrowser.self.show(new java.net.URL(argv[1]));
		} else {
		    error("No web browser available");
		}
	    } else if(cmd == "threads") {
		java.io.StringWriter out = new java.io.StringWriter();
		Utils.dumptg(null, new java.io.PrintWriter(out));
		for(HWindow w : wnds) {
		    if(w.title.equals("Messages")) {
			for(String line : Utils.splitlines(out.toString()))
			    ((Logwindow)w).log.append(line);
		    }
		}
	    } else if(cmd == "cam") {
		if(argv.length >= 2) {
		    MapView mv = ui.root.findchild(MapView.class);
		    if(argv[1].equals("orig")) {
			mv.cam = new MapView.OrigCam();
		    } else if(argv[1].equals("kingsquest")) {
			mv.cam = new MapView.WrapCam();
		    } else if(argv[1].equals("border")) {
			mv.cam = new MapView.BorderCam();
		    } else if(argv[1].equals("predict")) {
			mv.cam = new MapView.PredictCam();
		    } else if(argv[1].equals("fixed")) {
			mv.cam = new MapView.FixedCam();
		    }
		}
	    } else if(cmd == "plol") {
		MapView mv = ui.root.findchild(MapView.class);
		Indir<Resource> res = Resource.load(argv[1]).indir();
		Message sdt;
		if(argv.length > 2)
		    sdt = new Message(0, Utils.hex2byte(argv[2]));
		else
		    sdt = new Message(0);
		Gob pl;
		if((mv.playergob >= 0) && ((pl = ui.sess.glob.oc.getgob(mv.playergob)) != null))
		    pl.ols.add(new Gob.Overlay(-1, res, sdt));
	    } else if(cmd == "sfxvol") {
		Audio.setvolume(Double.parseDouble(argv[1]));
	    } else {
		error(cmd + ": no such command");
	    }
	} catch(Exception e) {
	    error(e.getMessage());
	}
	if(die)
	    throw(new RuntimeException("Triggered death"));
    }
	
    public void error(String err) {
	lasterr = errfoundry.render(err);
	errtime = System.currentTimeMillis();
    }
	
    private Coord beltc(int i) {
	if(i < 5) {
	    return(bc1.add(i * (invsq.sz().x + 2), 0));
	} else {
	    return(bc2.add((i - 5) * (invsq.sz().x + 2), 0));
	}
    }
    
    private int beltslot(Coord c) {
	c = xlate(c, false);
	int sw = invsq.sz().x + 2;
	if((c.x >= bc1.x) && (c.y >= bc1.y) && (c.y < bc1.y + invsq.sz().y)) {
	    if((c.x - bc1.x) / sw < 5) {
		if((c.x - bc1.x) % sw < invsq.sz().x)
		    return((c.x - bc1.x) / sw);
	    }
	}
	if((c.x >= bc2.x) && (c.y >= bc2.y) && (c.y < bc2.y + invsq.sz().y)) {
	    if((c.x - bc2.x) / sw < 5) {
		if((c.x - bc2.x) % sw < invsq.sz().x)
		    return(((c.x - bc2.x) / sw) + 5);
	    }
	}
	return(-1);
    }

    public void draw(GOut g) {
	vc.tick();
	Coord bgc = sz.add(bg.sz().inv());
	g.image(bg, bgc);
	super.draw(g);
	
	for(int i = 0; i < 10; i++) {
	    Coord c = xlate(beltc(i), true);
	    g.image(invsq, c);
	    g.chcolor(156, 180, 158, 255);
	    g.atext(Integer.toString((i + 1) % 10), c.add(invsq.sz()), 1, 1);
	    g.chcolor();
	    Resource res = null;
	    if(belt[i] != null)
		res = belt[i].get();
	    if(res != null)
		g.image(res.layer(Resource.imgc).tex(), c.add(1, 1));
	}
	
	if(cmdline != null) {
	    GOut eg = g.reclip(new Coord(0, -20), new Coord(sz.x, 20));
	    if((cmdtext == null) || !cmdtext.text.equals(cmdline))
		cmdtext = cmdfoundry.render(":" + cmdline);
	    eg.image(cmdtext.tex(), new Coord(15, 0));
	    eg.line(new Coord(cmdtext.sz().x + 16, 2), new Coord(cmdtext.sz().x + 16, 14), 1);
	} else if(lasterr != null) {
	    if((System.currentTimeMillis() - errtime) > 3000) {
		lasterr = null;
	    } else {
		GOut eg = g.reclip(new Coord(0, -20), new Coord(sz.x, 20));
		eg.image(lasterr.tex(), new Coord(15, 0));
	    }
	}
    }
	
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == hb) {
	    vc.hide();
	    return;
	} else if(sender == invb) {
	    wdgmsg("inv");
	    return;
	} else if(sender == equb) {
	    wdgmsg("equ");
	    return;
	} else if(sender == chrb) {
	    wdgmsg("chr");
	    return;
	} else if(sender == budb) {
	    wdgmsg("bud");
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "err") {
	    error((String)args[0]);
	} else if(msg == "setbelt") {
	    if(args.length < 2) {
		belt[(Integer)args[0]] = null;
	    } else {
		belt[(Integer)args[0]] = ui.sess.getres((Integer)args[1]);
	    }
	} else {
	    super.uimsg(msg, args);
	}
    }
	
    private void updbtns() {
	if(wnds.size() <= 5) {
	    woff = 0;
	} else {
	    if(woff < 0)
		woff = 0;
	    if(woff > wnds.size() - 5)
		woff = wnds.size() - 5;
	}
	for(Button b : btns.values())
	    b.visible = false;
	sub.visible = sdb.visible = false;
	for(int i = 0; i < 5; i++) {
	    int wi = i + woff;
	    if(wi >= wnds.size())
		continue;
	    if((i == 0) && (woff > 0)) {
		sub.visible = true;
	    } else if((i == 4) && (woff < wnds.size() - 5)) {
		sdb.visible = true;
	    } else {
		HWindow w = wnds.get(wi);
		Button b = btns.get(w);
		b.visible = true;
		b.c = new Coord(b.c.x, 29 + (i * 20));
	    }
	}
    }
	
    private void sup() {
	woff--;
	updbtns();
    }
	
    private void sdn() {
	woff++;
	updbtns();
    }
    
    public void updurgency(HWindow wnd, int level) {
	if((wnd == awnd) && vc.c)
	    level = -1;
	if(level == -1) {
	    if(wnd.urgent == 0)
		return;
	    wnd.urgent = 0;
	} else {
	    if(wnd.urgent >= level)
		return;
	    wnd.urgent = level;
	}
	Button b = btns.get(wnd);
	if(urgcols[wnd.urgent] != null)
	    b.change(wnd.title, urgcols[wnd.urgent]);
	else
	    b.change(wnd.title);
	int max = 0;
	for(HWindow w : wnds) {
	    if(w.urgent > max)
		max = w.urgent;
	}
	fb.urgency = max;
    }
	
    public void setawnd(HWindow wnd) {
	awnd = wnd;
	for(HWindow w : wnds)
	    w.visible = false;
	if(wnd != null)
	    wnd.visible = true;
	updurgency(wnd, -1);
    }
	
    public void addwnd(final HWindow wnd) {
	wnds.add(wnd);
	setawnd(wnd);
	btns.put(wnd, new Button(new Coord(134, 29), 100, this, wnd.title) {
		public void click() {
		    setawnd(wnd);
		}
	    });
	updbtns();
    }
	
    public void remwnd(HWindow wnd) {
	if(wnd == awnd) {
	    int i = wnds.indexOf(wnd);
	    if(wnds.size() == 1)
		setawnd(null);
	    else if(i < 0)
		setawnd(wnds.get(0));
	    else if(i >= wnds.size() - 1)
		setawnd(wnds.get(i - 1));
	    else
		setawnd(wnds.get(i + 1));
	}
	wnds.remove(wnd);
	ui.destroy(btns.get(wnd));
	btns.remove(wnd);
	updbtns();
    }
	
    public boolean mousedown(Coord c, int button) {
	int slot = beltslot(c);
	if(slot != -1) {
	    wdgmsg("belt", slot, button, ui.modflags());
	    return(true);
	}
	return(super.mousedown(c, button));
    }

    public boolean mousewheel(Coord c, int amount) {
	c = xlate(c, false);
	if(c.isect(new Coord(134, 29), new Coord(100, 100))) {
	    woff += amount;
	    updbtns();
	    return(true);
	}
	return(false);
    }
	
    public boolean globtype(char ch, KeyEvent ev) {
	if(ch == ' ') {
	    vc.toggle();
	    return(true);
	} else if(ch == ':') {
	    ui.grabkeys(this);
	    cmdline = "";
	    return(true);
	} else if(ch == '0') {
	    wdgmsg("belt", 9, 1, 0);
	    return(true);
	} else if((ch >= '1') && (ch <= '9')) {
	    wdgmsg("belt", ch - '1', 1, 0);
	    return(true);
	}
	return(super.globtype(ch, ev));
    }
	
    public boolean type(char ch, KeyEvent ev) {
	if(cmdline == null) {
	    return(super.type(ch, ev));
	} else {
	    if(ch >= 32) {
		cmdline += ch;
	    } else if(ch == 8) {
		if(cmdline.length() > 0) {
		    cmdline = cmdline.substring(0, cmdline.length() - 1);
		} else {
		    cmdline = null;
		    ui.grabkeys(null);
		}
	    } else if(ch == 27) {
		cmdline = null;
		ui.grabkeys(null);
	    } else if(ch == 10) {
		String[] argv = Utils.splitwords(cmdline);
		if(argv != null) {
		    if(argv.length > 0)
			runcmd(argv);
		    cmdline = null;
		    ui.grabkeys(null);
		}
	    }
	    return(true);
	}
    }
    
    public int foldheight() {
	return(600 - c.y);
    }
    
    public boolean drop(Coord cc, Coord ul) {
	int slot = beltslot(cc);
	if(slot != -1) {
	    wdgmsg("setbelt", slot, 0);
	    return(true);
	}
	return(false);
    }

    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }

    public boolean dropthing(Coord c, Object thing) {
	int slot = beltslot(c);
	if(slot != -1) {
	    if(thing instanceof Resource) {
		Resource res = (Resource)thing;
		if(res.layer(Resource.action) != null) {
		    wdgmsg("setbelt", slot, res.name);
		    return(true);
		}
	    }
	}
	return(false);
    }
}
