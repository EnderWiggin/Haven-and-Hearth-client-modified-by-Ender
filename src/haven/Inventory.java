package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class Inventory extends Widget implements DTarget {
	BufferedImage invsq;
	Coord isz;

	static {
		Widget.addtype("inv", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Inventory(c, (Coord)args[0], parent));
			}
		});
	}

	public void draw(Graphics g) {
		for(int y = 0; y < isz.y; y++) {
			for(int x = 0; x < isz.x; x++) {
				g.drawImage(invsq, x * (invsq.getWidth() - 1), y * (invsq.getHeight() - 1), null);
			}
		}
		super.draw(g);
	}
	
	public Inventory(Coord c, Coord sz, Widget parent) {
		super(c, Utils.imgsz(Resource.loadimg("gfx/hud/invsq.gif")).add(new Coord(-1, -1)).mul(sz).add(new Coord(1, 1)), parent);
		isz = sz;
		invsq = Resource.loadimg("gfx/hud/invsq.gif");
	}
	
	public void drop(Coord cc, Coord ul) {
		wdgmsg("drop", ul.add(new Coord(15, 15)).div(Utils.imgsz(invsq)));
	}
}
