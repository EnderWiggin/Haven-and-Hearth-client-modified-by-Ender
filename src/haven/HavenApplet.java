package haven;

import java.applet.*;
import java.awt.*;

public class HavenApplet extends Applet {
    ThreadGroup p;
    HavenPanel h;
    
    private class ErrorPanel extends Canvas implements haven.error.ErrorStatus {
	String status = "";
	
	public ErrorPanel() {
	    setBackground(Color.BLACK);
	}
	
	public void goterror(Throwable t) {
	    p.interrupt();
	    HavenApplet.this.remove(h);
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
	    repaint();
	}
	
	public void senderror(Exception e) {
	    status = "Could not send error report";
	    repaint();
	}
	
	public void paint(Graphics g) {
	    g.setColor(Color.BLACK);
	    g.fillRect(0, 0, getWidth(), getHeight());
	    g.setColor(Color.WHITE);
	    FontMetrics m = g.getFontMetrics();
	    int y = 0;
	    g.drawString("An error has occurred.", 0, y + m.getAscent());
	    y += m.getHeight();
	    g.drawString(status, 0, y + m.getAscent());
	}
    }
    
    public void destroy() {
	p.interrupt();
	if(Session.current != null)
	    Session.current.close();
    }
    
    public void init() {
	resize(800, 600);
	h = new HavenPanel(800, 600);
	add(h);
	h.init();
	p = new haven.error.ErrorHandler(new Runnable() {
		public void run() {
		    Bootstrap b = new Bootstrap(h.ui);
		    b.setaddr("www.seatribe.se");
		    b.start();
		    Thread main = new Thread(Utils.tg(), h, "Haven applet main thread");
		    main.start();
		}
	    }, new ErrorPanel());
    }
}
