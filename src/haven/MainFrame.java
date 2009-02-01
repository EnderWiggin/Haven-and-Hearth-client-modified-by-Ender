package haven;

import java.awt.*;
import java.net.URL;
import java.awt.event.*;
import java.io.PrintWriter;

public class MainFrame extends Frame implements Runnable, FSMan {
    HavenPanel p;
    ThreadGroup g;
    DisplayMode fsmode = null, prefs = null;
    static JnlpCache jnlpcache;
	
    static {
	try {
	    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
	} catch(Exception e) {}
    }
	
    DisplayMode findmode(int w, int h) {
	GraphicsDevice dev = getGraphicsConfiguration().getDevice();
	if(!dev.isFullScreenSupported())
	    return(null);
	DisplayMode b = null;
	for(DisplayMode m : dev.getDisplayModes()) {
	    int d = m.getBitDepth();
	    if((m.getWidth() == w) && (m.getHeight() == h) && ((d == 24) || (d == 32) || (d == DisplayMode.BIT_DEPTH_MULTI))) {
		if((b == null) || (d > b.getBitDepth()) || ((d == b.getBitDepth()) && (m.getRefreshRate() > b.getRefreshRate())))
		    b = m;
	    }
	}
	return(b);
    }
	
    public void setfs() {
	GraphicsDevice dev = getGraphicsConfiguration().getDevice();
	if(prefs != null)
	    return;
	prefs = dev.getDisplayMode();
	try {
	    setVisible(false);
	    dispose();
	    setUndecorated(true);
	    setVisible(true);
	    dev.setFullScreenWindow(this);
	    dev.setDisplayMode(fsmode);
			
	} catch(Exception e) {
	    throw(new RuntimeException(e));
	}
    }
	
    public void setwnd() {
	GraphicsDevice dev = getGraphicsConfiguration().getDevice();
	if(prefs == null)
	    return;
	try {
	    dev.setDisplayMode(prefs);
	    dev.setFullScreenWindow(null);
	    setVisible(false);
	    dispose();
	    setUndecorated(false);
	    setVisible(true);
	} catch(Exception e) {
	    throw(new RuntimeException(e));
	}
	prefs = null;
    }

    public boolean hasfs() {
	return(prefs != null);
    }

    public void togglefs() {
	if(prefs == null)
	    setfs();
	else
	    setwnd();
    }

    private void seticon() {
	Image icon;
	try {
	    java.io.InputStream data = MainFrame.class.getResourceAsStream("icon.png");
	    icon = javax.imageio.ImageIO.read(data);
	    data.close();
	} catch(java.io.IOException e) {
	    throw(new Error(e));
	}
	setIconImage(icon);
    }

    public MainFrame(int w, int h) {
	super("Haven and Hearth");
	p = new HavenPanel(w, h);
	fsmode = findmode(w, h);
	add(p);
	pack();
	setResizable(false);
	p.requestFocus();
	seticon();
	setVisible(true);
	p.init();
    }
	
    public void run() {
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    g.interrupt();
		}
	    });
	Thread ui = new Thread(Utils.tg(), p, "Haven UI thread");
	p.setfsm(this);
	ui.start();
	try {
	    while(true) {
		Bootstrap bill = new Bootstrap();
		String defaddr = Utils.getprop("haven.defserv", null);
		if(defaddr != null)
		    bill.setaddr(defaddr);
		if((Utils.getprop("haven.authuser", null) != null) && (Utils.getprop("haven.authck", null) != null))
		    bill.setinitcookie(Utils.getprop("haven.authuser", null), Utils.hex2byte(Utils.getprop("haven.authck", null)));
		Session sess = bill.run(p);
		RemoteUI rui = new RemoteUI(sess);
		rui.run(p.newui(sess));
	    }
	} catch(InterruptedException e) {
	} finally {
	    ui.interrupt();
	    dispose();
	}
    }
    
    public static void setupres() {
	jnlpcache = JnlpCache.create();
	if(jnlpcache != null)
	    Resource.addcache(jnlpcache);
	try {
	    String url = Utils.getprop("haven.resurl", "https://www.havenandhearth.com/res/");
	    if(!url.equals(""))
		Resource.addurl(new URL(url));
	} catch(java.net.MalformedURLException e) {
	    throw(new RuntimeException(e));
	}
	if(jnlpcache != null) {
	    try {
		Resource.loadlist(jnlpcache.fetch("tmp/allused"), -10);
	    } catch(java.io.IOException e) {}
	}
    }
    
    static {
	WebBrowser.self = JnlpBrowser.create();
    }

    private static void javabughack() throws InterruptedException {
	/* Work around a stupid deadlock bug in AWT. */
	try {
	    javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
		    public void run() {
			java.io.PrintStream bitbucket = new java.io.PrintStream(new java.io.ByteArrayOutputStream());
			bitbucket.print(LoginScreen.textf);
			bitbucket.print(LoginScreen.textfs);
		    }
		});
	} catch(java.lang.reflect.InvocationTargetException e) {
	    /* Oh, how I love Swing! */
	    throw(new Error(e));
	}
    }

    private static void main2() {
	ThreadGroup g = Utils.tg();
	Resource.loadergroup = g;
	setupres();
	MainFrame f = new MainFrame(800, 600);
	if(Utils.getprop("haven.fullscreen", "off").equals("on"))
	    f.setfs();
	f.g = g;
	if(g instanceof haven.error.ErrorHandler) {
	    final haven.error.ErrorHandler hg = (haven.error.ErrorHandler)g;
	    hg.sethandler(new haven.error.ErrorGui(null) {
		    public void errorsent() {
			hg.interrupt();
		    }
		});
	}
	f.run();
	dumplist(Resource.loadwaited, Utils.getprop("haven.loadwaited", null));
	dumplist(Resource.cached(), Utils.getprop("haven.allused", null));
	if(jnlpcache != null) {
	    try {
		dumplist(Resource.cached(), new PrintWriter(jnlpcache.store("tmp/allused")));
	    } catch(java.io.IOException e) {}
	}
    }
    
    public static void main(String[] args) {
	/* Set up the error handler as early as humanly possible. */
	ThreadGroup g;
	if(Utils.getprop("haven.errorhandler", "off").equals("on")) {
	    final haven.error.ErrorHandler hg = new haven.error.ErrorHandler();
	    hg.sethandler(new haven.error.ErrorGui(null) {
		    public void errorsent() {
			hg.interrupt();
		    }
		});
	    g = hg;
	} else {
	    g = new ThreadGroup("Haven client");
	}
	Thread main = new Thread(g, new Runnable() {
		public void run() {
		    try {
			javabughack();
		    } catch(InterruptedException e) {
			return;
		    }
		    main2();
		}
	    }, "Haven main thread");
	main.start();
	try {
	    main.join();
	} catch(InterruptedException e) {
	    g.interrupt();
	    return;
	}
	System.exit(0);
    }
	
    private static void dumplist(java.util.Collection<Resource> list, String fn) {
	try {
	    if(fn != null)
		dumplist(list, new PrintWriter(fn));
	} catch(Exception e) {
	    throw(new RuntimeException(e));
	}
    }
    
    private static void dumplist(java.util.Collection<Resource> list, PrintWriter out) {
	try {
	    for(Resource res : list) {
		if(res.loading)
		    continue;
		if(res.prio < 0)
		    continue;
		out.println(res.name + ":" + res.ver);
	    }
	    out.close();
	} catch(Exception e) {
	    throw(new RuntimeException(e));
	}
    }
}
