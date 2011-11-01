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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ender.timer.TimerController;

public class SlenHud extends ConsoleHost implements DTarget, DropTarget, Console.Directory, IHWindowParent {
    public static Tex bg = Resource.loadtex("gfx/hud/slen/low");
    //public static final Tex flarps = Resource.loadtex("gfx/hud/slen/flarps");
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
    int dy;
    List<HWindow> wnds = new ArrayList<HWindow>();
    HWindow awnd;
    Map<HWindow, Button> btns = new HashMap<HWindow, Button>();
    IButton hb, invb, equb, chrb, budb, optb;
    FoldButton fb;
    Button sub, sdb;
    VC vc;
    static Text.Foundry errfoundry = new Text.Foundry(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14), new Color(192, 0, 0));
    Text lasterr;
    long errtime;
    OptWnd optwnd = null;
    static int dh;
	
    static {
	Widget.addtype("slen", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new SlenHud(c, parent));
		}
	    });
	if(Config.new_minimap)
	    bg = Resource.loadtex("gfx/hud/slen/low2");
	int h = bg.sz().y;
	sz = new Coord(800, h);
	sz.y = (h - fc.y > sz.y)?(h - fc.y):sz.y;
	sz.y = (h - mc.y > sz.y)?(h - mc.y):sz.y;
	dh = h - sz.y;
    }
    
    static class FoldButton extends IButton {
	int urgency;
    int dy;
	
	public FoldButton(Coord c, Widget parent) {
	    super(c, parent, Resource.loadimg("gfx/hud/slen/sbu"), Resource.loadimg("gfx/hud/slen/sbd"));
        dy = sz.y;
	}
	
	public void draw(GOut g) {
        c.x = (MainFrame.innerSize.width - sz.x) / 2;
        c.y = MainFrame.innerSize.height + dy;
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
	FoldButton sb;
	long st;
	boolean w, c;
		
	VC(SlenHud m, FoldButton sb) {
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
		    m.dy = (int)(-sz.y * (1 - (ca / 0.6)));
		} else {
		    m.dy = 0;
		    sb.dy = (int)(-sb.sz.y * ((ca - 0.6) / 0.4));
		}
	    }
	    if(w && !c) {
		if(ca < 0.6) {
		    m.dy = (int)(-sz.y * (ca / 0.6));
		    sb.dy = (int)(-sb.sz.y * (1 - (ca / 0.6)));
		} else {
		    m.dy = -sz.y;
		    sb.dy = 0;
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
	super(new Coord((MainFrame.innerSize.width - sz.x) / 2, MainFrame.innerSize.height - sz.y), sz, parent);
	ui.slen = this;
	if(Config.new_chat)
	    new ChatHWPanel(new Coord(0,MainFrame.getInnerSize().y-300), new Coord(350,300), ui.root);
	else
	    ui.chat = this;
	dy = -sz.y;
	//new Img(fc, flarps, this);
	new Img(mc, mbg, this);
	if(!Config.new_minimap)
	    new Img(dispc, dispbg, this);
	hb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/hbu"), Resource.loadimg("gfx/hud/slen/hbd"));
	invb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/invu"), Resource.loadimg("gfx/hud/slen/invd"));
	equb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/equu"), Resource.loadimg("gfx/hud/slen/equd"));
	chrb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/chru"), Resource.loadimg("gfx/hud/slen/chrd"));
	budb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/budu"), Resource.loadimg("gfx/hud/slen/budd"));
	optb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/optu"), Resource.loadimg("gfx/hud/slen/optd"));
	if (!Config.new_minimap) {
	    {
		new IButton(dispc, this,Resource.loadimg("gfx/hud/slen/dispauth"),Resource.loadimg("gfx/hud/slen/dispauthd")) {
		    private boolean v = false;

		    public void click() {
			MapView mv = ui.mainview;
			if (v) {
			    mv.disol(2, 3);
			    v = false;
			} else {
			    mv.enol(2, 3);
			    v = true;
			}
		    }
		};
	    }
	    {
		new IButton(dispc, this,Resource.loadimg("gfx/hud/slen/dispclaim"),Resource.loadimg("gfx/hud/slen/dispclaimd")) {
		    private boolean v = false;

		    public void click() {
			MapView mv = ui.mainview;
			if (v) {
			    mv.disol(0, 1);
			    v = false;
			} else {
			    mv.enol(0, 1);
			    v = true;
			}
		    }
		};
	    }
	    new MiniMap(new Coord(5, 5), new Coord(125, 125), this, ui.mainview);
	} else {
	    new MinimapPanel(Coord.z, Coord.z, ui.root);
	}
	vc = new VC(this, fb = new FoldButton(new Coord((MainFrame.innerSize.width - 40) / 2, MainFrame.innerSize.height), parent) {
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
	sub.visible = sdb.visible = false;
	TimerController.getInstance().load();
    }

    public Coord xlate(Coord c, boolean in) {
	Coord bgc = sz.add(bg.sz().inv());
	if(in)
	    return(c.add(bgc));
	else
	    bgc.y += dh;
	    return(c.add(bgc.inv()));
    }
	
    public void error(String err) {
	lasterr = errfoundry.render(err);
	errtime = System.currentTimeMillis();
    }
	
    public void draw(GOut g) {
	vc.tick();
        c.x = (MainFrame.innerSize.width - sz.x) / 2;
        c.y = MainFrame.innerSize.height + dy;
	Coord bgc = sz.add(bg.sz().inv());
	g.image(bg, bgc);
	super.draw(g);
	
	if(cmdline != null) {
	    drawcmd(g.reclip(new Coord(0, -20), new Coord(sz.x, 20)), new Coord(15, 0));
	    GOut eg = g.reclip(new Coord(0, -20), new Coord(sz.x, 20));
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
	} else if((sender == chrb)&&(CharWnd.instance != null)) {
	    CharWnd.instance.toggle();
	    return;
	} else if((sender == budb)&&(BuddyWnd.instance != null)) {
	    BuddyWnd.instance.visible = !BuddyWnd.instance.visible;
	    return;
	} else if(sender == optb) {
	    toggleopts();
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }
    
    public void binded() {
	wdgmsg("bud");
	wdgmsg("chr");
    }
    
    public void uimsg(String msg, Object... args) {
	if (msg == "err") {
	    error((String) args[0]);
	} else if (msg == "setbelt") {
		if (args.length < 2) {
		    ToolbarWnd.setbelt((Integer) args[0], null);
		} else {
		    ToolbarWnd.setbelt((Integer) args[0], ui.sess.getres((Integer) args[1]));
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
	
    public void setawnd(HWindow wnd, boolean focus) {
	awnd = wnd;
	for(HWindow w : wnds)
	    w.visible = false;
	if(wnd != null)
	    wnd.visible = true;
	updurgency(wnd, -1);
	if(focus)
	    vc.show();
    }
    
    public void setawnd(HWindow wnd) {
	setawnd(wnd, false);
    }
    
    @Override
    public HWindow getawnd() {
	return awnd;
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
	if(wnd.title.equals("Messages")) {
	    ui.cons.out = new java.io.PrintWriter(new java.io.Writer() {
		    StringBuilder buf = new StringBuilder();
		    
		    public void write(char[] src, int off, int len) {
			buf.append(src, off, len);
			int p;
			while((p = buf.indexOf("\n")) >= 0) {
			    ((Logwindow)wnd).log.append((Config.timestamp?Utils.timestamp():"")+buf.substring(0, p));
			    buf.delete(0, p + 1);
			}
		    }
		    
		    public void close() {}
		    public void flush() {}
		});
	}
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
	/*int slot = beltslot(c);
	if(slot != -1) {
	    if (belt[currentBelt][slot] != null) {
		wdgmsg("belt", slot, button, ui.modflags());
	    }
	    return(true);
	}*/
	return(super.mousedown(c, button));
    }

    public boolean mousewheel(Coord c, int amount) {
	c = xlate(c, false);
	if(c.isect(new Coord(134, 29), new Coord(100, 100))) {
	    woff += amount;
	    updbtns();
	    return(true);
	}
	return(super.mousewheel(c, amount));
    }
    
    private void toggleopts() {
	if(optwnd != null) {
	    optwnd.wdgmsg("close");
	} else {
	    optwnd = new OptWnd(new Coord(100, 100), parent) {
		    public void wdgmsg(Widget sender, String msg, Object... args) {
			if(msg.equals("close")) {
			    ui.destroy(this);
			    optwnd = null;
			} else {
			    super.wdgmsg(sender, msg, args);
			}
		    }
		};
	}
    }
    
    public boolean globtype(char ch, KeyEvent ev) {
	if(ch == ' ') {
	    vc.toggle();
	    return(true);
	} else if(ch == ':') {
	    entercmd();
	    return(true);
	/*} else if((ch >= '0') && (ch <= '9')) {
        if (ev.isAltDown()) {
            currentBelt = ch - '0';
            for (int i = 0; i < 10; i++) {
                if (belt[currentBelt][i] != null) {
                    wdgmsg("setbelt", i, belt[currentBelt][i].get().name);
                } else {
                    wdgmsg("setbelt", i, 0);
                }
            }
        } else {
            int slot = (ch > '0' ? ch - '1' : 9);
            if (belt[currentBelt][slot] != null) {
                wdgmsg("belt", slot, 1, 0);
            }
        }
	    return(true);*/
	} else if(ch == 15) {
	    toggleopts();
	}
	return(super.globtype(ch, ev));
    }
	
    public int foldheight() {
	return(MainFrame.innerSize.height - c.y);
    }
    
    public boolean drop(Coord cc, Coord ul) {
	/*int slot = beltslot(cc);
	if(slot != -1) {
	    wdgmsg("setbelt", slot, 0);
	    return(true);
	}*/
	return(false);
    }

    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }

    public boolean dropthing(Coord c, Object thing) {
	/*int slot = beltslot(c);
	if(slot != -1) {
	    if(thing instanceof Resource) {
		Resource res = (Resource)thing;
		if(res.layer(Resource.action) != null) {
		    belt[currentBelt][slot] = res.indir();
		    wdgmsg("setbelt", slot, res.name);
		    return(true);
		}
	    }
	}*/
	return(false);
    }

    private Map<String, Console.Command> cmdmap = new TreeMap<String, Console.Command>();
    {
	cmdmap.put("afk", new Console.Command() {
		public void run(Console cons, String[] args) {
		    wdgmsg("afk");
		}
	    });
    }
    public Map<String, Console.Command> findcmds() {
	return(cmdmap);
    }
}
