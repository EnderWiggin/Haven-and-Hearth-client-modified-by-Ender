package haven;

import java.awt.Color;

public class FlowerMenu extends Widget {
	public static int barda = 9;
	public static Color pink = new Color(255, 0, 128);
	static int r = 50;
	String[] opts;
	
	static {
		Widget.addtype("sm", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				String[] opts = new String[args.length];
				for(int i = 0; i < args.length; i++)
					opts[i] = (String)args[i];
				return(new FlowerMenu(c, parent, opts));
			}
		});
	}
	
	public FlowerMenu(Coord c, Widget parent, String... options) {
		super(c.add(new Coord(-r - 100, -r - 20)), new Coord(r * 2 + 200, r * 2 + 40), parent);
		opts = options;
		ui.mousegrab = this;
	}
	
	public void draw(java.awt.Graphics g) {
		double a;
		int i;
		Utils.AA(g);
		for(a = Math.PI / 2, i = 0; i < opts.length; a += Math.PI * 2 / opts.length, i++) {
			int x = sz.x / 2 + (int)(Math.cos(a) * r);
			int y = sz.y / 2 - (int)(Math.sin(a) * r);
			java.awt.FontMetrics m = g.getFontMetrics();
			java.awt.geom.Rectangle2D ts = m.getStringBounds(opts[i], g);
			g.setColor(pink);
			Coord os = new Coord((int)ts.getWidth() * 2, (int)ts.getHeight() * 2);
			g.fillOval(x - os.x / 2, y - os.y / 2, os.x, os.y);
			g.setColor(Color.BLACK);
			g.drawString(opts[i], (int)(x - ts.getWidth() / 2), (int)(y + m.getAscent() - ts.getHeight() / 2));
		}
	}
	
	public boolean mousedown(Coord c, int button) {
		
		return(true);
	}
}
