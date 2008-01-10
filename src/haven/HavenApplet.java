package haven;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class HavenApplet extends Applet {
    ThreadGroup p;
    HavenPanel h;
    boolean running = false;
    
    private class ErrorPanel extends Canvas implements haven.error.ErrorStatus {
	String status = "";
	boolean ar = false;
	
	public ErrorPanel() {
	    setBackground(Color.BLACK);
	    addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
			if(ar && !running) {
			    HavenApplet.this.remove(ErrorPanel.this);
			    startgame();
			}
		    }
		});
	}
	
	public void goterror(Throwable t) {
	    stopgame();
	    setSize(HavenApplet.this.getSize());
	    HavenApplet.this.add(this);
	    repaint();
	}
	
	public void connecting() {
	    status = "Connecting to error report server...";
	    repaint();
	}
	
	public void sending() {
	    status = "Sending error report...";
	    repaint();
	}
	
	public void done() {
	    status = "Done";
	    ar = true;
	    repaint();
	}
	
	public void senderror(Exception e) {
	    status = "Could not send error report";
	    ar = true;
	    repaint();
	}
	
	public void paint(Graphics g) {
	    g.setColor(getBackground());
	    g.fillRect(0, 0, getWidth(), getHeight());
	    g.setColor(Color.WHITE);
	    FontMetrics m = g.getFontMetrics();
	    int y = 0;
	    g.drawString("An error has occurred.", 0, y + m.getAscent());
	    y += m.getHeight();
	    g.drawString(status, 0, y + m.getAscent());
	    y += m.getHeight();
	    if(ar) {
		g.drawString("Click to restart the game", 0, y + m.getAscent());
		y += m.getHeight();
	    }
	}
    }
    
    public void destroy() {
	p.interrupt();
    }
    
    public void startgame() {
	if(running)
	    return;
	h = new HavenPanel(800, 600);
	add(h);
	h.init();
	p = new haven.error.ErrorHandler(new ErrorPanel());
	Thread main = new Thread(p, new Runnable() {
		public void run() {
		    Bootstrap b = new Bootstrap(h.ui);
		    b.setaddr("www.seatribe.se");
		    b.start();
		    Thread main = new Thread(Utils.tg(), h, "Haven applet main thread");
		    main.start();
		}
	    });
	main.start();
	running = true;
    }
    
    public void stopgame() {
	if(!running)
	    return;
	p.interrupt();
	remove(h);
	p = null;
	h = null;
	running = false;
    }
    
    public void init() {
	resize(800, 600);
	startgame();
    }
}
