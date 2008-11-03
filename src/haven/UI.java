package haven;

import java.util.*;
import java.awt.event.KeyEvent;

public class UI {
    public RootWidget root;
    private Widget keygrab, mousegrab;
    public Map<Integer, Widget> widgets = new TreeMap<Integer, Widget>();
    public Map<Widget, Integer> rwidgets = new HashMap<Widget, Integer>();
    Receiver rcvr;
    public Coord mc, lcc = Coord.z;
    public Session sess;
    public MapView mainview;
    public boolean modshift, modctrl, modmeta, modsuper;
    long lastevent = System.currentTimeMillis();
    public Widget mouseon;
    public Object tooltip = null;
    public FSMan fsm;
    private Collection<AfterDraw> afterdraws = null;
	
    public interface Receiver {
	public void rcvmsg(int widget, String msg, Object... args);
    }
    
    public interface AfterDraw {
	public void draw(GOut g);
    }
	
    @SuppressWarnings("serial")
    public static class UIException extends RuntimeException {
	public String mname;
	public Object[] args;
		
	public UIException(String message, String mname, Object... args) {
	    super(message);
	    this.mname = mname;
	    this.args = args;
	}
    }
	
    public UI(Coord sz, Session sess) {
	root = new RootWidget(this, sz);
	widgets.put(0, root);
	rwidgets.put(root, 0);
	this.sess = sess;
    }
	
    public void setreceiver(Receiver rcvr) {
	this.rcvr = rcvr;
    }
	
    public void bind(Widget w, int id) {
	widgets.put(id, w);
	rwidgets.put(w, id);
    }
    
    public void drawafter(AfterDraw ad) {
	synchronized(afterdraws) {
	    afterdraws.add(ad);
	}
    }

    public void draw(GOut g) {
	afterdraws = new LinkedList<AfterDraw>();
	root.draw(g);
	synchronized(afterdraws) {
	    for(AfterDraw ad : afterdraws) {
		ad.draw(g);
	    }
	}
	afterdraws = null;
    }
	
    public void newwidget(int id, String type, Coord c, int parent, Object... args) throws InterruptedException {
	WidgetFactory f;
	if(type.indexOf('/') >= 0) {
	    Resource res = Resource.load(type);
	    res.loadwaitint();
	    f = res.layer(Resource.CodeEntry.class).wdg();
	} else {
	    f = Widget.gettype(type);
	}
	synchronized(this) {
	    Widget pwdg = widgets.get(parent);
	    if(pwdg == null)
		throw(new UIException("Null parent widget " + parent + " for " + id, type, args));
	    Widget wdg = f.create(c, pwdg, args);
	    bind(wdg, id);
	    if(wdg instanceof MapView)
		mainview = (MapView)wdg;
	}
    }
	
    public void grabmouse(Widget wdg) {
	mousegrab = wdg;
    }
	
    public void grabkeys(Widget wdg) {
	keygrab = wdg;
    }
	
    private void removeid(Widget wdg) {
	if(rwidgets.containsKey(wdg)) {
	    int id = rwidgets.get(wdg);
	    widgets.remove(id);
	    rwidgets.remove(wdg);
	}
	for(Widget child = wdg.child; child != null; child = child.next)
	    removeid(child);
    }
	
    public void destroy(Widget wdg) {
	if((mousegrab != null) && mousegrab.hasparent(wdg))
	    mousegrab = null;
	if((keygrab != null) && keygrab.hasparent(wdg))
	    keygrab = null;
	removeid(wdg);
	wdg.destroy();
	wdg.unlink();
    }
    
    public void destroy(int id) {
	synchronized(this) {
	    if(widgets.containsKey(id)) {
		Widget wdg = widgets.get(id);
		destroy(wdg);
	    }
	}
    }
	
    public void wdgmsg(Widget sender, String msg, Object... args) {
	int id;
	synchronized(this) {
	    if(!rwidgets.containsKey(sender))
		throw(new UIException("Wdgmsg sender (" + sender.getClass().getName() + ") is not in rwidgets", msg, args));
	    id = rwidgets.get(sender);
	}
	if(rcvr != null)
	    rcvr.rcvmsg(id, msg, args);
    }
	
    public void uimsg(int id, String msg, Object... args) {
	Widget wdg;
	synchronized(this) {
	    wdg = widgets.get(id);
	}
	if(wdg != null)
	    wdg.uimsg(msg.intern(), args);
	else
	    throw(new UIException("Uimsg to non-existent widget " + id, msg, args));
    }
	
    public void type(KeyEvent ev) {
	if(keygrab == null) {
	    if(!root.type(ev.getKeyChar(), ev))
		root.globtype(ev.getKeyChar(), ev);
	} else {
	    keygrab.type(ev.getKeyChar(), ev);
	}
    }
	
    public void keydown(KeyEvent ev) {
	if(ev.getKeyCode() == KeyEvent.VK_SHIFT)
	    modshift = true;
	else if(ev.getKeyCode() == KeyEvent.VK_CONTROL)
	    modctrl = true;
	if(ev.getKeyCode() == KeyEvent.VK_ALT)
	    modmeta = true;
	if(ev.getKeyCode() == KeyEvent.VK_WINDOWS)
	    modsuper = true;
	if(keygrab == null) {
	    if(!root.keydown(ev))
		root.globtype((char)0, ev);
	} else {
	    keygrab.keydown(ev);
	}
    }
	
    public void keyup(KeyEvent ev) {
	if(ev.getKeyCode() == KeyEvent.VK_SHIFT)
	    modshift = false;
	else if(ev.getKeyCode() == KeyEvent.VK_CONTROL)
	    modctrl = false;
	if(ev.getKeyCode() == KeyEvent.VK_ALT)
	    modmeta = false;
	if(ev.getKeyCode() == KeyEvent.VK_WINDOWS)
	    modsuper = false;
	if(keygrab == null)
	    root.keyup(ev);
	else
	    keygrab.keyup(ev);		
    }
	
    private Coord wdgxlate(Coord c, Widget wdg) {
	return(c.add(wdg.c.inv()).add(wdg.parent.rootpos().inv()));
    }
	
    public boolean dropthing(Widget w, Coord c, Object thing) {
	if(w instanceof DropTarget) {
	    if(((DropTarget)w).dropthing(c, thing))
		return(true);
	}
	for(Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
	    Coord cc = w.xlate(wdg.c, true);
	    if(c.isect(cc, wdg.sz)) {
		if(dropthing(wdg, c.add(cc.inv()), thing))
		    return(true);
	    }
	}
	return(false);
    }

    public void mousedown(Coord c, int button) {
	lcc = mc = c;
	if(mousegrab == null)
	    root.mousedown(c, button);
	else
	    mousegrab.mousedown(wdgxlate(c, mousegrab), button);
    }
	
    public void mouseup(Coord c, int button) {
	mc = c;
	if(mousegrab == null)
	    root.mouseup(c, button);
	else
	    mousegrab.mouseup(wdgxlate(c, mousegrab), button);
    }
	
    public void mousemove(Coord c) {
	mc = c;
	if(mousegrab == null)
	    root.mousemove(c);
	else
	    mousegrab.mousemove(wdgxlate(c, mousegrab));
    }
	
    public void mousewheel(Coord c, int amount) {
	lcc = mc = c;
	if(mousegrab == null)
	    root.mousewheel(c, amount);
	else
	    mousegrab.mousewheel(wdgxlate(c, mousegrab), amount);
    }
    
    private Object tooltipat(Widget w, Coord c) {
        for(Widget ch = w.lchild; ch != null; ch = ch.prev) {
	    if(!ch.visible)
		continue;
	    Coord cc = w.xlate(ch.c, true);
	    Object tt = tooltipat(ch, c.add(cc.inv()));
            if(tt != null)
                return(tt);
        }
        if(c.isect(Coord.z, w.sz))
            return(w.tooltip);
        return(null);
    }
    
    public Object tooltipat(Coord c) {
        return(tooltipat(root, c));
    }
    
    public int modflags() {
	return((modshift?1:0) |
	       (modctrl?2:0) |
	       (modmeta?4:0) |
	       (modsuper?8:0));
    }
}
