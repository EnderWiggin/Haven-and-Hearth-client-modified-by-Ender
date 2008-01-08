package haven;

import java.util.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;

public class Resource {
	private static Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private static Map<String, Sprite> sprites = new HashMap<String, Sprite>();
	private static Map<String, Anim> anims = new HashMap<String, Anim>();
	
	public static InputStream getres(String name) {
		InputStream s = Resource.class.getResourceAsStream("/res/" + name);
		if(s == null)
			throw(new RuntimeException("Could not find resource: " + name));
		return(s);
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
				byte[] ib = new byte[12];
				in.read(ib);
				Coord cc = new Coord(Utils.uint16d(ib, 0), Utils.uint16d(ib, 2));
				Sprite spr = new Sprite(ImageIO.read(in), cc);
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
				List<Integer> dur = new ArrayList<Integer>();
				while(true) {
					byte[] fb = new byte[6];
					in.read(fb);
					int len = Utils.int32d(fb, 2);
					if(len == 0)
						break;
					dur.add(Utils.uint16d(fb, 0));
					byte[] fdb = new byte[len];
					in.read(fdb);
					java.security.MessageDigest md = null;
					try {
						md = java.security.MessageDigest.getInstance("MD5");
					} catch(java.security.NoSuchAlgorithmException e) {}
					byte[] d = md.digest(fdb);
					for(byte b : d)
						System.out.format("%02X", b);
					System.out.println();
					frames.add(ImageIO.read(new ByteArrayInputStream(fdb)));
				}
				Anim anim = new Anim(frames, dur, cc, sz);
				anims.put(name, anim);
				return(anim);
			} catch(IOException e) {
				throw(new RuntimeException(e));
			}
		}
	}
}
