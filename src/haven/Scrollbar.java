package haven;

import java.util.*;

public class Scrollbar extends Widget {
    static Tex texpap = Resource.loadtex("gfx/hud/texpap");
    static Tex schain = Resource.loadtex("gfx/hud/schain");
    static Tex sflarp = Resource.loadtex("gfx/hud/sflarp");
    public int val, min, max;
    private boolean drag = false;
    
    public Scrollbar(Coord c, int h, Widget parent, int min, int max) {
	super(c.add(-sflarp.sz().x, 0), new Coord(sflarp.sz().x, h), parent);
	this.min = min;
	this.max = max;
	val = min;
    }
    
    public void draw(GOut g) {
	if(max > min) {
	    int cx = (sflarp.sz().x / 2) - (schain.sz().x / 2);
	    for(int y = 0; y < sz.y; y += schain.sz().y - 1)
		g.image(schain, new Coord(cx, y));
	    double a = (double)val / (double)(max - min);
	    int fy = (int)((sz.y - sflarp.sz().y) * a);
	    g.image(sflarp, new Coord(0, fy));
	}
    }
    
    public boolean mousedown(Coord c, int button) {
	if(button != 1)
	    return(false);
	if(max <= min)
	    return(false);
	drag = true;
	ui.grabmouse(this);
	mousemove(c);
	return(true);
    }
    
    public void mousemove(Coord c) {
	if(drag) {
	    double a = (double)(c.y - (sflarp.sz().y / 2)) / (double)(sz.y - sflarp.sz().y);
	    if(a < 0)
		a = 0;
	    if(a > 1)
		a = 1;
	    val = (int)Math.round(a * (max - min)) + min;
	    changed();
	}
    }
    
    public boolean mouseup(Coord c, int button) {
	if(button != 1)
	    return(false);
	if(!drag)
	    return(false);
	drag = false;
	ui.grabmouse(null);
	return(true);
    }
    
    public void changed() {}
    
    public void ch(int a) {
	int val = this.val + a;
	if(val > max)
	    val = max;
	if(val < min)
	    val = min;
	this.val = val;
    }
}
