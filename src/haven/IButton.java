package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class IButton extends SSWidget {
	BufferedImage up, down;
	boolean a = false;
	
	static {
		Widget.addtype("ibtn", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new IButton(c, parent, Resource.loadimg((String)args[0]), Resource.loadimg((String)args[1])));
			}
		});
	}
	
	public IButton(Coord c, Widget parent, BufferedImage up, BufferedImage down) {
		super(c, Utils.imgsz(up), parent);
		this.up = up;
		this.down = down;
		render();
	}
	
	public void render() {
		Graphics g = graphics();
		g.drawImage(a?down:up, 0, 0, null);
		update();
	}

	public boolean checkhit(Coord c) {
		int cl = up.getRGB(c.x, c.y);
		return(Utils.rgbm.getAlpha(cl) >= 128);
	}
	
	public boolean mousedown(Coord c, int button) {
		if(button != 1)
			return(false);
		if(!checkhit(c))
			return(false);
		a = true;
		ui.grabmouse(this);
		render();
		return(true);
	}
	
	public boolean mouseup(Coord c, int button) {
		if(a && button == 1) {
			a = false;
			ui.grabmouse(null);
			if(c.isect(new Coord(0, 0), sz) && checkhit(c))
				wdgmsg("activate");
			render();
			return(true);
		}
		return(false);
	}
}
