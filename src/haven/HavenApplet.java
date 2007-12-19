package haven;

import java.applet.*;

public class HavenApplet extends Applet {
    ThreadGroup p;
    HavenPanel h;
    
    public void destroy() {
	p.interrupt();
	if(Session.current != null)
	    Session.current.close();
    }
    
    public void start() {
	h = new HavenPanel(800, 600);
	add(h);
	h.init();
	p = new haven.error.ErrorHandler(new Runnable() {
		public void run() {
		    Bootstrap b = new Bootstrap(h.ui);
		    b.setaddr("www.seatribe.se");
		    b.start();
		    Thread main = new Thread(h, "Haven applet main thread");
		    main.start();
		}
	    });
    }

    public void init() {
	resize(800, 600);
	start();
    }
}
