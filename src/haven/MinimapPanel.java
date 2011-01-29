package haven;

import java.awt.image.BufferedImage;

public class MinimapPanel extends Window {

    static final BufferedImage grip = Resource.loadimg("gfx/hud/grip");
    static final Coord gzsz = new Coord(16,17);
    static final Coord minsz = new Coord(100, 100);
    
    boolean rsm = false;
    MiniMap mm;
    public MinimapPanel(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent, "Minimap");
	fbtn.visible = true;
	mm = new MiniMap(Coord.z, new Coord(125, 125), this, ui.mainview);
	pack();
	this.c = new Coord( MainFrame.getInnerSize().x - this.sz.x, 7);
    }
    
    public void draw(GOut g) {
	super.draw(g);
	g.image(grip, sz.sub(gzsz));
    }
    
    public boolean mousedown(Coord c, int button) {
	if(folded) {
	    return super.mousedown(c, button);
	}
	parent.setfocus(this);
	raise();
	if (button == 1) {
	    ui.grabmouse(this);
	    doff = c;
	    if(c.isect(sz.sub(gzsz), gzsz)) {
		rsm = true;
		return true;
	    }
	}
	return super.mousedown(c, button);
    }

    public boolean mouseup(Coord c, int button) {
	if (rsm){
	    ui.grabmouse(null);
	    rsm = false;
	} else {
	    super.mouseup(c, button);
	}
	return (true);
    }
    
    public void mousemove(Coord c) {
	if (rsm){
	    Coord d = c.sub(doff);
	    mm.sz = mm.sz.add(d);
	    mm.sz.x = Math.max(minsz.x, mm.sz.x);
	    mm.sz.y = Math.max(minsz.y, mm.sz.y);
	    doff = c;
	    pack();
	} else {
	    super.mousemove(c);
	}
    }
    
}
