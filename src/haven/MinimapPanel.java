package haven;

import java.awt.image.BufferedImage;

public class MinimapPanel extends Window {

    static final BufferedImage grip = Resource.loadimg("gfx/hud/gripbr");
    static final Coord gzsz = new Coord(16,17);
    static final Coord minsz = new Coord(150, 125);
    
    boolean rsm = false;
    MiniMap mm;
    IButton btncave;
    
    public MinimapPanel(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent, "Minimap");
	mrgn = Coord.z;
	fbtn.visible = true;
	cbtn.visible = false;
	{
	    new IButton(new Coord(-3, -2), this, Resource.loadimg("gfx/hud/slen/dispauth"), Resource.loadimg("gfx/hud/slen/dispauthd")) {
		private boolean v = false;
		public void click() {
		    MapView mv = ui.mainview;
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
		
		private Text tooltip = Text.render("Display village claims");
		@Override
		public Object tooltip(Coord c, boolean again) {
		    return checkhit(c)?tooltip:null;
		}
	    };
	}
	{
	    new IButton(new Coord(-3, 4), this, Resource.loadimg("gfx/hud/slen/dispclaim"), Resource.loadimg("gfx/hud/slen/dispclaimd")) {
		private boolean v = false;
		public void click() {
		    MapView mv = ui.mainview;
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
		
		private Text tooltip = Text.render("Display personal claims");
		@Override
		public Object tooltip(Coord c, boolean again) {
		    return checkhit(c)?tooltip:null;
		}
		
	    };
	}
	
	mm = new MiniMap(new Coord(0, 32), minsz, this, ui.mainview);
	
	new IButton(new Coord(42, 8), this, Resource.loadimg("gfx/hud/buttons/gridu"), Resource.loadimg("gfx/hud/buttons/gridd")) {
	    
	    public void click() {
		BufferedImage tmp = down;
		down = up;
		up = tmp;
		hover = tmp;
		mm.grid = !mm.grid;
	    }
	    private Text tooltip = Text.render("Toggle grid");
	    @Override
	    public Object tooltip(Coord c, boolean again) {
		return checkhit(c)?tooltip:null;
	    }
	};
	
	new IButton(new Coord(62, 8), this, Resource.loadimg("gfx/hud/buttons/centeru"), Resource.loadimg("gfx/hud/buttons/centerd")) {
	    public void click() {
		mm.off = new Coord();
	    }
	    private Text tooltip = Text.render("Center map");
	    @Override
	    public Object tooltip(Coord c, boolean again) {
		return checkhit(c)?tooltip:null;
	    }
	};
	
	new IButton(new Coord(83, 8), this, Resource.loadimg("gfx/hud/buttons/simpleu"), Resource.loadimg("gfx/hud/buttons/simpled")) {
	    public void click() {
		BufferedImage tmp = down;
		down = up;
		up = tmp;
		hover = tmp;
		Config.simplemap = !Config.simplemap;
	    }
	    private Text tooltip = Text.render("Toggle simplified map");
	    @Override
	    public Object tooltip(Coord c, boolean again) {
		return checkhit(c)?tooltip:null;
	    }
	};
	
	new IButton(new Coord(103, 2), this, Resource.loadimg("gfx/hud/charsh/plusup"), Resource.loadimg("gfx/hud/charsh/plusdown")) {
	    public void click() {
		mm.setScale(mm.scale+1);
	    }

	    private Text tooltip = Text.render("Zoom in");
	    @Override
	    public Object tooltip(Coord c, boolean again) {
		return checkhit(c)?tooltip:null;
	    }
	};
	
	new IButton(new Coord(103, 16), this, Resource.loadimg("gfx/hud/charsh/minusup"), Resource.loadimg("gfx/hud/charsh/minusdown")) {
	    public void click() {
		mm.setScale(mm.scale-1);
	    }
	    private Text tooltip = Text.render("Zoom out");
	    @Override
	    public Object tooltip(Coord c, boolean again) {
		return checkhit(c)?tooltip:null;
	    }
	};
	
	btncave = new IButton(new Coord(121, 8), this, Resource.loadimg("gfx/hud/buttons/saveu"), Resource.loadimg("gfx/hud/buttons/saved")) {
	    public void click() {
		if(mm.isCave()){
		    mm.saveCaveMaps();
		} else {
		    mm.saveSimpleMaps();
		}
	    }
	    private Text tooltip = Text.render("Save map");
	    @Override
	    public Object tooltip(Coord c, boolean again) {
		return checkhit(c)?tooltip:null;
	    }
	};
	pack();
	this.c = new Coord( MainFrame.getInnerSize().x - this.sz.x, 7);
	loadpos();
    }
    
    private void loadpos(){
	synchronized (Config.window_props) {
	    c = new Coord(Config.window_props.getProperty("minimap_pos", c.toString()));
	    mm.sz = new Coord(Config.window_props.getProperty("minimap_sz", mm.sz.toString()));
	    pack();
	}
    }
    
    protected void placecbtn() {
	fbtn.c = new Coord(wsz.x - 3 - Utils.imgsz(cbtni[0]).x, 3).add(mrgn.inv().add(wbox.tloff().inv()));
	//fbtn.c = new Coord(cbtn.c.x - 1 - Utils.imgsz(fbtni[0]).x, cbtn.c.y);
    }
    
    public void draw(GOut g) {
	super.draw(g);
	btncave.visible = !folded && (mm.isCave() || Config.simplemap);
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
	if(dm){
	    Config.setWindowOpt("minimap_pos", this.c.toString());
	}
	if (rsm){
	    ui.grabmouse(null);
	    rsm = false;
	    Config.setWindowOpt("minimap_sz", mm.sz.toString());
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
