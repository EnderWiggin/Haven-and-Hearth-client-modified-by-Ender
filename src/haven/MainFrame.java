package haven;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class MainFrame extends Frame implements Runnable {
	HavenPanel p;
	ThreadGroup g;
	
	public MainFrame(int w, int h) {
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
					g.interrupt();
					setVisible(false);
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
