package haven;

import java.util.*;

public class Profwnd extends HWindow {
    public final Profile prof;
    public String hover;
    public long mt = 50000000;
    private static final int h = 80;
    
    public Profwnd(Widget parent, Profile prof, String title) {
	super(parent, title, true);
	this.prof = prof;
    }
    
    public void draw(GOut g) {
	long[] ttl = new long[prof.hist.length];
	for(int i = 0; i < prof.hist.length; i++) {
	    if(prof.hist[i] != null)
		ttl[i] = prof.hist[i].total;
	}
	Arrays.sort(ttl);
	int ti = ttl.length;
	for(int i = 0; i < ttl.length; i++) {
	    if(ttl[i] != 0) {
		ti = ttl.length - ((ttl.length - i) / 10);
		break;
	    }
	}
	if(ti < ttl.length)
	    mt = ttl[ti];
	else
	    mt = 50000000;
	g.image(prof.draw(h, mt / h), new Coord(10, 10));
	if(hover != null)
	    ui.tooltip = hover;
    }
    
    public void mousemove(Coord c) {
	hover = null;
	if((c.x >= 10) && (c.x < 10 + prof.hist.length) && (c.y >= 10) && (c.y < 10 + h)) {
	    int x = c.x - 10;
	    int y = c.y - 10;
	    long t = (h - y) * (mt / h);
	    Profile.Frame f = prof.hist[x];
	    if(f != null) {
		for(int i = 0; i < f.prt.length; i++) {
		    if((t -= f.prt[i]) < 0) {
			hover = String.format("%.2f ms, %s: %.2f ms", (((double)f.total) / 1000000), f.nm[i], (((double)f.prt[i]) / 1000000));
			break;
		    }
		}
	    }
	}
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == cbtn) {
	    ui.destroy(this);
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
}
