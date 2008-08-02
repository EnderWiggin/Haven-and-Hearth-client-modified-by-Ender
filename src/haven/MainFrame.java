package haven;

import java.awt.*;
import java.net.URL;
import java.awt.event.*;

public class MainFrame extends Frame implements Runnable {
	HavenPanel p;
	ThreadGroup g;
	
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
			if((m.getWidth() == w) && (m.getHeight() == h) && ((d == 24) || (d == 32))) {
				if((b == null) || (d > b.getBitDepth()))
					b = m;
			}
		}
		return(b);
	}
	
	void setfs(DisplayMode m) {
		GraphicsDevice dev = getGraphicsConfiguration().getDevice();
		try {
			setUndecorated(true);
			dev.setFullScreenWindow(this);
			dev.setDisplayMode(m);
		} catch(Exception e) {
			throw(new RuntimeException(e));
		}
	}
	
	public MainFrame(int w, int h, boolean fs) {
		super("Haven and Hearth");
		p = new HavenPanel(w, h);
		DisplayMode tm = findmode(w, h);
		if(tm == null)
			fs = false;
		if(fs)
		    setfs(tm);
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
		ui.start();
		try {
			while(true) {
				Bootstrap bill = new Bootstrap();
				String defaddr = System.getProperty("haven.defserv");
				if(defaddr != null)
					bill.setaddr(defaddr);
				Session sess = bill.run(p);
				RemoteUI rui = new RemoteUI(sess);
				rui.run(p);
			}
		} catch(InterruptedException e) {
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			ui.interrupt();
			dispose();
		}
	}
	
	public static void main(String[] args) {
		boolean fs = false;
		if(System.getProperty("haven.fullscreen", "off").equals("on"))
			fs = true;
		final MainFrame f = new MainFrame(800, 600, fs);
		try {
			Resource.baseurl = new URL(System.getProperty("haven.resurl", "https://www.havenandhearth.com/res/"));
		} catch(java.net.MalformedURLException e) {
			throw(new RuntimeException(e));
		}
		ThreadGroup g;
		if(System.getProperty("haven.errorhandler", "off").equals("on")) {
			g = new haven.error.ErrorHandler(new haven.error.ErrorGui(f) {
					public void errorsent() {
						f.g.interrupt();
					}
				});
		} else {
			g = new ThreadGroup("Haven client");
		}
		f.g = g;
		Thread main = new Thread(g, f);
		main.start();
		try {
		    main.join();
		} catch(InterruptedException e) {
		    return;
		}
		System.exit(0);
	}
}
