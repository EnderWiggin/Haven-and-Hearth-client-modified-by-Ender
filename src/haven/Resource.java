package haven;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.imageio.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Resource implements Comparable<Resource>, Prioritized, Serializable {
    private static Map<String, Resource> cache = new TreeMap<String, Resource>();
    private static Loader loader = new Loader(new JarSource());
    private static CacheSource prscache;
    static ThreadGroup loadergroup = null;
    private static Map<String, Class<? extends Layer>> ltypes = new TreeMap<String, Class<? extends Layer>>();
    static Set<String> loadwaited = new HashSet<String>();
    static Set<String> allused = new HashSet<String>();
    public static Class<Image> imgc = Image.class;
    public static Class<Tile> tile = Tile.class;
    public static Class<Neg> negc = Neg.class;
    public static Class<Anim> animc = Anim.class;
    public static Class<Tileset> tileset = Tileset.class;
    public static Class<Pagina> pagina = Pagina.class;
    public static Class<AButton> action = AButton.class;
    public static Class<Audio> audio = Audio.class;
    public static Class<Tooltip> tooltip = Tooltip.class;
	
    private LoadException error;
    private Collection<? extends Layer> layers = new LinkedList<Layer>();
    public final String name;
    public int ver;
    public boolean loading;
    public ResSource source;
    private Indir<Resource> indir = null;
    private int prio = 0;

    private Resource(String name, int ver) {
	this.name = name;
	this.ver = ver;
	error = null;
	loading = true;
    }
	
    public static void addcache(ResCache cache) {
	CacheSource src = new CacheSource(cache);
	prscache = src;
	chainloader(new Loader(src));
    }

    public static void addurl(URL url) {
	ResSource src = new HttpSource(url);
	final CacheSource mc = prscache;
	if(mc != null) {
	    src = new TeeSource(src) {
		    public OutputStream fork(String name) throws IOException {
			return(mc.cache.store("res/" + name));
		    }
		};
	}
	chainloader(new Loader(src));
    }
    
    private static void chainloader(Loader nl) {
	synchronized(Resource.class) {
	    Loader l;
	    for(l = loader; l.next != null; l = l.next);
	    l.chain(nl);
	}
    }
    
    public static Resource load(String name, int ver, int prio) {
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
	    if(res != null) {
		if(res.prio < prio)
		    res.prio = prio;
		return res;
	    }
	    res = new Resource(name, ver);
	    res.prio = prio;
	    cache.put(name, res);
	}
	loader.load(res);
	return(res);
    }
	
    public static Resource load(String name, int ver) {
	return(load(name, ver, 0));
    }

    public static int qdepth() {
	int ret = 0;
	for(Loader l = loader; l != null; l = l.next)
	    ret += l.queue.size();
	return(ret);
    }
	
    public static Resource load(String name) {
	return(load(name, -1));
    }
	
    public void loadwaitint() throws InterruptedException {
	synchronized(this) {
	    prio = 10;
	    while(loading) {
		wait();
	    }
	}
    }
	
    public void loadwait() {
	boolean i = false;
	synchronized(loadwaited) {
	    loadwaited.add(name);
	}
	synchronized(this) {
	    prio = 10;
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
	
    public static interface ResSource {
	public InputStream get(String name) throws IOException;
    }
    
    public static abstract class TeeSource implements ResSource {
	public ResSource back;
	
	public TeeSource(ResSource back) {
	    this.back = back;
	}
	
	public InputStream get(String name) throws IOException {
	    StreamTee tee = new StreamTee(back.get(name));
	    tee.attach(fork(name));
	    return(tee);
	}
	
	public abstract OutputStream fork(String name) throws IOException;
	
	public String toString() {
	    return("forking source backed by " + back);
	}
    }
    
    public static class CacheSource implements ResSource {
	public ResCache cache;
	
	public CacheSource(ResCache cache) {
	    this.cache = cache;
	}
	
	public InputStream get(String name) throws IOException {
	    return(cache.fetch("res/" + name));
	}
	
	public String toString() {
	    return("cache source backed by " + cache);
	}
    }

    public static class JarSource implements ResSource {
	public InputStream get(String name) {
	    InputStream s = Resource.class.getResourceAsStream("/res/" + name + ".res");
	    if(s == null)
		throw(new LoadException("Could not find resource locally: " + name, JarSource.this));
	    return(s);
	}
	
	public String toString() {
	    return("local res source");
	}
    }
    
    public static class HttpSource implements ResSource {
	private transient SslHelper ssl;
	public URL baseurl;
	
	public HttpSource(URL baseurl) {
	    this.baseurl = baseurl;
	}
		
	public InputStream get(String name) throws IOException {
	    if(ssl == null) {
		ssl = new SslHelper();
		try {
		    ssl.trust(ssl.loadX509(Resource.class.getResourceAsStream("ressrv.crt")));
		} catch(java.security.cert.CertificateException e) {
		    throw(new LoadException("Invalid built-in certificate", e, null));
		}
		ssl.ignoreName();
	    }
	    URL resurl = new URL(baseurl, name + ".res");
	    URLConnection c = ssl.connect(resurl);
	    return(c.getInputStream());
	}

	public String toString() {
	    return("HTTP res source (" + baseurl + ")");
	}
    }

    private static class Loader implements Runnable {
	private ResSource src;
	private Loader next = null;
	private Queue<Resource> queue = new PrioQueue<Resource>();
	private transient Thread th = null;
	
	public Loader(ResSource src) {
	    this.src = src;
	}
	
	public void chain(Loader next) {
	    this.next = next;
	}
	
	public void load(Resource res) {
	    synchronized(queue) {
		queue.add(res);
		queue.notifyAll();
	    }
	    synchronized(Loader.this) {
		if(th == null) {
		    ThreadGroup tg = loadergroup;
		    if(tg == null)
			tg = Utils.tg();
		    th = new Thread(tg, Loader.this, "Haven resource loader");
		    th.setDaemon(true);
		    th.start();
		}
	    }
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
			handle(cur);
		    }
		    cur = null;
		}
	    } catch(InterruptedException e) {
	    } finally {
		synchronized(Loader.this) {
		    /* Yes, I know there's a race condition. */
		    th = null;
		}
	    }
	}
		
	private void handle(Resource res) {
	    InputStream in = null;
	    try {
		res.error = null;
		res.source = src;
		try {
		    try {
			in = src.get(res.name);
			res.load(in);
			res.loading = false;
			res.notifyAll();
			return;
		    } catch(IOException e) {
			throw(new LoadException(e, res));
		    }
		} catch(LoadException e) {
		    if(next == null) {
			res.error = e;
			res.loading = false;
			res.notifyAll();
		    } else {
			next.load(res);
		    }
		}
	    } finally {
		try {
		    if(in != null)
			in.close();
		} catch(IOException e) {}
	    }
	}
    }
	
    @SuppressWarnings("serial")
	public static class LoadException extends RuntimeException {
	    public Resource res;
	    public ResSource src;
	    
	    public LoadException(String msg, ResSource src) {
		super(msg);
		this.src = src;
	    }
	    
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
	public transient BufferedImage img;
	transient private Tex tex;
	public final int z, subz;
	public final boolean l;
	public final int id;
	private int gay = -1;
	public Coord sz;
	public Coord o;
		
	public Image(byte[] buf) {
	    z = Utils.int16d(buf, 0);
	    subz = Utils.int16d(buf, 2);
	    l = (buf[4] & 1) != 0;
	    id = Utils.int16d(buf, 5);
	    o = cdec(buf, 7);
	    try {
		img = ImageIO.read(new ByteArrayInputStream(buf, 11, buf.length - 11));
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
        
    public class Tooltip extends Layer {
	public final String t;
                
	public Tooltip(byte[] buf) {
	    try {
		t = new String(buf, "UTF-8");
	    } catch(UnsupportedEncodingException e) {
		throw(new RuntimeException(e));
	    }
	}
                
	public void init() {}
    }
    static {ltypes.put("tooltip", Tooltip.class);}
	
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
	    if(tex == null)
		tex = new TexI(img);
	    return(tex);
	}
		
	public void init() {}
    }
    static {ltypes.put("tile", Tile.class);}
	
    public class Neg extends Layer {
	public Coord cc;
	public Coord bc, bs;
	public Coord sz;
	public Coord[][] ep;
		
	public Neg(byte[] buf) {
	    int off;
			
	    cc = cdec(buf, 0);
	    bc = cdec(buf, 4);
	    bs = cdec(buf, 8);
	    sz = cdec(buf, 12);
	    bc = MapView.s2m(bc);
	    bs = MapView.s2m(bs).add(bc.inv());
	    ep = new Coord[8][0];
	    int en = buf[16];
	    off = 17;
	    for(int i = 0; i < en; i++) {
		int epid = buf[off];
		int cn = Utils.uint16d(buf, off + 1);
		off += 3;
		ep[epid] = new Coord[cn];
		for(int o = 0; o < cn; o++) {
		    ep[epid][o] = cdec(buf, off);
		    off += 4;
		}
	    }
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
		    if((img.id == ids[i]) || (img.id == -1))
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
		
	private void packtiles(Collection<Tile> tiles, Coord tsz) {
	    int min = -1, minw = -1, minh = -1;
	    int nt = tiles.size();
	    for(int i = 1; i <= nt; i++) {
		int w = Tex.nextp2(tsz.x * i);
		int h;
		if((nt % i) == 0)
		    h = nt / i;
		else
		    h = (nt / i) + 1;
		h = Tex.nextp2(tsz.y * h);
		int a = w * h;
		if((min == -1) || (a < min)) {
		    min = a;
		    minw = w;
		    minh = h;
		}
	    }
	    TexIM packbuf = new TexIM(new Coord(minw, minh));
	    Graphics g = packbuf.graphics();
	    int x = 0, y = 0;
	    for(Tile t :  tiles) {
		g.drawImage(t.img, x, y, null);
		t.tex = new TexSI(packbuf, new Coord(x, y), tsz);
		if((x += tsz.x) > (minw - tsz.x)) {
		    x = 0;
		    if((y += tsz.y) >= minh)
			throw(new LoadException("Could not pack tiles into calculated minimum texture", Resource.this));
		}
	    }
	    packbuf.update();
	}
		
	public void init() {
	    flavobjs = new WeightList<Resource>();
	    for(int i = 0; i < flw.length; i++)
		flavobjs.add(load(String.format("%s/f%d", fobase, i + 1)), flw[i]);
	    Collection<Tile> tiles = new LinkedList<Tile>();
	    ground = new WeightList<Tile>();
	    if((fl & 1) != 0) {
		ctrans = new WeightList[15];
		btrans = new WeightList[15];
		for(int i = 0; i < 15; i++) {
		    ctrans[i] = new WeightList<Tile>();
		    btrans[i] = new WeightList<Tile>();
		}
	    }
	    Coord tsz = null;
	    for(Tile t : layers(Tile.class)) {
		if(t.t == 'g')
		    ground.add(t, t.w);
		else if(t.t == 'b')
		    btrans[t.id - 1].add(t, t.w);
		else if(t.t == 'c')
		    ctrans[t.id - 1].add(t, t.w);
		tiles.add(t);
		if(tsz == null) {
		    tsz = Utils.imgsz(t.img);
		} else {
		    if(!Utils.imgsz(t.img).equals(tsz)) {
			throw(new LoadException("Different tile sizes within set", Resource.this));
		    }
		}
	    }
	    packtiles(tiles, tsz);
	}
    }
    static {ltypes.put("tileset", Tileset.class);}
	
    public class Pagina extends Layer {
	public final String text;
		
	public Pagina(byte[] buf) {
	    try {
		text = new String(buf, "UTF-8");
	    } catch(UnsupportedEncodingException e) {
		throw(new RuntimeException(e));
	    }
	}
		
	public void init() {}
    }
    static {ltypes.put("pagina", Pagina.class);}
	
    public class AButton extends Layer {
	public final String name;
	public final Resource parent;
	public final char hk;
	public final String[] ad;
		
	public AButton(byte[] buf) {
	    int[] off = new int[1];
	    off[0] = 0;
	    String pr = Utils.strd(buf, off);
	    if(pr.length() == 0)
		parent = null;
	    else
		parent = load(pr);
	    name = Utils.strd(buf, off);
	    Utils.strd(buf, off); /* Prerequisite skill */
	    hk = (char)Utils.uint16d(buf, off[0]);
	    off[0] += 2;
	    ad = new String[Utils.uint16d(buf, off[0])];
	    off[0] += 2;
	    for(int i = 0; i < ad.length; i++)
		ad[i] = Utils.strd(buf, off);
	}
		
	public void init() {}
    }
    static {ltypes.put("action", AButton.class);}
	
    public class Code extends Layer {
	public final String name;
	transient public final byte[] data;
		
	public Code(byte[] buf) {
	    int[] off = new int[1];
	    off[0] = 0;
	    name = Utils.strd(buf, off);
	    data = new byte[buf.length - off[0]];
	    System.arraycopy(buf, off[0], data, 0, data.length);
	}
		
	public void init() {}
    }
    static {ltypes.put("code", Code.class);}
	
    public class ResClassLoader extends ClassLoader {
	public ResClassLoader(ClassLoader parent) {
	    super(parent);
	}
		
	public Resource getres() {
	    return(Resource.this);
	}
    };

    public class CodeEntry extends Layer {
	private String clnm;
	private Map<String, Code> clmap = new TreeMap<String, Code>();
	private Map<String, String> pe = new TreeMap<String, String>();
	transient private ClassLoader loader;
	transient public Class<? extends Sprite> spr;
		
	public CodeEntry(byte[] buf) {
	    int[] off = new int[1];
	    off[0] = 0;
	    while(off[0] < buf.length) {
		pe.put(Utils.strd(buf, off), Utils.strd(buf, off));
	    }
	}
		
	public void init() {
	    for(Code c : layers(Code.class))
		clmap.put(c.name, c);
	    loader = new ResClassLoader(Resource.class.getClassLoader()) {
		    public Class<?> findClass(String name) throws ClassNotFoundException {
			Code c = clmap.get(name);
			if(c == null)
			    throw(new ClassNotFoundException("Could not find main sprite class"));
			return(defineClass(name, c.data, 0, c.data.length));
		    }
		};
	    try {
		String clnm;
		if((clnm = pe.get("spr")) != null)
		    spr = loader.loadClass(clnm).asSubclass(Sprite.class);
	    } catch(ClassNotFoundException e) {
		throw(new LoadException(e, Resource.this));
	    }
	}
    }
    static {ltypes.put("codeentry", CodeEntry.class);}
	
    public class Audio extends Layer {
	transient public byte[] clip;
	    
	public Audio(byte[] buf) {
	    try {
		clip = Utils.readall(new VorbisDecoder(new ByteArrayInputStream(buf)));
	    } catch(IOException e) {
		throw(new LoadException(e, Resource.this));
	    }
	}
	    
	public void init() {}
    }
    static {ltypes.put("audio", Audio.class);}
	
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
	synchronized(allused) {
	    allused.add(name);
	}
    }
	
    public Indir<Resource> indir() {
	if(indir != null)
	    return(indir);
	indir = new Indir<Resource>() {
	    public Resource res = Resource.this;
			
	    public Resource get() {
		if(loading)
		    return(null);
		return(Resource.this);
	    }
			
	    public void set(Resource r) {
		throw(new RuntimeException());
	    }
			
	    public int compareTo(Indir<Resource> x) {
		return(Resource.this.compareTo(this.getClass().cast(x).res));
	    }
	};
	return(indir);
    }
	
    private void checkerr() {
	if(error != null)
	    throw(new RuntimeException("Delayed error in resource " + name + " (v" + ver + ")", error));
    }
	
    public int priority() {
	return(prio);
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
	
    public String toString() {
	return(name + "(v" + ver + ")");
    }
	
    public static void loadlist(InputStream list, int prio) throws IOException {
	BufferedReader in = new BufferedReader(new InputStreamReader(list, "us-ascii"));
	String nm;
	while((nm = in.readLine()) != null)
	    load(nm, -1, prio);
	in.close();
    }

    static {
	try {
	    InputStream pls;
	    pls = Resource.class.getResourceAsStream("res-preload");
	    if(pls != null)
		loadlist(pls, -5);
	    pls = Resource.class.getResourceAsStream("res-bgload");
	    if(pls != null)
		loadlist(pls, -10);
	} catch(IOException e) {
	    throw(new Error(e));
	}
    }
}
