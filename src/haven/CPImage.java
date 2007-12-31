package haven;

import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.*;

public class CPImage {
    private boolean accel = true;
    private BufferedImage real;
    private VolatileImage me;
    private Graphical surf;
    static private boolean printed = false;
    
    public CPImage(BufferedImage back, Graphical surf) {
	real = back;
	this.surf = surf;
    }
    
    public void draw(Graphics g, Coord c) {
	outer: do {
	    if(accel) {
		do {
		    if(me == null) {
			recreate();
		    } else {
			int s = me.validate(surf.getconf());
			if(s == VolatileImage.IMAGE_RESTORED) {
			    redraw();
			} else if(s == VolatileImage.IMAGE_INCOMPATIBLE) {
			    recreate();
			}
		    }
		    if(me == null)
			continue outer;
		    g.drawImage(me, c.x, c.y, null);
		} while(me.contentsLost());
	    } else {
		g.drawImage(real, c.x, c.y, null);
	    }
	    break;
	} while(true);
    }
    
    private void redraw() {
	Graphics g = me.createGraphics();
	g.drawImage(real, 0, 0, null);
	g.dispose();
    }
    
    private void recreate() {
	try {
	    me = surf.getconf().createCompatibleVolatileImage(real.getWidth(), real.getHeight(), new ImageCapabilities(true), real.getTransparency());
	    synchronized(CPImage.class) {
		if(!printed) {
		    System.out.println("Accelerated :)");
		    printed = true;
		}
	    }
	} catch(java.awt.AWTException e) {
	    synchronized(CPImage.class) {
		if(!printed) {
		    System.out.println("Not accelerated :(");
		    printed = true;
		}
	    }
	    accel = false;
	    me = null;
	    return;
	}
	redraw();
    }
}
