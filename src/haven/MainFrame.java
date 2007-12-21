package haven;

import java.awt.Frame;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Graphics;
import java.util.*;

public class MainFrame extends Frame {
	HavenPanel p;
	
	public MainFrame(int w, int h) {
		p = new HavenPanel(w, h);
		add(p);
		pack();
		setVisible(true);
		p.init();
	}
	
	public static void main(String[] args) {
		final MainFrame f = new MainFrame(800, 600);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				synchronized(f.p.ui) {
					if(Session.current != null)
						Session.current.close();
					System.exit(0);
				}
			}
		});
		Thread t = new Bootstrap(f.p.ui);
		t.start();
		f.p.run();
	}
}
