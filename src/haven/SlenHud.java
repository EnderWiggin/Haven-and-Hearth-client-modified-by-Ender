package haven;

import java.util.*;
import java.awt.Color;
import java.awt.event.KeyEvent;

public class SlenHud extends Widget {
	public static final Tex bg = Resource.loadtex("gfx/hud/slen/low");
	public static final Tex flarps = Resource.loadtex("gfx/hud/slen/flarps");
	public static final Tex mbg = Resource.loadtex("gfx/hud/slen/mcircle");
	public static final Coord fc = new Coord(96, -29);
	public static final Coord mc = new Coord(316, -55);
	public static final Coord sz;
	int woff = 0;
	List<HWindow> wnds = new ArrayList<HWindow>();
	HWindow awnd;
	Map<HWindow, Button> btns = new HashMap<HWindow, Button>();
	IButton hb, invb, equb;
	Button sub, sdb;
	VC vc;
	String cmdline = null;
	static Text.Foundry errfoundry = new Text.Foundry(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14), new Color(192, 0, 0));
	static Text.Foundry cmdfoundry = new Text.Foundry(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12), new Color(245, 222, 179));
	Text cmdtext, lasterr;
	long errtime;
	
	static {
		Widget.addtype("slen", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new SlenHud(c, parent));
			}
		});
		int h = bg.sz().y;
		h = (h - fc.y > h)?(h - fc.y):h;
		h = (h - mc.y > h)?(h - mc.y):h;
		sz = new Coord(800, h);
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
			if(ct >= ms)
				c = w;
		}
	}

	public SlenHud(Coord c, Widget parent) {
		super(new Coord(800, 600).add(sz.inv()), sz, parent);
		new Img(fc, flarps, this);
		new Img(mc, mbg, this);
		hb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/hbu"), Resource.loadimg("gfx/hud/slen/hbd"));
		invb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/invu"), Resource.loadimg("gfx/hud/slen/invd"));
		equb = new IButton(mc, this, Resource.loadimg("gfx/hud/slen/equu"), Resource.loadimg("gfx/hud/slen/equd"));
		vc = new VC(this, new IButton(new Coord(380, 600), parent, Resource.loadimg("gfx/hud/slen/sbu"), Resource.loadimg("gfx/hud/slen/sbd")) {
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
		if(cmd == "q") {
			Utils.tg().interrupt();
		} else if(cmd == "lo") {
			ui.sess.close();
		} else if(cmd == "afk") {
			wdgmsg("afk");
		} else {
			error(cmd + ": no such command");
		}
	}
	
	public void error(String err) {
		lasterr = errfoundry.render(err);
		errtime = System.currentTimeMillis();
	}
	
	public void draw(GOut g) {
		vc.tick();
		Coord bgc = sz.add(bg.sz().inv());
		g.image(bg, bgc);
		super.draw(g);
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
		}
		super.wdgmsg(sender, msg, args);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "err") {
			error((String)args[0]);
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
	
	private void setawnd(HWindow wnd) {
		awnd = wnd;
		for(HWindow w : wnds)
			w.visible = false;
		if(wnd != null)
			wnd.visible = true;
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
		if(ev.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
			vc.toggle();
			return(true);
		} else if(ch == ':') {
			ui.grabkeys(this);
			cmdline = "";
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
}
