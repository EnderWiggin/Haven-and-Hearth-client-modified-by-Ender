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
	public static Class<Tile> tile = Tile.class;
	public static Class<Neg> negc = Neg.class;
	public static Class<Anim> animc = Anim.class;
	public static Class<Tileset> tileset = Tileset.class;
	
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
			if((res != null) && (res.ver != -1) && (ver != -1)) {
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
			if(loader == null)
				loader = new Loader();
			loader.load(res);
		}
		return(res);
	}
	
	public static int qdepth() {
		return(loader.queue.size());
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
		private SslHelper ssl = new SslHelper();
		
		public Loader() {
			super(Utils.tg(), "Haven resource loader");
			setDaemon(true);
			start();
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
		
		private InputStream getreshttp(Resource res) throws IOException {
				URL resurl = new URL(baseurl, res.name + ".res");
				URLConnection c = resurl.openConnection();
				c.connect();
				return(c.getInputStream());
		}

		private void handle(Resource res) throws IOException {
			InputStream in = null;
			try {
				try {
					res.load(getres(res.name));
					return;
				} catch(LoadException e) {}
				res.load(getreshttp(res));
			} finally {
				if(in != null)
					in.close();
			}
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
	
	public class Image extends Layer implements Comparable<Image> {
		transient BufferedImage img;
		transient private Tex tex;
		final int z;
		final boolean l;
		final int id;
		private int gay = -1;
		Coord sz;
		Coord o;
		
		public Image(byte[] buf) {
			z = Utils.int16d(buf, 0);
			l = (buf[2] & 1) != 0;
			id = Utils.int16d(buf, 3);
			o = cdec(buf, 5);
			try {
				img = ImageIO.read(new ByteArrayInputStream(buf, 9, buf.length - 9));
			} catch(IOException e) {
				throw(new RuntimeException(e));
			}
			if(img == null)
				throw(new LoadException("Invalid image data in " + name, Resource.this));
			sz = Utils.imgsz(img);
		}
		
		public synchronized Tex tex() {
			if(tex != null)
				return(tex);
			tex = new TexI(img);
			return(tex);
		}
		
		private boolean detectgay() {
			for(int y = 0; y < sz.y; y++) {
				for(int x = 0; x < sz.x; x++) {
					if((img.getRGB(x, y) & 0x00ffffff) == 0x00ff0080)
						return(true);
				}
			}
			return(false);
		}
		
		public boolean gayp() {
			if(gay == -1)
				gay = detectgay()?1:0;
			return(gay == 1);
		}

		public int compareTo(Image other) {
			return(z - other.z);
		}
		
		public void init() {}
	}
	static {ltypes.put("image", Image.class);}
	
	public class Tile extends Layer {
		transient BufferedImage img;
		transient private Tex tex;
		int id;
		int w;
		char t;
		
		public Tile(byte[] buf) {
			t = (char)Utils.ub(buf[0]);
			id = Utils.ub(buf[1]);
			w = Utils.uint16d(buf, 2);
			try {
				img = ImageIO.read(new ByteArrayInputStream(buf, 4, buf.length - 4));
			} catch(IOException e) {
				throw(new RuntimeException(e));
			}
			if(img == null)
				throw(new LoadException("Invalid image data in " + name, Resource.this));
		}

		public synchronized Tex tex() {
			if(tex != null)
				return(tex);
			tex = new TexI(img);
			return(tex);
		}
		
		public void init() {}
	}
	static {ltypes.put("tile", Tile.class);}
	
	public class Neg extends Layer {
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
	
	public class Anim extends Layer {
		Image[][] f;
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
			f = new Image[ids.length][];
			Image[] typeinfo = new Image[0];
			for(int i = 0; i < ids.length; i++) {
				LinkedList<Image> buf = new LinkedList<Image>();
				for(Image img : layers(Image.class)) {
					if(img.id == ids[i])
						buf.add(img);
				}
				f[i] = buf.toArray(typeinfo);
			}
		}
	}
	static {ltypes.put("anim", Anim.class);}
	
	public class Tileset extends Layer {
		private int fl;
		private String fobase;
		private int[] flw;
		WeightList<Resource> flavobjs;
		WeightList<Tile> ground;
		WeightList<Tile>[] ctrans, btrans;
		int flavprob;
		
		public Tileset(byte[] buf) {
			int[] off = new int[1];
			off[0] = 0;
			fobase = Utils.strd(buf, off);
			fl = Utils.ub(buf[off[0]]);
			flw = new int[Utils.uint16d(buf, off[0] + 1)];
			flavprob = Utils.uint16d(buf, off[0] + 3);
			for(int i = 0; i < flw.length; i++)
				flw[i] = Utils.ub(buf[off[0] + 5]);
		}
		
		public void init() {
			flavobjs = new WeightList<Resource>();
			for(int i = 0; i < flw.length; i++)
				flavobjs.add(load(String.format("%s/%d", fobase, i + 1)), flw[i]);
			ground = new WeightList<Tile>();
			if((fl & 1) != 0) {
				ctrans = new WeightList[15];
				btrans = new WeightList[15];
				for(int i = 0; i < 15; i++) {
					ctrans[i] = new WeightList<Tile>();
					btrans[i] = new WeightList<Tile>();
				}
			}
			for(Tile t : layers(Tile.class)) {
				if(t.t == 'g')
					ground.add(t, t.w);
				else if(t.t == 'b')
					btrans[t.id - 1].add(t, t.w);
				else if(t.t == 'c')
					ctrans[t.id - 1].add(t, t.w);
			}
		}
	}
	static {ltypes.put("tileset", Tileset.class);}
	
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
			throw(new RuntimeException("Resource identity crisis!"));
		return(0);
	}
	
	public boolean equals(Object other) {
		if(!(other instanceof Resource) || (other == null))
			return(false);
		return(compareTo((Resource)other) == 0);
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
			if(lc == null)
				continue;
			Constructor<? extends Layer> cons;
			try {
				cons = lc.getConstructor(Resource.class, byte[].class);
			} catch(NoSuchMethodException e) {
				throw(new RuntimeException(e));
			}
			Layer l;
			try {
				l = cons.newInstance(this, buf);
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
		this.layers = layers;
		for(Layer l : layers)
			l.init();
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
		File f = new File(basedir, fn + ".res");
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
}
