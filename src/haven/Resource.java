package haven;

import java.util.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;

public class Resource {
	private static File basedir = new File("D:\\Haven\\src\\res");
	private static Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private static Map<String, Sprite> sprites = new HashMap<String, Sprite>();
	
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
					throw(new RuntimeException("Illegal sprite format"));
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
}
