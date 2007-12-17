package haven;

import java.awt.RenderingHints;
import java.io.*;
import java.util.prefs.*;
import java.awt.Graphics;

public class Utils {
	private static Preferences prefs = null;
	public static java.awt.image.ColorModel rgbm = java.awt.image.ColorModel.getRGBdefault();
	
	static void centertext(Graphics g, String text, Coord c) {
		java.awt.FontMetrics m = g.getFontMetrics();
		java.awt.geom.Rectangle2D ts = m.getStringBounds(text, g);
		g.drawString(text, (int)(c.x - ts.getWidth() / 2), (int)(c.y + m.getAscent() - ts.getHeight() / 2));
	}
	
	static void AA(Graphics g) {
		java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);		
	}
	
	static synchronized String getpref(String prefname, String def) {
		if(prefs == null)
			prefs = Preferences.userNodeForPackage(Utils.class);
		return(prefs.get(prefname, def));
	}
	
	static synchronized void setpref(String prefname, String val) {
		if(prefs == null)
			prefs = Preferences.userNodeForPackage(Utils.class);
		prefs.put(prefname, val);
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
