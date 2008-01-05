package haven;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class Window extends Widget {
	static Color bg = new Color(179, 129, 95);
	BufferedImage ctl, ctr, cbl, cbr;
	BufferedImage bl, br, bt, bb;
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
	}

	public Window(Coord c, Coord sz, Widget parent, Coord tlo, Coord rbo) {
		super(c, new Coord(0, 0), parent);
		this.tlo = tlo;
		this.rbo = rbo;
		ctl = Resource.loadimg("gfx/hud/tl.gif");
		ctr = Resource.loadimg("gfx/hud/tr.gif");
		cbl = Resource.loadimg("gfx/hud/bl.gif");
		cbr = Resource.loadimg("gfx/hud/br.gif");
		bl = Resource.loadimg("gfx/hud/extvl.gif");
		br = Resource.loadimg("gfx/hud/extvr.gif");
		bt = Resource.loadimg("gfx/hud/extht.gif");
		bb = Resource.loadimg("gfx/hud/exthb.gif");
		sz = sz.add(tlo).add(rbo).add(new Coord(bl.getWidth() + br.getWidth(), bt.getHeight() + bb.getHeight()));
		this.sz = sz;
		atl = new Coord(bl.getWidth(), bt.getHeight()).add(tlo);
		wsz = sz.add(tlo.inv()).add(rbo.inv());
		asz = new Coord(wsz.x - bl.getWidth() - br.getWidth(), wsz.y - bt.getHeight() - bb.getHeight());
		settabfocus(true);
	}
	
	public Window(Coord c, Coord sz, Widget parent) {
		this(c, sz, parent, new Coord(0, 0), new Coord(0, 0));
	}
	
	public void draw(Graphics og) {
		Graphics g = og.create(tlo.x, tlo.y, wsz.x, wsz.y);
		g.setColor(bg);
		g.fillRect(bl.getWidth(), bt.getHeight(), asz.x, asz.y);
		for(int x = ctl.getWidth(); x < wsz.x - ctr.getWidth(); x++)
			g.drawImage(bt, x, 0, null);
		for(int x = cbl.getWidth(); x < wsz.x - cbr.getWidth(); x++)
			g.drawImage(bb, x, wsz.y - bb.getHeight(), null);
		for(int y = ctl.getHeight(); y < wsz.y - cbl.getHeight(); y++)
			g.drawImage(bl, 0, y, null);
		for(int y = ctr.getHeight(); y < wsz.y - cbr.getHeight(); y++)
			g.drawImage(br, wsz.x - br.getWidth(), y, null);
		g.drawImage(ctl, 0, 0, null);
		g.drawImage(ctr, wsz.x - ctr.getWidth(), 0, null);
		g.drawImage(cbl, 0, wsz.y - cbl.getHeight(), null);
		g.drawImage(cbr, wsz.x - cbr.getWidth(), wsz.y - cbr.getHeight(), null);
		super.draw(og);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "pack") {
			Coord max = new Coord(0, 0);
			for(Widget wdg = child; wdg != null; wdg = wdg.next) {
				Coord br = wdg.c.add(wdg.sz);
				if(br.x > max.x)
					max.x = br.x;
				if(br.y > max.y)
					max.y = br.y;
			}
			sz = max.add(Utils.imgsz(ctl)).add(Utils.imgsz(cbr)).add(tlo).add(rbo);
			wsz = sz.add(tlo.inv()).add(rbo.inv());
			asz = new Coord(wsz.x - bl.getWidth() - br.getWidth(), wsz.y - bt.getHeight() - bb.getHeight());
		} else {
			super.uimsg(msg, args);
		}
	}
	
	public Coord xlate(Coord c, boolean in) {
		Coord ctl = new Coord(bl.getWidth(), bt.getHeight());
		if(in)
			return(c.add(ctl).add(tlo));
		else
			return(c.add(ctl.inv()).add(tlo.inv()));
	}
	
	public boolean mousedown(Coord c, int button) {
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
