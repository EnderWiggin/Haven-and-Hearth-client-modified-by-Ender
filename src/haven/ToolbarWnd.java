package haven;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


public class ToolbarWnd extends Window implements DTarget, DropTarget {
    public final static Tex bg = Resource.loadtex("gfx/hud/invsq");
    private static final int BELTS_NUM = 10;
    private static final BufferedImage ilockc = Resource.loadimg("gfx/hud/lockc");
    private static final BufferedImage ilockch = Resource.loadimg("gfx/hud/lockch");
    private static final BufferedImage ilocko = Resource.loadimg("gfx/hud/locko");
    private static final BufferedImage ilockoh = Resource.loadimg("gfx/hud/lockoh");
    public final static Coord bgsz = bg.sz().add(-1, -1);
    private static final Properties beltsConfig = new Properties();
    public static MenuGrid mnu;
    private Coord gsz, off;
    Resource pressed, dragging, layout[];
    private IButton lockbtn, flipbtn;
    public boolean flipped = false, locked = false;
    private int belt;
    
    public final static RichText.Foundry ttfnd = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
    
    public ToolbarWnd(Coord c, Widget parent) {
	super( c, Coord.z,  parent, null);
	init(1, 10, new Coord(5, 10), KeyEvent.VK_0);
    }
    
    public ToolbarWnd(Coord c, Widget parent, int belt, int sz, Coord off, int key) {
	super( c, Coord.z,  parent, null);
	init(belt, sz, off, key);
    }
    
    private void init(int belt, int sz, Coord off, int key) {
	lockbtn = new IButton(Coord.z, this, ilocko, ilockc, ilockoh) {
		
		public void click() {
		    locked = !locked;
		    if(locked) {
			up = ilockc;
			down = ilocko;
			hover = ilockch;
		    } else {
			up = ilocko;
			down = ilockc;
			hover = ilockoh;
		    }
		}
	};
	lockbtn.recthit = true;
	flipbtn = new IButton(Coord.z, this, Resource.loadimg("gfx/hud/flip"), Resource.loadimg("gfx/hud/flip"), Resource.loadimg("gfx/hud/flipo")) {
		public void click() {
		    flip();
		}
	};
	flipbtn.recthit = true;
	gsz = new Coord(1, sz);
	this.off = off;
	fbtn.show();
	mrgn = new Coord(2,18);
	layout = new Resource[sz];
	loadBelt(belt);
	pack();
    }
    
    public static void loadBelts() {
	
	String configFileName = "belts_" + Config.currentCharName.replaceAll("[^a-zA-Z()]", "_") + ".conf";
	try {
	    synchronized (beltsConfig) {
		beltsConfig.load(new FileInputStream(configFileName));
	    }
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}
    }
    
    private void loadBelt(int beltNr) {
	belt = beltNr % BELTS_NUM;
	synchronized (beltsConfig) {
	    for (int slot = 0; slot < layout.length; slot++) {
		String icon = beltsConfig.getProperty("belt_" + belt + "_"
			+ slot, "");
		if (!icon.isEmpty()) {
		    layout[slot] = Resource.load(icon);
		}
	    }
	}
    }
    
    public static void saveBelts() {
	synchronized (beltsConfig) {
	    String configFileName = "belts_" + Config.currentCharName.replaceAll("[^a-zA-Z()]", "_") + ".conf";
	    try {
		beltsConfig.store(new FileOutputStream(configFileName), "Belts actions for " + Config.currentCharName);
	    } catch (FileNotFoundException e) {
	    } catch (IOException e) {
	    }
	}
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == cbtn)
	    ui.destroy(this);
	if(sender == fbtn)
	    super.wdgmsg(sender, msg, args);
    }
    
    public void draw(GOut g) {
	super.draw(g);
	if(folded)
	    return;
	for(int y = 0; y < gsz.y; y++) {
	    for(int x = 0; x < gsz.x; x++) {
		Coord p = getcoord(x, y);
		g.image(bg, p);
		Resource btn = layout[x+y];
		if(btn != null) {
		    Tex btex = btn.layer(Resource.imgc).tex();
		    g.image(btex, p.add(1, 1));
		    if(btn == pressed) {
			g.chcolor(new Color(0, 0, 0, 128));
			g.frect(p.add(1, 1), btex.sz());
			g.chcolor();
		    }
		}
	    }
	}
	g.chcolor();
	if(dragging != null) {
	    final Tex dt = dragging.layer(Resource.imgc).tex();
	    ui.drawafter(new UI.AfterDraw() {
		    public void draw(GOut g) {
			g.image(dt, ui.mc.add(dt.sz().div(2).inv()));
		    }
		});
	}
    } 
    
    private Coord getcoord(int x, int y) {
	Coord p = xlate(bgsz.mul(new Coord(x, y)),true);
	if (off.x > 0)
	    if (flipped) {
		p.x += off.y*(x/off.x);
	    } else {
		p.y += off.y*(y/off.x);
	    }
	return p;
    }
    
    public void checkfold() {
	super.checkfold();
	Coord max = new Coord(ssz);
	if((folded)&&(flipped)) {
	    max.x = 0;
	    recalcsz(max);
	}
	placecbtn();
    }
    
    protected void recalcsz(Coord max)
    {
	sz = max.add(wbox.bsz().add(mrgn.mul(2)).add(tlo).add(rbo)).add(-1, -1);
	wsz = sz.sub(tlo).sub(rbo);
	if(folded)
	    if (flipped)
		wsz.x = wsz.x/2;
	    else
		wsz.y = wsz.y/2;
	asz = wsz.sub(wbox.bl.sz()).sub(wbox.br.sz()).sub(mrgn.mul(2));
    }
    
    public void flip() {
	flipped = !flipped;
	gsz = new Coord(gsz.y, gsz.x);
	mrgn = new Coord(mrgn.y, mrgn.x);
	pack();
    }
    
    protected void placecbtn() {
	cbtn.c = new Coord(wsz.x - 3 - Utils.imgsz(cbtni[0]).x, 3).sub(mrgn).sub(wbox.tloff());
	if(flipped) {
	    fbtn.c = new Coord(cbtn.c.x, wsz.y - 3 - Utils.imgsz(fbtni[0]).y - mrgn.y - wbox.tloff().y);
	    if(lockbtn != null)
		lockbtn.c = new Coord(3 - wbox.tloff().x - mrgn.x, cbtn.c.y );
	    if(flipbtn != null)
		flipbtn.c = new Coord(5 - wbox.tloff().x - mrgn.x, fbtn.c.y);
	} else {
	    fbtn.c = new Coord(3 - wbox.tloff().x, cbtn.c.y);
	    if(lockbtn != null)
		lockbtn.c = new Coord(fbtn.c.x, wsz.y - 21 - mrgn.y - wbox.tloff().y );
	    if(flipbtn != null)
		flipbtn.c = new Coord(cbtn.c.x - 2, wsz.y - 21 - mrgn.y - wbox.tloff().y);
	}
    }
    
    public void pack() {
	ssz = bgsz.mul(gsz);
	if (off.x > 0)
	    if (flipped) {
		ssz.x += off.y*((gsz.x/off.x) - ((gsz.x%off.x == 0)?1:0));
	    } else {
		ssz.y += off.y*((gsz.y/off.x) - ((gsz.y%off.x == 0)?1:0));
	    }
	checkfold();
	placecbtn();
    }
    
    private Resource bhit(Coord c) {
	int i = index(c);
	if (i >= 0)
	    return (layout[i]);
	else
	    return (null);
    }

    private int index(Coord c) {
	for(int y = 0; y < gsz.y; y++) {
	    for(int x = 0; x < gsz.x; x++) {
		if (c.isect(getcoord(x, y), bgsz))
		    return x+y;
	    }
	}
	return -1;
    }
    
    public boolean mousedown(Coord c, int button) {
	Resource h = bhit(c);
	if (button == 1) {
	    if (h != null) {
		pressed = h;
		ui.grabmouse(this);
	    } else {
		super.mousedown(c, button);
	    }
	}
	return (true);
    }

    public boolean mouseup(Coord c, int button) {
	Resource h = bhit(c);
	if (button == 1) {
	    if(dragging != null) {
		ui.dropthing(ui.root, ui.mc, dragging);
		dragging = pressed = null;
	    } else if (pressed != null) {
		if (pressed == h)
		    if(mnu != null)
			mnu.use(h);
		pressed = null;
	    }
	    ui.grabmouse(null);
	}
	super.mouseup(c, button);
	return (true);
    }
    
    public void mousemove(Coord c) {
	if ((!locked)&&(dragging == null) && (pressed != null)) {
	    dragging = pressed;
	    pressed = layout[index(c)] = null;
	} else {
	    super.mousemove(c);
	}
	    
    }
    
    public boolean drop(Coord cc, Coord ul) {
	return(true);
    }
	
    public boolean iteminteract(Coord cc, Coord ul) {
	return(true);
    }
    
    public boolean dropthing(Coord c, Object thing) {
	if ((!locked)&&(thing instanceof Resource)) {
	    layout[index(c)] = (Resource) thing;
	    return true;
	}
	return false;
    }
    
    private Resource curttr = null;
    private boolean curttl = false;
    private Text curtt = null;
    private long hoverstart;
    public Object tooltip(Coord c, boolean again) {
	Resource res = bhit(c);
	long now = System.currentTimeMillis();
	if((res != null) && (res.layer(Resource.action) != null)) {
	    if(!again)
		hoverstart = now;
	    boolean ttl = (now - hoverstart) > 500;
	    if((res != curttr) || (ttl != curttl)) {
		curtt = rendertt(res, ttl);
		curttr = res;
		curttl = ttl;
	    }
	    return(curtt);
	} else {
	    hoverstart = now;
	    return("");
	}
    }
    
    private static Text rendertt(Resource res, boolean withpg) {
	Resource.AButton ad = res.layer(Resource.action);
	Resource.Pagina pg = res.layer(Resource.pagina);
	String tt = ad.name;
	if(withpg && (pg != null)) {
	    tt += "\n\n" + pg.text;
	}
	return(ttfnd.render(tt, 0));
    }
    
    public boolean type(char key, java.awt.event.KeyEvent ev) {
	if(key == 27) {
	    wdgmsg(fbtn, "click");
	    return(true);
	}
	return(super.type(key, ev));
    }
}
