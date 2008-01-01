package haven;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class MainFrame extends Frame {
	HavenPanel p;
	
	public MainFrame(int w, int h) {
		p = new HavenPanel(w, h);
		add(p);
		pack();
		p.requestFocus();
		setVisible(true);
		p.init();
	}
	
	public static void main2(final MainFrame f) {
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				synchronized(f.p.ui) {
					if(Session.current != null)
						Session.current.close();
					System.exit(0);
				}
			}
		});
		Thread boot = new Bootstrap(f.p.ui);
		boot.start();
		Thread ui = new Thread(Utils.tg(), f.p, "Haven UI thread");
		ui.start();
	}

	public static void main(String[] args) {
		final MainFrame f = new MainFrame(800, 600);
		ThreadGroup g;
		g = new haven.error.ErrorHandler(new haven.error.ErrorGui(f) {
				public void errorsent() {
					System.exit(1);
				}
			});
		Thread main = new Thread(g, new Runnable() {
				public void run() {
					main2(f);
				}
			});
		main.start();
	}
}
