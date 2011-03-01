package haven;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikiBrowser extends Window implements IHWindowParent {
    static final BufferedImage grip = Resource.loadimg("gfx/hud/gripbr");
    static final Coord gzsz = new Coord(16,17);
    static final Coord minsz = new Coord(150, 150);
    private static final int btnh = 30;
    
    boolean rsm = false, recalcsz = false;
    HWindow awnd;
    List<HWindow> wnds = new ArrayList<HWindow>();
    Map<HWindow, Button> btns = new HashMap<HWindow, Button>();
    
    public WikiBrowser(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent, "Wiki");
	ui.wiki = this;
	mrgn = Coord.z;
    }
    
    public void draw(GOut g) {
	if(recalcsz){
	    recalcsz = false;
	    deltasz();
	}
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
	    deltasz();
	} else {
	    super.mouseup(c, button);
	}
	return (true);
    }
    
    public void mousemove(Coord c) {
	if (rsm){
	    Coord d = c.sub(doff);
	    doff = c;
	    ssz = ssz.add(d);
	    ssz.x = Math.max(ssz.x, minsz.x);
	    ssz.y = Math.max(ssz.y, minsz.y);
	    pack();
	} else {
	    super.mousemove(c);
	}
    }
    
    public void pack() {
	checkfold();
	placecbtn();
    }
    
    private void deltasz() {
	Coord s = ssz.sub(0, btnh  + gzsz.y);
	for (int i = 0; i < wnds.size(); i++) {
	    HWindow wnd = wnds.get(i);
	    wnd.setsz(s);
	}
    }
    
    public boolean type(char key, java.awt.event.KeyEvent ev) {
	if(key == 27) {
	    wdgmsg(fbtn, "click");
	    return(true);
	}
	return(super.type(key, ev));
    }
    
    @Override
    public void addwnd(final HWindow wnd) {
	wnd.sz = sz.sub(0, btnh  + gzsz.y);
	wnd.c = new Coord(0, gzsz.y+btnh);
	wnds.add(wnd);
	btns.put(wnd, new Button(Coord.z, 100, this, wnd.title) {
	    public void click() {
		setawnd(wnd, true);
	    }
	});
	if (!folded)
	    setawnd(wnd);
	else
	    wnd.visible = false;
	recalcsz = true;
    }

    @Override
    public void remwnd(HWindow wnd) {
	if (wnd == awnd) {
	    int i = wnds.indexOf(wnd);
	    if (wnds.size() == 1)
		setawnd(null);
	    else if (i < 0)
		setawnd(wnds.get(0));
	    else if (i >= wnds.size() - 1)
		setawnd(wnds.get(i - 1));
	    else
		setawnd(wnds.get(i + 1));
	}
	wnds.remove(wnd);
	ui.destroy(btns.get(wnd));
	btns.remove(wnd);
	//updbtns();
	
    }

    @Override
    public void updurgency(HWindow wnd, int level) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void setawnd(HWindow wnd) {
	setawnd(wnd, true);
	
    }

    @Override
    public void setawnd(HWindow wnd, boolean focus) {
	awnd = wnd;
	for (HWindow w : wnds)
	    w.visible = false;
	if (wnd != null) {
	    wnd.visible = !folded;
	    updurgency(wnd, -1);
	}
	//updbtns();
    }
}
