package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHWPanel extends Widget implements IHWindowParent {

    public static ChatHWPanel instance;
    static BufferedImage[] fbtni = new BufferedImage[] {
	    Resource.loadimg("gfx/hud/fbtn"),
	    Resource.loadimg("gfx/hud/fbtnd"),
	    Resource.loadimg("gfx/hud/fbtnh") };
    HWindow awnd;
    List<HWindow> wnds = new ArrayList<HWindow>();
    Map<HWindow, Button> btns = new HashMap<HWindow, Button>();
    Button sub, sdb;
    IButton fbtn;
    int urgency, woff = 0, bpp = 6;
    boolean folded = false;

    public ChatHWPanel(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent);
	instance = this;
	sub = new Button(new Coord(300, 260), 50, this,
		Resource.loadimg("gfx/hud/slen/sau")) {
	    public void click() {
		sup();
	    }
	};
	sdb = new Button(new Coord(300, 280), 50, this,
		Resource.loadimg("gfx/hud/slen/sad")) {
	    public void click() {
		sdn();
	    }
	};
	sub.visible = sdb.visible = false;
	fbtn = new IButton(Coord.z, this, fbtni[0], fbtni[1], fbtni[2]);
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
	Coord c = folded ? new Coord(0, 245) : Coord.z;
	fbtn.c = c;
	g.chcolor(220, 220, 200, folded?32:200);
	g.frect(c, sz.sub(c));
	if(folded)
	    g.chcolor(255,255,255,160);
	else
	    g.chcolor();
	super.draw(g);
	g.chcolor(64, 64, 64, folded?32:255);
	g.rect(c, sz.add(new Coord(1, 1).sub(c)));
	g.chcolor();
	if ((folded) && (SlenHud.urgcols[urgency] != null)) {
	    g.chcolor(SlenHud.urgcols[urgency]);
	    g.image(fbtni[0], c);
	    g.chcolor();
	}
    }

    private void updbtns() {
	if (wnds.size() <= bpp) {
	    woff = 0;
	} else {
	    if (woff < 0)
		woff = 0;
	    if (woff > wnds.size() - bpp)
		woff = wnds.size() - bpp;
	}
	for (Button b : btns.values())
	    b.visible = false;
	sub.visible = sdb.visible = false;
	for (int i = 0; i < 6; i++) {
	    int wi = i + woff;
	    if (wi >= wnds.size())
		continue;
	    if (woff > 0)
		sub.visible = true;
	    if (woff < wnds.size() - bpp)
		sdb.visible = true;
	    HWindow w = wnds.get(wi);
	    Button b = btns.get(w);
	    b.change(w.title, w.visible ? Color.WHITE
		    : SlenHud.urgcols[w.urgent]);
	    b.visible = true;
	    b.c = new Coord(100 * (i % 3), 260 + ((int) i / 3) * 20);
	}
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if (sender == fbtn) {
	    folded = !folded;
	    if(awnd != null)
		awnd.visible = !folded;
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    @Override
    public void addwnd(final HWindow wnd) {
	fbtn.raise();
	wnd.sz = sz.sub(new Coord(0, 40));
	wnd.c = Coord.z;
	wnds.add(wnd);
	btns.put(wnd, new Button(new Coord(0, 260), 100, this, wnd.title) {
	    public void click() {
		setawnd(wnd, true);
	    }
	});
	if(!folded)
	    setawnd(wnd);
	else
	    wnd.visible = false;
	    updbtns();
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
	if ((wnd == awnd) && !folded)
	    level = -1;
	if (level == -1) {
	    if (wnd.urgent == 0)
		return;
	    wnd.urgent = 0;
	} else {
	    if (wnd.urgent >= level)
		return;
	    wnd.urgent = level;
	}
	Button b = btns.get(wnd);
	b.change(wnd.title, SlenHud.urgcols[wnd.urgent]);
	int max = 0;
	for (HWindow w : wnds) {
	    if (w.urgent > max)
		max = w.urgent;
	}
	urgency = (level>0)?level:0;
    }

    @Override
    public void setawnd(HWindow wnd) {
	setawnd(wnd, false);
    }

    @Override
    public void setawnd(HWindow wnd, boolean focus) {
	if (focus)
	    folded = false;
	awnd = wnd;
	for (HWindow w : wnds)
	    w.visible = false;
	if (wnd != null) {
	    wnd.visible = !folded;
	    updurgency(wnd, -1);
	}
	updbtns();
    }
}
