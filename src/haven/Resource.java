package haven;

import java.util.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;

public class Resource {
	private static File basedir = new File("D:\\Haven\\src\\res");
	private static Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private static Map<String, Sprite> sprites = new HashMap<String, Sprite>();
	private static Map<String, Anim> anims = new HashMap<String, Anim>();
	
	public static InputStream getres(String name) {
		String fn = "";
		for(int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if(c == '/')
				fn += File.separator;
			else
				fn += c;
		}
		File f = new File(basedir, fn);
		try {
			return(new FileInputStream(f));
		} catch(FileNotFoundException e) {
			throw(new RuntimeException(e));
		}		
	}
	
	public static Reader gettext(String name) {
		try {
			return(new InputStreamReader(getres(name), "utf-8"));
		} catch(UnsupportedEncodingException e) {
			throw(new RuntimeException(e));
		}
	}
	
	public static byte[] loadres(String name) {
		InputStream is = getres(name);
		int len = 0;
		byte[] buf = new byte[1024];
		byte[] buf2 = new byte[1024];
		while(true) {
			int ret;
			try {
				ret = is.read(buf2);
			} catch(IOException e) {
				throw(new RuntimeException(e));
			}
			if(ret < 0)
				break;
			if(buf.length < len + ret) {
				byte[] buf3 = new byte[(len + ret) * 2];
				System.arraycopy(buf, 0, buf3, 0, len);
				buf = buf3;
			}
			System.arraycopy(buf2, 0, buf, len, ret);
			len += ret;
		}
		buf2 = new byte[len];
		System.arraycopy(buf, 0, buf2, 0, len);
		return(buf2);
	}
	
	public static BufferedImage loadimg(String name) {
		synchronized(images) {
			if(images.containsKey(name))
				return(images.get(name));
			try {
				BufferedImage ni = ImageIO.read(getres(name));
				images.put(name, ni);
				return(ni);
			} catch(IOException e) {
				throw(new RuntimeException(e));
			}
		}
	}
	
	public static boolean detectgay(BufferedImage i) {
		Coord sz = Utils.imgsz(i);
		for(int y = 0; y < sz.y; y++) {
			for(int x = 0; x < sz.x; x++) {
				if((i.getRGB(x, y) & 0x00ffffff) == 0x00ff0080)
					return(true);
			}
		}
		return(false);
	}
	
	public static Sprite loadsprite(String name) {
		synchronized(sprites) {
			if(sprites.containsKey(name))
				return(sprites.get(name));
			try {
				InputStream in = getres(name);
				String sig = "Haven Sprite 1";
				byte[] sigb = new byte[sig.length()];
				in.read(sigb);
				if(!sig.equals(new String(sigb)))
					throw(new FormatException("Illegal sprite format", name));
				byte[] ib = new byte[13];
				in.read(ib);
				Coord cc = new Coord(Utils.uint16d(ib, 0), Utils.uint16d(ib, 2));
				int prio = Utils.sb(ib[12]);
				BufferedImage frame = ImageIO.read(in);
				if(frame == null)
					throw(new RuntimeException("Bad image data in " + name));
				Sprite spr = new Sprite(frame, cc, prio);
				sprites.put(name, spr);
				return(spr);
			} catch(IOException e) {
				throw(new RuntimeException(e));
			}
		}
	}
	
	public static Anim loadanim(String name) {
		synchronized(anims) {
			if(anims.containsKey(name))
				return(anims.get(name));
			try {
				InputStream in = getres(name);
				String sig = "Haven Animation 1";
				byte[] sigb = new byte[sig.length()];
				in.read(sigb);
				if(!sig.equals(new String(sigb)))
					throw(new FormatException("Illegal animation format", name));
				byte[] ib = new byte[16];
				in.read(ib);
				Coord sz = new Coord(Utils.uint16d(ib, 0), Utils.uint16d(ib, 2));
				Coord cc = new Coord(Utils.uint16d(ib, 4), Utils.uint16d(ib, 6));
				List<BufferedImage> frames = new ArrayList<BufferedImage>();
				List<Integer> prio = new ArrayList<Integer>();
				List<Integer> dur = new ArrayList<Integer>();
				while(true) {
					byte[] fb = new byte[7];
					in.read(fb);
					int len = Utils.int32d(fb, 3);
					if(len == 0)
						break;
					prio.add((int)Utils.sb(fb[2]));
					dur.add(Utils.uint16d(fb, 0));
					byte[] fdb = new byte[len];
					in.read(fdb);
					BufferedImage frame = ImageIO.read(new ByteArrayInputStream(fdb));
					if(frame == null)
						throw(new RuntimeException("Bad frame in " + name));
					frames.add(frame);
				}
				Anim anim = new Anim(frames, prio, dur, cc, sz);
				anims.put(name, anim);
				return(anim);
			} catch(IOException e) {
				throw(new RuntimeException(e));
			}
		}
	}
}
