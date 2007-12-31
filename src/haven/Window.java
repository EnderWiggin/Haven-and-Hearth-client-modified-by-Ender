package haven;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class Window extends Widget {
	public static int barda = 7;
	static Color bg = new Color(179, 129, 95);
	BufferedImage ctl, ctr, cbl, cbr;
	BufferedImage bl, br, bt, bb;
	boolean dm = false;
	Coord doff;
	
	static {
		Widget.addtype("wnd", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Window(c, (Coord)args[0], parent));
			}
		});
	}

	public Window(Coord c, Coord sz, Widget parent) {
		super(c, sz, parent);
		ctl = Resource.loadimg("gfx/hud/tl.gif");
		ctr = Resource.loadimg("gfx/hud/tr.gif");
		cbl = Resource.loadimg("gfx/hud/bl.gif");
		cbr = Resource.loadimg("gfx/hud/br.gif");
		bl = Resource.loadimg("gfx/hud/extvl.gif");
		br = Resource.loadimg("gfx/hud/extvr.gif");
		bt = Resource.loadimg("gfx/hud/extht.gif");
		bb = Resource.loadimg("gfx/hud/exthb.gif");
		settabfocus(true);
	}
	
	public void draw(Graphics g) {
		g.setColor(bg);
		g.fillRect(bt.getHeight(), bl.getWidth(), sz.x - bl.getWidth() - br.getWidth(), sz.y - bt.getHeight() - bb.getHeight());
		super.draw(g);
		for(int x = ctl.getWidth(); x < sz.x - ctr.getWidth(); x++)
			g.drawImage(bt, x, 0, null);
		for(int x = cbl.getWidth(); x < sz.x - cbr.getWidth(); x++)
			g.drawImage(bb, x, sz.y - bb.getHeight(), null);
		for(int y = ctl.getHeight(); y < sz.y - cbl.getHeight(); y++)
			g.drawImage(bl, 0, y, null);
		for(int y = ctr.getHeight(); y < sz.y - cbr.getHeight(); y++)
			g.drawImage(br, sz.x - br.getWidth(), y, null);
		g.drawImage(ctl, 0, 0, null);
		g.drawImage(ctr, sz.x - ctr.getWidth(), 0, null);
		g.drawImage(cbl, 0, sz.y - cbl.getHeight(), null);
		g.drawImage(cbr, sz.x - cbr.getWidth(), sz.y - cbr.getHeight(), null);
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
			sz = max.add(Utils.imgsz(ctl)).add(Utils.imgsz(cbr));
		} else {
			super.uimsg(msg, args);
		}
	}
	
	public Coord xlate(Coord c, boolean in) {
		if(in)
			return(c.add(Utils.imgsz(ctl)));
		else
			return(c.add(Utils.imgsz(ctl).inv()));
	}
	
	public boolean mousedown(Coord c, int button) {
		raise();
		if(super.mousedown(c, button))
			return(true);
		if(button != 1)
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
		if(dm)
			this.c = this.c.add(c.add(doff.inv()));
	}
}
