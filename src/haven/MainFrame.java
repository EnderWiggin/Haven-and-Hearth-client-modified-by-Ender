package haven;

import java.awt.*;
import java.net.URL;
import java.awt.event.*;

public class MainFrame extends Frame implements Runnable {
	HavenPanel p;
	ThreadGroup g;
	boolean exiting = false;
	
	public MainFrame(int w, int h) {
		super("Haven and Hearth");
		p = new HavenPanel(w, h);
		add(p);
		pack();
		p.requestFocus();
		setVisible(true);
		p.init();
	}
	
	public void run() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				synchronized(p.ui) {
					exiting = true;
					if(p.ui.sess != null) {
						p.ui.sess.close();
					} else {
						g.interrupt();
						setVisible(false);
					}
				}
			}
		});
		Bootstrap bill = new Bootstrap(p.ui, System.getProperty("haven.srvlist").equals("on"));
		String defaddr = System.getProperty("haven.defserv");
		if(defaddr != null)
			bill.setaddr(defaddr);
		bill.start();
		Thread ui = new Thread(Utils.tg(), p, "Haven UI thread");
		ui.start();
		try {
			ui.join();
		} catch(InterruptedException e) {
		}
	}
	
	public static void main(String[] args) {
		final MainFrame f = new MainFrame(800, 600);
		try {
			Resource.baseurl = new URL(System.getProperty("haven.resurl", "http://www.havenandhearth.com/res/"));
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
		while(!f.exiting) {
			Thread main = new Thread(g, f);
			main.start();
			try {
				main.join();
			} catch(InterruptedException e) {
				return;
			}
		}
		System.exit(0);
	}
}
