package haven;

import java.awt.Canvas;
import java.awt.GraphicsConfiguration;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Graphics;
import java.util.*;

public class HavenPanel extends Canvas implements Runnable, Graphical {
	RootWidget root;
	UI ui;
	int w, h;
	long fd = 60, fps = 0;
	List<InputEvent> events = new LinkedList<InputEvent>();
	
	public HavenPanel(int w, int h) {
		setSize(this.w = w, this.h = h);
	}
	
	public void init() {
		setFocusTraversalKeysEnabled(false);
		createBufferStrategy(2);
		root = new RootWidget(new Coord(w, h), this);
		ui = new UI(root);
		addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				synchronized(events) {
					events.add(e);
					events.notifyAll();
				}
			}

			public void keyPressed(KeyEvent e) {
				synchronized(events) {
					events.add(e);
					events.notifyAll();
				}
			}
			public void keyReleased(KeyEvent e) {
				synchronized(events) {
					events.add(e);
					events.notifyAll();
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				synchronized(events) {
					events.add(e);
					events.notifyAll();
				}
			}

			public void mouseReleased(MouseEvent e) {
				synchronized(events) {
					events.add(e);
					events.notifyAll();
				}
			}
		});
		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}

			public void mouseMoved(MouseEvent e) {
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
			g.setColor(java.awt.Color.WHITE);
			Utils.drawtext(g, "FPS: " + fps, new Coord(0, 0));
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
					} else if(me.getID() == MouseEvent.MOUSE_MOVED || me.getID() == MouseEvent.MOUSE_DRAGGED) {
						ui.mousemove(new Coord(me.getX(), me.getY()));
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
	
	public void run() {
		try {
			long now, fthen, then;
			int frames = 0;
			fthen = System.currentTimeMillis();
			while(true) {
				then = System.currentTimeMillis();
				synchronized(ui) {
					dispatch();
					redraw();
				}
				frames++;
				now = System.currentTimeMillis();
				if(now - then < fd) {
					synchronized(events) {
						events.wait(fd - (now - then));
					}
				}
				if(now - fthen > 1000) {
					fps = frames;
					frames = 0;
					fthen = now;
				}
				if(Thread.interrupted())
					throw(new InterruptedException());
			}
		} catch(InterruptedException e) {}
	}
	
	public GraphicsConfiguration getconf() {
		return(getGraphicsConfiguration());
	}
}
