package haven;

import java.io.*;

public class Utils {
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
