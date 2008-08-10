package haven;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Text {
	public static final Foundry std;
	public BufferedImage img;
	public final String text;
	private FontMetrics m;
	private Tex tex;
	public static final Color black = Color.BLACK;
	public static final Color white = Color.WHITE;
	
	static {
		std = new Foundry(new Font("SansSerif", Font.PLAIN, 10));
	}
	
	public static class Foundry {
		private FontMetrics m;
		Font font;
		Color defcol;
		
		public Foundry(Font f, Color defcol) {
			font = f;
			this.defcol = defcol;
			BufferedImage junk = TexI.mkbuf(new Coord(10, 10));
			Graphics g = junk.getGraphics();
			g.setFont(f);
			m = g.getFontMetrics();
		}
		
		public Foundry(Font f) {
			this(f, Color.WHITE);
		}
		
		public Text render(String text, Color c) {
			Text t = new Text(text);
			Rectangle2D b = font.getStringBounds(text, m.getFontRenderContext());
			t.img = TexI.mkbuf(new Coord((int)b.getWidth(), m.getHeight()));
			Graphics g = t.img.createGraphics();
			g.setFont(font);
			g.setColor(c);
			t.m = g.getFontMetrics();
			g.drawString(text, 0, t.m.getAscent());
			g.dispose();
			return(t);
		}
		
		public Text render(String text) {
			return(render(text, defcol));
		}
	}
	
	private Text(String text) {
		this.text = text;
	}
	
	public Coord sz() {
		return(Utils.imgsz(img));
	}
	
	public Coord base() {
		return(new Coord(0, m.getAscent()));
	}
	
	public static Text render(String text, Color c) {
		return(std.render(text, c));
	}
	
	public static Text renderf(Color c, String text, Object... args) {
		return(std.render(String.format(text, args), c));
	}
	
	public static Text render(String text) {
		return(render(text, Color.WHITE));
	}
	
	public Tex tex() {
		if(tex == null)
			tex = new TexI(img);
		return(tex);
	}
}
