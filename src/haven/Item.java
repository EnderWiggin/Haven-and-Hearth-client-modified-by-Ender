package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class Item extends SSWidget {
	public static int barda = 47;
	static Coord shoff = new Coord(1, 3);
	int state = 0;
	Coord doff;
	BufferedImage img, sh;
	
	static {
		Widget.addtype("item", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Item(c, Resource.loadsprite((String)args[0]).img, parent, (Integer)args[1] != 0));
			}
		});
	}
	
	void render() {
		clear();
		Graphics g = surf.getGraphics();
		if(state != 0)
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
	
	public Item(Coord c, BufferedImage img, Widget parent, boolean d) {
		super(c, Utils.imgsz(img).add(shoff), parent, true);
		this.img = img;
		state = d?2:0;
		sh = makesh(img);
		render();
	}
	
	public boolean mousedown(Coord c, int button) {
		if(state == 0) {
			if(button == 1) {
				this.c = rootpos();
				unlink();
				parent = ui.root;
				link();
				ui.grabmouse(this);
				state = 1;
				doff = c;
				render();
				return(true);
			}
		} else if(state == 2) {
			if(button == 1) {
				wdgmsg("drop");
			}
			return(true);
		}
		return(false);
	}
	
	public boolean mouseup(Coord c, int button) {
		if(state == 1) {
			if(button == 1) {
				state = 2;
				render();
				return(true);
			}
		}
		return(false);
	}
	
	public void mousemove(Coord c) {
		if(state != 0)
			this.c = this.c.add(c.add(doff.inv()));
	}
}
