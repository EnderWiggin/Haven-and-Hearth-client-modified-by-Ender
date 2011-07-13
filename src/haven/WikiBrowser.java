package haven;

import haven.Resource.AButton;
import haven.Resource.Tooltip;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikiBrowser extends Window implements DTarget2, DropTarget, IHWindowParent {
    static final BufferedImage grip = Resource.loadimg("gfx/hud/gripbr");
    static final Coord gzsz = new Coord(16,17);
    static final Coord minsz = new Coord(230, 150);
    static final int addrh = 40;
    static final int minbtnw = 90;
    static final int maxbtnw = 200;
    static final int sbtnw = 50;
    static final int btnh = 30;
    
    boolean rsm = false, recalcsz = true;
    Button sub, sdb;
    HWindow awnd;
    List<HWindow> wnds = new ArrayList<HWindow>();
    Map<HWindow, Button> btns = new HashMap<HWindow, Button>();
    int woff = 0;
    Coord btnc;
    TextEntry search;
    
    
    public WikiBrowser(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent, "Wiki");
	ssz = new Coord(minsz);
	ui.wiki = this;
	mrgn = Coord.z;
	btnc = Coord.z.add(0, addrh);
	search = new TextEntry(new Coord(5,15), new Coord(sz.x, 20), this, "");
	sub = new Button(new Coord(300, 260), sbtnw, this,
		Resource.loadimg("gfx/hud/slen/sau")) {
	    public void click() {
		sup();
	    }
	};
	sdb = new Button(new Coord(300, 280), sbtnw, this,
		Resource.loadimg("gfx/hud/slen/sad")) {
	    public void click() {
		sdn();
	    }
	};
	sub.visible = sdb.visible = false;
	fbtn = new IButton(Coord.z, this, fbtni[0], fbtni[1], fbtni[2]);
	
	pack();
    }

    private void sup() {
	woff--;
	updbtns();
    }

    private void sdn() {
	woff++;
	updbtns();
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
	Coord s = ssz.sub(0, btnh  + gzsz.y + addrh);
	for (int i = 0; i < wnds.size(); i++) {
	    HWindow wnd = wnds.get(i);
	    wnd.setsz(s);
	}
	search.sz = new Coord(s.x-10, 20);
	updbtns();
    }
    
    public boolean type(char key, java.awt.event.KeyEvent ev) {
	if(key == 27) {
	    wdgmsg(fbtn, "click");
	    return(true);
	}
	if((key == 10) && (focused == search)) {
	    open(search.text);
	    return true;
	}
	return(super.type(key, ev));
    }
    
    private void open(String request) {
	new WikiPage(this, request, true);
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if (sender == cbtn) {
	    while(wnds.size() > 0) {
		ui.destroy(wnds.get(0));
	    }
	    ui.destroy(this);
	    ui.wiki = null;
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }
    
    public boolean drop(Coord cc, Coord ul, Item item) {
	//ui.slen.wdgmsg("setbelt", 1, 0);
	String name = item.name();
	if(name != null){
	    open(name);
	}
	return(true);
    }
    
    public boolean dropthing(Coord c, Object thing) {
	if (thing instanceof Resource) {
	    Resource res = (Resource)thing;
	    String name = null;
	    Tooltip tt = res.layer(Resource.tooltip);
	    if(tt!=null){
		name = tt.t;
	    } else {
		AButton ad = res.layer(Resource.action);
		if(ad != null) {
		    name = ad.name;
		}
	    }
	    if(name!=null)
		open(name);
	    return true;
	}
	return false;
    }
    
    @Override
    public void addwnd(final HWindow wnd) {
	wnd.sz = ssz.sub(0, btnh + gzsz.y + addrh);
	wnd.c = new Coord(0, btnh + gzsz.y + addrh);
	wnds.add(wnd);
	Button btn = new Button(new Coord(), maxbtnw+1, this, wnd.title) {
	    public void click() {
		setawnd(wnd, true);
	    }
	};
	btns.put(wnd, btn);
	setawnd(wnd);
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
	updbtns();
	
    }

    @Override
    public void updurgency(HWindow wnd, int level) {
	btns.get(wnd).change(wnd.title, wnd.visible ? Color.WHITE:null);	
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
	updbtns();
    }
    
    private void updbtns() {
	int ws = wnds.size();
	int k = Math.max((ssz.x - sbtnw) / minbtnw, 1);
	if (k > (ws >> 1)) {
	    k = Math.max(ws >> 1, 1);
	    if ((ws % 2) != 0)
		k++;
	}
	int bw = Math.min((ssz.x - sbtnw) / k, maxbtnw);
	int bpp = 2 * k;

	if (ws <= bpp) {
	    woff = 0;
	} else {
	    if (woff < 0)
		woff = 0;
	    if (woff > ws - bpp)
		woff = ws - bpp;
	}
	for (Button b : btns.values())
	    b.visible = false;
	sub.visible = sdb.visible = false;
	for (int i = 0; i < bpp; i++) {
	    int wi = i + woff;
	    if (wi >= ws)
		continue;
	    if (woff > 0) {
		sub.visible = true;
		sub.c = btnc.add(ssz.x - sbtnw, 0);
	    }
	    if (woff < ws - bpp) {
		sdb.visible = true;
		sdb.c = btnc.add(ssz.x - sbtnw, 20);
	    }
	    HWindow w = wnds.get(wi);
	    Button b = btns.get(w);
	    //w.sz = ssz.sub(0, btnh + addrh+gzsz.y);
	    b.change(w.title, w.visible ? Color.WHITE:null);
	    b.visible = true;
	    b.sz = new Coord(bw, b.sz.y);
	    b.c = btnc.add(bw * (i % k), ((int) i / k) * 20);
	}
    }

    @Override
    public HWindow getawnd() {
	return awnd;
    }
}
