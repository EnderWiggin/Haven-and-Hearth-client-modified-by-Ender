package haven;

import java.awt.image.BufferedImage;

public class Img extends SSWidget {
	static {
		Widget.addtype("img", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Img(c, Resource.loadimg((String)args[0]), parent));
			}
		});
	}
	
	public Img(Coord c, BufferedImage img, Widget parent) {
		super(c, Utils.imgsz(img), parent, false);
		surf.getGraphics().drawImage(img, 0, 0, null);
	}
}
