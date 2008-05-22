package haven;

import java.util.*;

@SuppressWarnings("serial")
public class Message implements java.io.Serializable {
	public static final int RMSG_NEWWDG = 0;
	public static final int RMSG_WDGMSG = 1;
	public static final int RMSG_DSTWDG = 2;
	public static final int RMSG_MAPIV = 3;
	public static final int RMSG_GLOBLOB = 4;
	public static final int RMSG_PAGINAE = 5;
	public static final int RMSG_RESID = 6;
	public static final int RMSG_PARTY = 7;
	
	public static final int T_END = 0;
	public static final int T_INT = 1;
	public static final int T_STR = 2;
	public static final int T_COORD = 3;
	
	public int type;
	public byte[] blob;
	public long last = 0;
	public int seq;
	int off = 0;
	
	public Message(int type, byte[] blob) {
		this.type = type;
		this.blob = blob;
	}
	
	public Message(int type, byte[] blob, int offset, int len) {
		this.type = type;
		this.blob = new byte[len];
		System.arraycopy(blob, offset, this.blob, 0, len);
	}
	
	public Message(int type) {
		this.type = type;
		blob = new byte[0];
	}
	
	public void addbytes(byte[] src) {
		byte[] n = new byte[blob.length + src.length];
		System.arraycopy(blob, 0, n, 0, blob.length);
		System.arraycopy(src, 0, n, blob.length, src.length);
		blob = n;
	}
	
	public void adduint8(int num) {
		addbytes(new byte[] {Utils.sb(num)});
	}
	
	public void adduint16(int num) {
		byte[] buf = new byte[2];
		Utils.uint16e(num, buf, 0);
		addbytes(buf);
	}
	
	public void addint32(int num) {
		byte[] buf = new byte[4];
		Utils.int32e(num, buf, 0);
		addbytes(buf);
	}
	
	public void addstring(String str) {
		byte[] buf;
		try {
			buf = str.getBytes("utf-8");
		} catch(java.io.UnsupportedEncodingException e) {
			throw(new RuntimeException(e));
		}
		addbytes(buf);
		addbytes(new byte[] {0});
	}
	
	public void addcoord(Coord c) {
		addint32(c.x);
		addint32(c.y);
	}
	
	public void addlist(Object... args) {
		for(Object o : args) {
			if(o instanceof Integer) {
				adduint8(T_INT);
				addint32(((Integer)o).intValue());
			} else if(o instanceof String) {
				adduint8(T_STR);
				addstring((String)o);
			} else if(o instanceof Coord) {
				adduint8(T_COORD);
				addcoord((Coord)o);
			}
		}
	}
	
	public boolean eom() {
		return(off >= blob.length);
	}
	
	public int uint8() {
		return(Utils.ub(blob[off++]));
	}
	
	public int uint16() {
		off += 2;
		return(Utils.uint16d(blob, off - 2));
	}
	
	public int int32() {
		off += 4;
		return(Utils.int32d(blob, off - 4));
	}
	
	public String string() {
		int[] ob = new int[] {off};
		String ret = Utils.strd(blob, ob);
		off = ob[0];
		return(ret);
	}
	
	public Coord coord() {
		return(new Coord(int32(), int32()));
	}
	
	public Object[] list() {
		ArrayList<Object> ret = new ArrayList<Object>();
		while(true) {
			if(off >= blob.length)
				break;
			int t = uint8();
			if(t == T_END)
				break;
			else if(t == T_INT)
				ret.add(int32());
			else if(t == T_STR)
				ret.add(string());
			else if(t == T_COORD)
				ret.add(coord());
		}
		return(ret.toArray());
	}
	
	public String toString() {
		String ret = "";
		for(byte b : blob) {
			ret += String.format("%02x ", b);
		}
		return("Message(" + type + "): " + ret);
	}
}
