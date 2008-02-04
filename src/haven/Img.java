package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class Img extends Widget {
	private BufferedImage img;
	
	static {
		Widget.addtype("img", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Img(c, Resource.loadimg((String)args[0]), parent));
			}
		});
	}
	
	public void draw(Graphics g) {
		synchronized(img) {
			g.drawImage(img, 0, 0, null);
		}
	}
	
	public Img(Coord c, BufferedImage img, Widget parent) {
		super(c, Utils.imgsz(img), parent);
		this.img = img;
	}
	
	public void uimsg(String name, Object... args) {
		if(name == "ch") {
			img = Resource.loadimg((String)args[0]);
		}
	}
}
