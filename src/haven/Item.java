package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Color;

public class Item extends SSWidget {
	static Coord shoff = new Coord(1, 3);
	boolean dm = false;
	Coord doff;
	int num = -1;
	BufferedImage img, sh;
	
	static {
		Widget.addtype("item", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				String res = (String)args[0];
				BufferedImage img = Resource.loadimg(res);
				int num = -1;
				int ca = 2;
				Coord drag = null;
				if((Integer)args[1] != 0)
					drag = (Coord)args[ca++];
				if(args.length > ca)
					num = (Integer)args[ca++];
				return(new Item(c, img, parent, drag, num));
			}
		});
	}
	
	void render() {
		clear();
		Graphics g = graphics();
		if(dm)
			g.drawImage(sh, shoff.x, shoff.y, null);
		g.drawImage(img, 0, 0, null);
		if(num >= 0) {
			g.setColor(Color.BLACK);
			Utils.aligntext(g, Integer.toString(num), Utils.imgsz(img), 1, 1);
		}
		update();
	}
	
	static BufferedImage makesh(BufferedImage img) {
		Coord sz = Utils.imgsz(img);
		BufferedImage sh = new BufferedImage(sz.x, sz.y, BufferedImage.TYPE_INT_ARGB);
		for(int y = 0; y < sz.y; y++) {
			for(int x = 0; x < sz.x; x++) {
				int c = img.getRGB(x, y);
				if((c & 0xff000000) != 0)
					sh.setRGB(x, y, 0x80000000);
				else
					sh.setRGB(x, y, 0);
			}
		}
		return(sh);
	}
	
	public Item(Coord c, BufferedImage img, Widget parent, Coord drag, int num) {
		super(c, Utils.imgsz(img).add(shoff), parent);
		this.img = img;
		this.num = num;
		if(drag == null) {
			dm = false;
		} else {
			dm = true;
			doff = drag;
			ui.grabmouse(this);
			this.c = ui.mc.add(doff.inv());
		}
		sh = makesh(img);
		render();
	}

	public Item(Coord c, String res, Widget parent, Coord drag, int num) {
		this(c, Resource.loadimg(res), parent, drag, num);
	}

	public Item(Coord c, BufferedImage img, Widget parent, Coord drag) {
		this(c, img, parent, drag, -1);
	}
	
	public Item(Coord c, String res, Widget parent, Coord drag) {
		this(c, Resource.loadimg(res), parent, drag);
	}

	public boolean findrelevant(Widget w, Coord c) {
		for(Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
			Coord cc = w.xlate(wdg.c, true);
			if(c.isect(cc, wdg.sz)) {
				if(findrelevant(wdg, c.add(cc.inv())))
					return(true);
			}
		}
		if(w instanceof DTarget) {
			((DTarget)w).drop(c, c.add(doff.inv()));
			return(true);
		}
		return(false);
	}
	
	public void uimsg(String name, Object... args)  {
		if(name == "num") {
			num = (Integer)args[0];
			render();
		}
	}
	
	public boolean mousedown(Coord c, int button) {
		if(!dm) {
			if(button == 1) {
				wdgmsg("take", c);
				return(true);
			} else if(button == 3) {
				wdgmsg("iact", c);
				return(true);
			}
		} else {
			if(button == 1) {
				findrelevant(parent, c.add(this.c));
			}
			return(true);
		}
		return(false);
	}
	
	public void mousemove(Coord c) {
		if(dm)
			this.c = this.c.add(c.add(doff.inv()));
	}
}
