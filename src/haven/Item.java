package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class Item extends SSWidget {
	public static int barda = 47;
	static Coord shoff = new Coord(1, 3);
	boolean dm = false;
	Coord doff;
	BufferedImage img, sh;
	
	static {
		Widget.addtype("item", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				BufferedImage img;
				String res = (String)args[0];
				if(res.substring(res.length() - 4).equals(".spr"))
					img = Resource.loadsprite(res).img;
				else
					img = Resource.loadimg(res);
				if((Integer)args[1] != 0)
					return(new Item(c, img, parent, (Coord)args[2]));
				else
					return(new Item(c, img, parent, null));
			}
		});
	}
	
	void render() {
		clear();
		Graphics g = surf.getGraphics();
		if(dm)
			g.drawImage(sh, shoff.x, shoff.y, null);
		g.drawImage(img, 0, 0, null);
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
	
	public Item(Coord c, BufferedImage img, Widget parent, Coord drag) {
		super(c, Utils.imgsz(img).add(shoff), parent, drag != null);
		this.img = img;
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

	public boolean findrelevant(Widget w, Coord c) {
		if(w instanceof Inventory) {
			wdgmsg("drop", c.add(doff.inv()).add(new Coord(15, 15)).div(new Coord(29, 29)));
			return(true);
		} else if(w instanceof MapView) {
			wdgmsg("mapdrop");
			return(true);
		} else {
			for(Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
				Coord cc = w.xlate(wdg.c, true);
				if(c.isect(cc, wdg.sz)) {
					if(findrelevant(wdg, c.add(cc.inv())))
						return(true);
				}
			}
		}
		return(false);
	}
	
	public boolean mousedown(Coord c, int button) {
		if(!dm) {
			if(button == 1) {
				wdgmsg("take", c);
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
