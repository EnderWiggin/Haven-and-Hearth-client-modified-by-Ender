package haven;

import static java.lang.Math.PI;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Cal extends SSWidget {
	public static final double hbr = 23;
	static BufferedImage bg = Resource.loadimg("gfx/hud/calendar/setting.gif");
	static BufferedImage dlnd = Resource.loadimg("gfx/hud/calendar/dayscape.gif");
	static BufferedImage dsky = Resource.loadimg("gfx/hud/calendar/daysky.gif");
	static BufferedImage nlnd = Resource.loadimg("gfx/hud/calendar/nightscape.gif");
	static BufferedImage nsky = Resource.loadimg("gfx/hud/calendar/nightsky.gif");
	static BufferedImage sun = Resource.loadimg("gfx/hud/calendar/sun.png");
	static BufferedImage moon[];
	long update = 0;
	Astronomy current;
	
	static {
		moon = new BufferedImage[8];
		for(int i = 0; i < moon.length; i++)
			moon[i] = Resource.loadimg(String.format("gfx/hud/calendar/m%02d.png", i));
		Widget.addtype("cal", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Cal(c, parent));
			}
		});
	}
	
	private void render() {
		Astronomy a = current = ui.sess.glob.ast;
		clear();
		Graphics g = graphics();
		g.drawImage(bg, 0, 0, null);
		g.drawImage(a.night?nsky:dsky, 0, 0, null);
		int mp = (int)((a.mp + (0.5 / (double)moon.length)) * (double)moon.length);
		BufferedImage moon = this.moon[mp];
		Coord mc = Coord.sc((a.dt + 0.25) * 2 * PI, hbr).add(sz.div(2)).add(Utils.imgsz(moon).div(2).inv());
		Coord sc = Coord.sc((a.dt + 0.75) * 2 * PI, hbr).add(sz.div(2)).add(Utils.imgsz(sun).div(2).inv());
		g.drawImage(moon, mc.x, mc.y, null);
		g.drawImage(sun, sc.x, sc.y, null);
		g.drawImage(a.night?nlnd:dlnd, 0, 0, null);
		update();
		update = System.currentTimeMillis();
	}
	
	public Cal(Coord c, Widget parent) {
		super(c, Utils.imgsz(bg), parent);
		render();
	}
	
	public void draw(GOut g) {
		if(!current.equals(ui.sess.glob.ast))
			render();
		super.draw(g);
	}
}
