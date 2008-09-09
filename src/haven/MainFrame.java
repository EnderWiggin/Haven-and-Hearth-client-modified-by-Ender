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

    public MainFrame(int w, int h) {
	super("Haven and Hearth");
	p = new HavenPanel(w, h);
	fsmode = findmode(w, h);
	add(p);
	pack();
	//setResizable(false);
	p.requestFocus();
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
		if(System.getProperty("haven.authck") != null)
		    bill.setinitcookie(Utils.hex2byte(System.getProperty("haven.authck")));
		Session sess = bill.run(p);
		RemoteUI rui = new RemoteUI(sess);
		rui.run(p);
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
	    Resource.addurl(new URL(Utils.getprop("haven.resurl", "https://www.havenandhearth.com/res/")));
	} catch(java.net.MalformedURLException e) {
	    throw(new RuntimeException(e));
	}
	if(jnlpcache != null) {
	    try {
		Resource.loadlist(jnlpcache.fetch("tmp/allused"), -10);
	    } catch(java.io.IOException e) {}
	}
    }
    
    public static void main(String[] args) {
	final MainFrame f = new MainFrame(800, 600);
	if(Utils.getprop("haven.fullscreen", "off").equals("on"))
	    f.setfs();
	setupres();
	ThreadGroup g;
	if(Utils.getprop("haven.errorhandler", "off").equals("on")) {
	    g = new haven.error.ErrorHandler(new haven.error.ErrorGui(f) {
		    public void errorsent() {
			f.g.interrupt();
		    }
		});
	} else {
	    g = new ThreadGroup("Haven client");
	}
	Resource.loadergroup = g;
	f.g = g;
	Thread main = new Thread(g, f);
	main.start();
	try {
	    main.join();
	} catch(InterruptedException e) {
	    return;
	}
	dumplist(Resource.loadwaited, Utils.getprop("haven.loadwaited", null));
	dumplist(Resource.allused, Utils.getprop("haven.allused", null));
	if(jnlpcache != null) {
	    try {
		dumplist(Resource.allused, new PrintWriter(jnlpcache.store("tmp/allused")));
	    } catch(java.io.IOException e) {}
	}
	System.exit(0);
    }
	
    private static void dumplist(java.util.Collection<String> list, String fn) {
	try {
	    if(fn != null)
		dumplist(list, new PrintWriter(fn));
	} catch(Exception e) {
	    throw(new RuntimeException(e));
	}
    }
    
    private static void dumplist(java.util.Collection<String> list, PrintWriter out) {
	try {
	    for(String res : list)
		out.println(res);
	    out.close();
	} catch(Exception e) {
	    throw(new RuntimeException(e));
	}
    }
}
