package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class Skillwindow extends Window {
	Label exptext;
	int skx = 0;
	Coord sksz = new Coord(24, 24); 
	
	static {
		Widget.addtype("skill", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Skillwindow(c, parent, (Integer)args[0]));
			}
		});
	}
	
	public Skillwindow(Coord c, Widget parent, int exp) {
		super(c, new Coord(150, 100), parent);
		exptext = new Label(Coord.z, this, "Learning points: " + exp);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "exp") {
			exptext.settext("Learning points: " + (Integer)args[0]);
		} else if(msg == "ask") {
			BufferedImage oski = Resource.loadimg("gfx/hud/skills/" + (String)args[0]);
			BufferedImage ski = TexI.mkbuf(sksz);
			Graphics g = ski.getGraphics();
			g.drawImage(oski, 0, 0, sksz.x, sksz.y, null);
			new Img(new Coord(skx, 20), new TexI(ski), this);
			skx += Utils.imgsz(ski).x;
		}
	}
}
