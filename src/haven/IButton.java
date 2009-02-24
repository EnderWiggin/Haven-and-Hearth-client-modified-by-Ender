package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class IButton extends SSWidget {
    BufferedImage up, down, hover;
    boolean a = false, h = false;
	
    static {
	Widget.addtype("ibtn", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new IButton(c, parent, Resource.loadimg((String)args[0]), Resource.loadimg((String)args[1])));
		}
	    });
    }
	
    public IButton(Coord c, Widget parent, BufferedImage up, BufferedImage down, BufferedImage hover) {
	super(c, Utils.imgsz(up), parent);
	this.up = up;
	this.down = down;
	this.hover = hover;
	render();
    }
	
    public IButton(Coord c, Widget parent, BufferedImage up, BufferedImage down) {
	this(c, parent, up, down, up);
    }
	
    public void render() {
	Graphics g = graphics();
	if(a)
	    g.drawImage(down, 0, 0, null);
	else if(h)
	    g.drawImage(hover, 0, 0, null);
	else
	    g.drawImage(up, 0, 0, null);
	update();
    }

    public boolean checkhit(Coord c) {
	int cl = up.getRGB(c.x, c.y);
	return(Utils.rgbm.getAlpha(cl) >= 128);
    }
	
    public void click() {
	wdgmsg("activate");
    }
	
    public boolean mousedown(Coord c, int button) {
	if(button != 1)
	    return(false);
	if(!checkhit(c))
	    return(false);
	a = true;
	ui.grabmouse(this);
	render();
	return(true);
    }
	
    public boolean mouseup(Coord c, int button) {
	if(a && button == 1) {
	    a = false;
	    ui.grabmouse(null);
	    if(c.isect(new Coord(0, 0), sz) && checkhit(c))
		click();
	    render();
	    return(true);
	}
	return(false);
    }
	
    public void mousemove(Coord c) {
	boolean h = c.isect(Coord.z, sz);
	if(h != this.h) {
	    this.h = h;
	    render();
	}
    }
}
