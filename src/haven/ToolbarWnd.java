package haven;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class ToolbarWnd extends Window implements DTarget, DropTarget {
    public final static Tex bg = Resource.loadtex("gfx/hud/invsq");
    public final static Coord bgsz = bg.sz().add(-1, -1);
    private static Coord gsz = new Coord(1, 10);
    Resource pressed, dragging, layout[];
    MenuGrid mnu;
    public boolean flipped = false;
    
    public final static RichText.Foundry ttfnd = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
    
    public ToolbarWnd(Coord c, Widget parent, Object... args) {
	super( c, Coord.z,  parent, null);
	fbtn.show();
	mrgn = new Coord(0,13);
	layout = new Resource[gsz.x*gsz.y];
	loadBelt(2);
	mnu = (MenuGrid)args[0];
	pack();
    }
    
    private void loadBelt(int beltNr) {
        String configFileName = "belts_" + Config.currentCharName.replaceAll("[^a-zA-Z()]", "_") + ".conf";
        File inputFile = new File(configFileName);
        if (!inputFile.exists()) {
            return;
        }
        Properties configFile = new Properties();
        try {
            configFile.load(new FileInputStream(configFileName));
                for (int slot  = 0; slot < 10; slot++) {
                    String icon = configFile.getProperty("belt_" + beltNr + "_" + slot, "");
                    if (!icon.isEmpty()) {
                        layout[slot] = Resource.load(icon);
                    }
                }
        }
        catch (IOException e) {
            System.out.println(e);
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
		Coord p = xlate(bgsz.mul(new Coord(x, y)),true);
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
	if(dragging != null) {
	    final Tex dt = dragging.layer(Resource.imgc).tex();
	    ui.drawafter(new UI.AfterDraw() {
		    public void draw(GOut g) {
			g.image(dt, ui.mc.add(dt.sz().div(2).inv()));
		    }
		});
	}
    } 
    
    public void flip() {
	flipped = !flipped;
	gsz = new Coord(gsz.y, gsz.x);
	mrgn = new Coord(mrgn.y, mrgn.x);
	pack();
    }
    
    protected void placecbtn() {
	if(flipped) {
	    cbtn.c = new Coord(3, 3).sub(mrgn).sub(wbox.tloff());
	    cbtn.c.x -= wbox.tloff().x;
	    fbtn.c = new Coord(cbtn.c.x, wsz.y - 3 - Utils.imgsz(fbtni[0]).y - mrgn.y - wbox.tloff().y);
	} else {
	    cbtn.c = new Coord(wsz.x - 3 - Utils.imgsz(cbtni[0]).x, 3).sub(mrgn).sub(wbox.tloff());
	    fbtn.c = new Coord(3 - wbox.tloff().x, cbtn.c.y);
	}
    }
    
    public void pack() {
	ssz = bgsz.mul(gsz);
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
	Coord bc = xlate(c, false).div(bgsz);
	if ((bc.x >= 0) && (bc.y >= 0) && (bc.x < gsz.x) && (bc.y < gsz.y))
	    return bc.x + bc.y;
	else
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
		    mnu.use(h);
		pressed = null;
	    }
	    ui.grabmouse(null);
	}
	super.mouseup(c, button);
	return (true);
    }
    
    public void mousemove(Coord c) {
	if ((dragging == null) && (pressed != null)) {
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
	if (thing instanceof Resource) {
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
}
