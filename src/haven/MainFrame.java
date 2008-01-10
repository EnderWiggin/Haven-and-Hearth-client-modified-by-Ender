package haven;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class MainFrame extends Frame {
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
	
	public void main2() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				synchronized(p.ui) {
					if(Session.current != null)
						Session.current.close();
					System.exit(0);
				}
			}
		});
		Thread boot = new Bootstrap(p.ui);
		boot.start();
		Thread ui = new Thread(Utils.tg(), p, "Haven UI thread");
		ui.start();
	}

	public static void main(String[] args) {
		final MainFrame f = new MainFrame(800, 600);
		ThreadGroup g;
		if(System.getProperty("haven.errorhandler", "off").equals("on")) {
			g = new haven.error.ErrorHandler(new haven.error.ErrorGui(f) {
					public void errorsent() {
						System.exit(1);
					}
				});
		} else {
			g = new ThreadGroup("Haven client");
		}
		f.g = g;
		Thread main = new Thread(g, new Runnable() {
				public void run() {
					f.main2();
				}
			});
		main.start();
	}
}
