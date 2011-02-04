package haven;

import java.awt.image.BufferedImage;

public class MinimapPanel extends Window {

    static final BufferedImage grip = Resource.loadimg("gfx/hud/gripbr");
    static final Coord gzsz = new Coord(16,17);
    static final Coord minsz = new Coord(100, 100);
    
    boolean rsm = false;
    MiniMap mm;
    public MinimapPanel(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent, "Minimap");
	mrgn = Coord.z;
	fbtn.visible = true;
	cbtn.visible = false;
	{
	    new IButton(new Coord(0, -2), this, Resource.loadimg("gfx/hud/slen/dispauth"), Resource.loadimg("gfx/hud/slen/dispauthd")) {
		private boolean v = false;
		
		public void click() {
		    MapView mv = ui.root.findchild(MapView.class);
		    BufferedImage tmp = down;
		    down = up;
		    up = tmp;
		    hover = tmp;
		    if(v) {
			mv.disol(2, 3);
			v = false;
		    } else {
			mv.enol(2, 3);
			v = true;
		    }
		}
	    };
	}
	{
	    new IButton(new Coord(0, 4), this, Resource.loadimg("gfx/hud/slen/dispclaim"), Resource.loadimg("gfx/hud/slen/dispclaimd")) {
		private boolean v = false;
		
		public void click() {
		    MapView mv = ui.root.findchild(MapView.class);
		    BufferedImage tmp = down;
		    down = up;
		    up = tmp;
		    hover = tmp;
		    if(v) {
			mv.disol(0, 1);
			v = false;
		    } else {
			mv.enol(0, 1);
			v = true;
		    }
		}
	    };
	}
	
	mm = new MiniMap(new Coord(0, 32), new Coord(125, 125), this, ui.mainview);
	pack();
	this.c = new Coord( MainFrame.getInnerSize().x - this.sz.x, 7);
    }
    
    protected void placecbtn() {
	fbtn.c = new Coord(wsz.x - 3 - Utils.imgsz(cbtni[0]).x, 3).add(mrgn.inv().add(wbox.tloff().inv()));
	//fbtn.c = new Coord(cbtn.c.x - 1 - Utils.imgsz(fbtni[0]).x, cbtn.c.y);
    }
    
    public void draw(GOut g) {
	super.draw(g);
	if(!folded)
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
    
    public boolean type(char key, java.awt.event.KeyEvent ev) {
	if(key == 27) {
	    wdgmsg(fbtn, "click");
	    return(true);
	}
	return(super.type(key, ev));
    }
    
}
