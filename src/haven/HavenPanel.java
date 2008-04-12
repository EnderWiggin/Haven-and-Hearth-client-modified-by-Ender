package haven;

import java.awt.GraphicsConfiguration;
import java.awt.event.*;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

@SuppressWarnings("serial")
public class HavenPanel extends GLCanvas implements Runnable, Graphical {
	RootWidget root;
	UI ui;
	boolean inited = false;
	int w, h;
	long fd = 20, fps = 0;
	List<InputEvent> events = new LinkedList<InputEvent>();
	
	public HavenPanel(int w, int h) {
		setSize(this.w = w, this.h = h);
		initgl();
	}
	
	private void initgl() {
		addGLEventListener(new GLEventListener() {
			public void display(GLAutoDrawable d) {
				GL gl = d.getGL();
				if(inited)
					redraw(gl);
				Tex.disposeall(gl);
			}
			
			public void init(GLAutoDrawable d) {
				GL gl = d.getGL();
				gl.glClearColor(0, 0, 0, 1);
				gl.glColor3f(1, 1, 1);
				gl.glPointSize(4);
				gl.setSwapInterval(1);
				gl.glEnable(GL.GL_BLEND);
				gl.glEnable(GL.GL_LINE_SMOOTH);
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			}

			public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
				GL gl = d.getGL();
				GLU glu = new GLU();
				gl.glMatrixMode(GL.GL_PROJECTION);
				gl.glLoadIdentity();
				glu.gluOrtho2D(0, w, h, 0);
			}
			
			public void displayChanged(GLAutoDrawable d, boolean cp1, boolean cp2) {}
		});
	}
	
	public void init() {
		setFocusTraversalKeysEnabled(false);
		root = new RootWidget(new Coord(w, h), this);
		ui = new UI(root, null);
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
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				synchronized(events) {
					events.add(e);
					events.notifyAll();
				}
			}
		});
		inited = true;
	}
	
	void redraw(GL gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		GOut g = new GOut(gl, getContext(), new Coord(800, 600));
		synchronized(ui) {
			root.draw(g);
		}
		g.atext("FPS: " + fps, new Coord(790, 590), 1, 1);
	}
	
/*
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
*/
	
	void dispatch() {
		synchronized(events) {
			while(events.size() > 0) {
				InputEvent e = events.remove(0);
				if(e instanceof MouseEvent) {
					MouseEvent me = (MouseEvent)e;
					if(me.getID() == MouseEvent.MOUSE_PRESSED) {
						if((me.getX() < 10) && (me.getY() < 10))
							throw(new RuntimeException("test"));
						ui.mousedown(new Coord(me.getX(), me.getY()), me.getButton());
					} else if(me.getID() == MouseEvent.MOUSE_RELEASED) {
						ui.mouseup(new Coord(me.getX(), me.getY()), me.getButton());
					} else if(me.getID() == MouseEvent.MOUSE_MOVED || me.getID() == MouseEvent.MOUSE_DRAGGED) {
						ui.mousemove(new Coord(me.getX(), me.getY()));
					} else if(me instanceof MouseWheelEvent) {
						ui.mousewheel(new Coord(me.getX(), me.getY()), ((MouseWheelEvent)me).getWheelRotation());
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
	
	public void uglyjoglhack() throws InterruptedException {
		try {
			display();
		} catch(GLException e) {
			if(e.getCause() instanceof InterruptedException) {
				throw((InterruptedException)e.getCause());
			} else {
				e.printStackTrace();
				throw(e);
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
					try {
						if(ui.sess != null)
							ui.sess.glob.oc.ctick();
						dispatch();
						//redraw();
					} catch(Throwable t) {
						t.printStackTrace();
					}
				}
				uglyjoglhack();
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
