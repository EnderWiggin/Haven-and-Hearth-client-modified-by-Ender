package haven;

import java.applet.*;

public class HavenApplet extends Applet {
    Thread main;
    
    public void destroy() {
	main.interrupt();
    }
    
    public void init() {
	resize(800, 600);
	final HavenPanel h = new HavenPanel(800, 600);
	add(h);
	h.init();
	Bootstrap b = new Bootstrap(h.ui);
	b.setaddr("www.seatribe.se");
	b.start();
	main = new Thread() {
	    public void run() {
		h.loop();
	    }
	};
	main.start();
    }
}
