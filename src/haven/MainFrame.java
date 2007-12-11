package haven;

import java.awt.Frame;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Graphics;
import java.util.*;

public class MainFrame extends Frame {
	RootWidget root;
	UI ui;
	List<InputEvent> events = new LinkedList<InputEvent>();
	
	public MainFrame(int w, int h) {
		setSize(w, h);
		setVisible(true);
		setFocusTraversalKeysEnabled(false);
		createBufferStrategy(2);
		root = new RootWidget(new Coord(w, h), getGraphicsConfiguration());
		ui = new UI(root);
		addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}

			public void keyPressed(KeyEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}
			public void keyReleased(KeyEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}
		});
	}
	
	void redraw() {
		BufferStrategy bs = getBufferStrategy();
		Graphics g = bs.getDrawGraphics();
		try {
			root.draw(g);
		} finally {
			g.dispose();
		}
		bs.show();
	}
	
	void dispatch() {
		synchronized(events) {
			while(events.size() > 0) {
				InputEvent e = events.remove(0);
				if(e instanceof MouseEvent) {
					MouseEvent me = (MouseEvent)e;
					if(me.getID() == MouseEvent.MOUSE_PRESSED) {
						ui.mousedown(new Coord(me.getX(), me.getY()), me.getButton());
					} else if(me.getID() == MouseEvent.MOUSE_RELEASED) {
						ui.mouseup(new Coord(me.getX(), me.getY()), me.getButton());
					}
				} else if(e instanceof KeyEvent) {
					KeyEvent ke = (KeyEvent)e;
					if(ke.getID() == KeyEvent.KEY_PRESSED) {
						ui.keydown(ke);
					} else if(ke.getID() == KeyEvent.KEY_RELEASED) {
						ui.keyup(ke);
					} else if(ke.getID() == KeyEvent.KEY_TYPED) {
						ui.type(ke);
					}
				}
			}
		}
	}
	
	void loop() {
		while(true) {
			long now, then;
			then = System.currentTimeMillis();
			try {
				if(Session.current != null)
					Session.current.oc.tick();
				synchronized(ui) {
					dispatch();
					redraw();
				}
			} catch(Throwable t) {
				throw(new Error(t));
			}
			now = System.currentTimeMillis();
			//System.out.println(now - then);
			if(now - then < 60) {
				try {
					Thread.sleep(60 - (now - then));
				} catch(InterruptedException e) {}
			}
		}
	}
	
	public static void main2() {
		MainFrame f = new MainFrame(800, 600);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		Thread t = new Bootstrap(f.ui);
		t.start();
		f.loop();
	}

	public static void main(String[] args) {
		new ErrorHandler(new Runnable() {
				public void run() {
					main2();
				}
			});
	}
}
