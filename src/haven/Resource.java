package haven;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;

public class Resource implements Comparable<Resource>, Serializable {
	private static File basedir = new File("Y:\\res");
	public static URL baseurl = null;
	private static Map<String, Resource> cache = new TreeMap<String, Resource>();
	private static Loader loader;
	private static Map<String, Class<? extends Layer>> ltypes = new TreeMap<String, Class<? extends Layer>>();
	public static Class<Image> imgc = Image.class;
	public static Class<Neg> negc = Neg.class;
	public static Class<Anim> animc = Anim.class;
	
	private LoadException error;
	private Collection<? extends Layer> layers = new LinkedList<Layer>();
	final String name;
	int ver;
	boolean loading;

	private Resource(String name, int ver) {
		this.name = name;
		this.ver = ver;
		error = null;
		loading = true;
	}
	
	public static Resource load(String name, int ver) {
		Resource res;
		synchronized(cache) {
			res = cache.get(name);
			if(ver != -1) {
				if(res.ver < ver) {
					res = null;
					cache.remove(name);
				} else if(res.ver > ver) {
					throw(new RuntimeException("Weird version number on " + name));
				}
			}
			if(res != null)
				return res;
			res = new Resource(name, ver);
			cache.put(name, res);
		}
		synchronized(Resource.class) {
			if(loader == null) {
				loader = new Loader();
				loader.start();
			}
			loader.load(res);
		}
		return(res);
	}
	
	public static Resource load(String name) {
		return(load(name, -1));
	}
	
	public void loadwaitint() throws InterruptedException {
		synchronized(this) {
			while(loading) {
				wait();
			}
		}
	}
	
	public void loadwait() {
		boolean i = false;
		synchronized(this) {
			while(loading) {
				try {
					wait();
				} catch(InterruptedException e) {
					i = true;
				}
			}
		}
		if(i)
			Thread.currentThread().interrupt();
	}
	
	private static class Loader extends Thread {
		private Queue<Resource> queue = new LinkedList<Resource>();
		
		public Loader() {
			super(Utils.tg(), "Haven resource loader");
			setDaemon(true);
		}
		
		public void run() {
			try {
				while(true) {
					Resource cur;
					synchronized(queue) {
						while((cur = queue.poll()) == null)
							queue.wait();
					}
					synchronized(cur) {
						try {
							try {
								handle(cur);
							} catch(IOException e) {
								cur.error = new LoadException(e, cur);
							} catch(LoadException e) {
								cur.error = e;
							}
						} finally {
							cur.loading = false;
							cur.notifyAll();
						}
					}
					cur = null;
				}
			} catch(InterruptedException e) {}
		}
		
		private void handle(Resource res) throws IOException {
			try {
				res.load(getres(res.name));
				return;
			} catch(LoadException e) {}
			URL resurl;
			try {
				resurl = new URL(baseurl, res.name);
			} catch(MalformedURLException e) {
				throw(new LoadException("Could not construct res URL", e, res));
			}
			URLConnection c = resurl.openConnection();
			c.connect();
			res.load(c.getInputStream());
		}
		
		public void load(Resource res) { 
			synchronized(queue) {
				queue.add(res);
				queue.notifyAll();
			}
		}
	}
	
	@SuppressWarnings("serial")
	public static class LoadException extends RuntimeException {
		public Resource res;
		
		public LoadException(String msg, Resource res) {
			super(msg);
			this.res = res;
		}

		public LoadException(String msg, Throwable cause, Resource res) {
			super(msg, cause);
			this.res = res;
		}
		
		public LoadException(Throwable cause, Resource res) {
			super(cause);
			this.res = res;
		}
	}
	
	public static Coord cdec(byte[] buf, int off) {
		return(new Coord(Utils.int16d(buf, off), Utils.int16d(buf, off + 2)));
	}
	
	public abstract class Layer implements Serializable {
		public abstract void init();
	}
	
	public class Image extends Layer implements Serializable{
		transient BufferedImage img;
		transient private Tex tex;
		final int z;
		final boolean l;
		final int id;
		Coord o;
		
		public Image(byte[] buf) {
			z = Utils.int16d(buf, 0);
			l = buf[2] != 0;
			id = Utils.int16d(buf, 3);
			o = cdec(buf, 5);
			try {
				img = ImageIO.read(new ByteArrayInputStream(buf, 9, buf.length - 9));
			} catch(IOException e) {
				throw(new RuntimeException(e));
			}
		}
		
		public synchronized Tex tex() {
			if(tex != null)
				return(tex);
			tex = new TexI(img);
			return(tex);
		}
		
		public void init() {}
	}
	static {ltypes.put("image", Image.class);}
	
	public class Neg extends Layer implements Serializable {
		Coord cc;
		Coord bc, bs;
		Coord sz;
		
		public Neg(byte[] buf) {
			cc = cdec(buf, 0);
			bc = cdec(buf, 4);
			bs = cdec(buf, 8);
			sz = cdec(buf, 12);
			bc = MapView.s2m(bc.add(cc.inv()));
			bs = MapView.s2m(bs.add(cc.inv())).add(bc.inv());
		}
		
		public void init() {}
	}
	static {ltypes.put("neg", Neg.class);}
	
	public class Anim extends Layer implements Serializable {
		Image[] f;
		private int[] ids;
		int d;
		
		public Anim(byte[] buf) {
			d = Utils.uint16d(buf, 0);
			ids = new int[Utils.uint16d(buf, 2)];
			if(buf.length - 4 != ids.length * 2)
				throw(new LoadException("Invalid anim descriptor in " + name, Resource.this));
			for(int i = 0; i < ids.length; i++)
				ids[i] = Utils.int16d(buf, 4 + (i * 2));
		}
		
		public void init() {
			f = new Image[ids.length];
			for(int i = 0; i < ids.length; i++) {
				for(Image img : layers(Image.class)) {
					if(img.id == ids[i])
						f[i] = img;
				}
			}
		}
	}
	static {ltypes.put("anim", Anim.class);}
	
	private void readall(InputStream in, byte[] buf) throws IOException {
		int ret, off = 0;
		while(off < buf.length) {
			ret = in.read(buf, off, buf.length - off);
			if(ret < 0)
				throw(new LoadException("Incomplete resource at " + name, this));
			off += ret;
		}
	}
	
	public <L extends Layer> Collection<L> layers(Class<L> cl) {
		checkerr();
		Collection<L> ret = new LinkedList<L>();
		for(Layer l : layers) {
			if(cl.isInstance(l))
				ret.add(cl.cast(l));
		}
		return(ret);
	}
	
	public <L extends Layer> L layer(Class<L> cl) {
		checkerr();
		for(Layer l : layers) {
			if(cl.isInstance(l))
				return(cl.cast(l));
		}
		return(null);
	}
	
	public int compareTo(Resource other) {
		checkerr();
		int nc = name.compareTo(other.name);
		if(nc != 0)
			return(nc);
		if(ver != other.ver)
			return(ver - other.ver);
		if(other != this)
			throw(new RuntimeException("Resource identity broken!"));
		return(0);
	}
	
	private void load(InputStream in) throws IOException {
		String sig = "Haven Resource 1";
		byte buf[] = new byte[sig.length()];
		readall(in, buf);
		if(!sig.equals(new String(buf)))
			throw(new LoadException("Invalid res signature", this));
		buf = new byte[2];
		readall(in, buf);
		int ver = Utils.uint16d(buf, 0);
		List<Layer> layers = new LinkedList<Layer>();
		if(this.ver == -1) {
			this.ver = ver;
		} else {
			if(ver != this.ver)
				throw(new LoadException("Wrong res version (" + ver + " != " + this.ver + ")", this));
		}
		outer: while(true) {
			StringBuilder tbuf = new StringBuilder();
			while(true) {
				byte bb;
				int ib;
				if((ib = in.read()) == -1) {
					if(tbuf.length() == 0)
						break outer;
					throw(new LoadException("Incomplete resource at " + name, this));
				}
				bb = (byte)ib;
				if(bb == 0)
					break;
				tbuf.append((char)bb);
			}
			buf = new byte[4];
			readall(in, buf);
			int len = Utils.int32d(buf, 0);
			buf = new byte[len];
			readall(in, buf);
			Class<? extends Layer> lc = ltypes.get(tbuf.toString());
			Constructor<? extends Layer> cons;
			try {
				cons = lc.getConstructor(byte[].class);
			} catch(NoSuchMethodException e) {
				throw(new RuntimeException(e));
			}
			Layer l;
			try {
				l = cons.newInstance(buf);
			} catch(InstantiationException e) {
				throw(new RuntimeException(e));
			} catch(InvocationTargetException e) {
				Throwable c = e.getCause();
				if(c instanceof RuntimeException) 
					throw((RuntimeException)c);
				else
					throw(new RuntimeException(c));
			} catch(IllegalAccessException e) {
				throw(new RuntimeException(e));
			}
			layers.add(l);
		}
		for(Layer l : layers)
			l.init();
		this.layers = layers;
	}
	
	private void checkerr() {
		if(error != null)
			throw(new RuntimeException(error));
	}
	
	private static InputStream getres(String name) {
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
			throw(new LoadException("Resource not found locally", e, null));
		}
	}
	
	public static BufferedImage loadimg(String name) {
		Resource res = load(name);
		res.loadwait();
		return(res.layer(imgc).img);
	}
	
	public static Tex loadtex(String name) {
		Resource res = load(name);
		res.loadwait();
		return(res.layer(imgc).tex());
	}
	
	/* Beware! Olde functions be here! */
	
	/*
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
	
	public static Tex loadtex(String name) {
		synchronized(texes) {
			if(texes.containsKey(name))
				return(texes.get(name));
			Tex tex = new TexI(loadimg(name));
			texes.put(name, tex);
			return(tex);
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
	
	private static BufferedImage loadlenimg(InputStream in) throws IOException {
		byte[] lb = new byte[4];
		in.read(lb);
		int len = Utils.int32d(lb, 0);
		byte[] buf = new byte[len];
		in.read(buf);
		return(ImageIO.read(new ByteArrayInputStream(buf)));
	}
	
	private static Sprite loadspr1(InputStream in, String name) throws IOException {
		byte[] ib = new byte[13];
		in.read(ib);
		Coord cc = new Coord(Utils.uint16d(ib, 0), Utils.uint16d(ib, 2));
		int prio = Utils.sb(ib[12]);
		BufferedImage frame = ImageIO.read(in);
		if(frame == null)
			throw(new RuntimeException("Bad image data in " + name));
		Sprite spr = new Sprite(frame, cc, prio);
		return(spr);
	}
	
	private static Sprite loadspr2(InputStream in, String name) throws IOException {
		byte[] ib = new byte[13];
		in.read(ib);
		Coord cc = new Coord(Utils.uint16d(ib, 0), Utils.uint16d(ib, 2));
		int prio = Utils.sb(ib[12]);
		BufferedImage frame = loadlenimg(in);
		if(frame == null)
			throw(new RuntimeException("Bad image data in " + name));
		Sprite spr = new Sprite(frame, cc, prio);
		BufferedImage sframe = loadlenimg(in);
		if(sframe == null)
			throw(new RuntimeException("Bad shadow data in " + name));
		Sprite sdw = new Sprite(sframe, Utils.imgsz(sframe).div(new Coord(2, 2)), 0);
		spr.shadow = sdw;
		return(spr);
	}
	
	public static Sprite loadsprite(String name) {
		synchronized(sprites) {
			if(sprites.containsKey(name))
				return(sprites.get(name));
			try {
				InputStream in = getres(name);
				String sig = "Haven Sprite ";
				byte[] sigb = new byte[sig.length()];
				in.read(sigb);
				if(!sig.equals(new String(sigb)))
					throw(new FormatException("Illegal sprite format", name));
				int ver = in.read() - '0';
				Sprite ret;
				if(ver == 1) {
					ret = loadspr1(in, name);
				} else if(ver == 2) {
					ret = loadspr2(in, name);
				} else {
					throw(new FormatException("Illegal sprite version", name));
				}
				sprites.put(name, ret);
				return(ret);
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
	*/
}
