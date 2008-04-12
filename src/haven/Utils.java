package haven;

import java.awt.RenderingHints;
import java.io.*;
import java.util.prefs.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Utils {
	private static Preferences prefs = null;
	public static java.awt.image.ColorModel rgbm = java.awt.image.ColorModel.getRGBdefault();

	static Coord imgsz(BufferedImage img) {
		return(new Coord(img.getWidth(), img.getHeight()));
	}
	
	static void drawgay(BufferedImage t, BufferedImage img, Coord c) {
		Coord sz = imgsz(img);
		for(int y = 0; y < sz.y; y++) {
			for(int x = 0; x < sz.x; x++) {
				int p = img.getRGB(x, y);
				if(Utils.rgbm.getAlpha(p) > 128) {
					if((p & 0x00ffffff) == 0x00ff0080)
						t.setRGB(x + c.x, y + c.y, 0);
					else
						t.setRGB(x + c.x, y + c.y, p);
				}
			}
		}
	}
	
	static int drawtext(Graphics g, String text, Coord c) {
		java.awt.FontMetrics m = g.getFontMetrics();
		g.drawString(text, c.x, c.y + m.getAscent());
		return(m.getHeight());
	}
	
	static Coord textsz(Graphics g, String text) {
		java.awt.FontMetrics m = g.getFontMetrics();
		java.awt.geom.Rectangle2D ts = m.getStringBounds(text, g);
		return(new Coord((int)ts.getWidth(), (int)ts.getHeight()));
	}
	
	static void aligntext(Graphics g, String text, Coord c, double ax, double ay) {
		java.awt.FontMetrics m = g.getFontMetrics();
		java.awt.geom.Rectangle2D ts = m.getStringBounds(text, g);
		g.drawString(text, (int)(c.x - ts.getWidth() * ax), (int)(c.y + m.getAscent() - ts.getHeight() * ay));
	}
	
	static ThreadGroup tg() {
		return(Thread.currentThread().getThreadGroup());
	}

	static void line(Graphics g, Coord c1, Coord c2) {
		g.drawLine(c1.x, c1.y, c2.x, c2.y);
	}
	
	static void AA(Graphics g) {
		java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);		
	}
	
	static synchronized String getpref(String prefname, String def) {
		try {
			if(prefs == null)
				prefs = Preferences.userNodeForPackage(Utils.class);
			return(prefs.get(prefname, def));
		} catch(SecurityException e) {
			return(def);
		}
	}
	
	static synchronized void setpref(String prefname, String val) {
		try {
			if(prefs == null)
				prefs = Preferences.userNodeForPackage(Utils.class);
			prefs.put(prefname, val);
		} catch(SecurityException e) {
		}
	}
	
	static int ub(byte b) {
		if(b < 0)
			return(256 + b);
		else
			return(b);
	}
	
	static byte sb(int b) {
		if(b > 127)
			return((byte)(-256 + b));
		else
			return((byte)b);
	}
	
	static int uint16d(byte[] buf, int off) {
		return(ub(buf[off]) + (ub(buf[off + 1]) * 256));
	}
	
	static int int16d(byte[] buf, int off) {
		int u = uint16d(buf, off);
		if(u > 32767)
			return(-65536 + u);
		else
			return(u);
	}
	
	static long uint32d(byte[] buf, int off) {
		return(ub(buf[off]) + (ub(buf[off + 1]) * 256) + (ub(buf[off + 2]) * 65536) + (ub(buf[off + 3]) * 16777216));
	}
	
	static void uint32e(long num, byte[] buf, int off) {
		buf[off] = sb((int)(num & 0xff));
		buf[off + 1] = sb((int)((num & 0xff00) >> 8));
		buf[off + 2] = sb((int)((num & 0xff0000) >> 16));
		buf[off + 3] = sb((int)((num & 0xff000000) >> 24));
	}
	
	static int int32d(byte[] buf, int off) {
		long u = uint32d(buf, off);
		if(u > Integer.MAX_VALUE)
			return((int)((((long)Integer.MIN_VALUE) * 2) - u));
		else
			return((int)u);
	}
	
	static void int32e(int num, byte[] buf, int off) {
		if(num < 0)
			uint32e(0x100000000L + ((long)num), buf, off);
		else
			uint32e(num, buf, off);
	}
	
	static void uint16e(int num, byte[] buf, int off) {
		buf[off] = sb(num & 0xff);
		buf[off + 1] = sb((num & 0xff00) >> 8);
	}
	
	static String strd(byte[] buf, int[] off) {
		int i;
		for(i = off[0]; buf[i] != 0; i++);
		String ret;
		try {
			ret = new String(buf, off[0], i - off[0], "utf-8");
		} catch(UnsupportedEncodingException e) {
			throw(new RuntimeException(e));
		}
		off[0] = i + 1;
		return(ret);
	}
}
