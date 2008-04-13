package haven;

import java.awt.Color;

public class Window extends Widget {
	static Color bg = new Color(179, 129, 95);
	static IBox wbox;
	boolean dm = false;
	Coord atl, asz, wsz;
	Coord tlo, rbo;
	Coord doff;
	
	static {
		Widget.addtype("wnd", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Window(c, (Coord)args[0], parent));
			}
		});
		wbox = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");
	}

	public Window(Coord c, Coord sz, Widget parent, Coord tlo, Coord rbo) {
		super(c, new Coord(0, 0), parent);
		this.tlo = tlo;
		this.rbo = rbo;
		sz = sz.add(tlo).add(rbo).add(wbox.bisz());
		this.sz = sz;
		atl = new Coord(wbox.bl.sz().x, wbox.bt.sz().y).add(tlo);
		wsz = sz.add(tlo.inv()).add(rbo.inv());
		asz = new Coord(wsz.x - wbox.bl.sz().x - wbox.br.sz().x, wsz.y - wbox.bt.sz().y - wbox.bb.sz().y);
		setfocustab(true);
	}
	
	public Window(Coord c, Coord sz, Widget parent) {
		this(c, sz, parent, new Coord(0, 0), new Coord(0, 0));
	}
	
	public void draw(GOut og) {
		GOut g = og.reclip(tlo, wsz);
		g.chcolor(bg);
		g.frect(wbox.tloff(), asz);
		wbox.draw(g, Coord.z, wsz);
		super.draw(og);
	}
	
	public void pack() {
		Coord max = new Coord(0, 0);
		for(Widget wdg = child; wdg != null; wdg = wdg.next) {
			Coord br = wdg.c.add(wdg.sz);
			if(br.x > max.x)
				max.x = br.x;
			if(br.y > max.y)
				max.y = br.y;
		}
		sz = max.add(wbox.bsz().add(tlo).add(rbo));
		wsz = sz.add(tlo.inv()).add(rbo.inv());
		asz = new Coord(wsz.x - wbox.bl.sz().x - wbox.br.sz().x, wsz.y - wbox.bt.sz().y - wbox.bb.sz().y);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "pack") {
			pack();
		} else {
			super.uimsg(msg, args);
		}
	}
	
	public Coord xlate(Coord c, boolean in) {
		Coord ctl = wbox.tloff();
		if(in)
			return(c.add(ctl).add(tlo));
		else
			return(c.add(ctl.inv()).add(tlo.inv()));
	}
	
	public boolean mousedown(Coord c, int button) {
		parent.setfocus(this);
		raise();
		if(super.mousedown(c, button))
			return(true);
		if(button != 1)
			return(false);
		if(!c.isect(tlo, sz.add(tlo.inv()).add(rbo.inv())))
			return(false);
		ui.grabmouse(this);
		dm = true;
		doff = c;
		return(true);
	}
	
	public boolean mouseup(Coord c, int button) {
		if(dm) {
			ui.grabmouse(null);
			dm = false;
		} else {
			super.mouseup(c, button);
		}
		return(true);
	}
	
	public void mousemove(Coord c) {
		if(dm) {
			this.c = this.c.add(c.add(doff.inv()));
		} else {
			super.mousemove(c);
		}
	}
}
