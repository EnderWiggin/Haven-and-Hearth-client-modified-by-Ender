package haven;

import java.util.*;
import java.awt.event.KeyEvent;

public class UI {
	RootWidget root;
	private Widget keygrab, mousegrab;
	Map<Integer, Widget> widgets = new TreeMap<Integer, Widget>();
	Map<Widget, Integer> rwidgets = new HashMap<Widget, Integer>();
	Receiver rcvr;
	Coord mc, lcc = Coord.z;
	Session sess;
	MapView mainview;
	public Widget mouseon;
	public Object tooltip = null;
	
	public interface Receiver {
		public void rcvmsg(int widget, String msg, Object... args);
	}
	
	@SuppressWarnings("serial")
	public static class UIException extends RuntimeException {
		String mname;
		Object[] args;
		
		public UIException(String message, String mname, Object... args) {
			super(message);
			this.mname = mname;
			this.args = args;
		}
	}
	
	public UI(Coord sz, Graphical backer, Session sess) {
		root = new RootWidget(this, sz, backer);
		widgets.put(0, root);
		rwidgets.put(root, 0);
		this.sess = sess;
	}
	
	public void setreceiver(Receiver rcvr) {
		this.rcvr = rcvr;
	}
	
	public void newwidget(int id, String type, Coord c, int parent, Object... args) {
		synchronized(this) {
			Widget pwdg = widgets.get(parent);
			if(pwdg == null)
				throw(new UIException("Null parent widget " + parent + " for " + id, type, args));
			Widget wdg = Widget.create(type, c, pwdg, args);
			widgets.put(id, wdg);
			rwidgets.put(wdg, id);
			if(wdg instanceof MapView)
				mainview = (MapView)wdg;
		}
	}
	
	public void grabmouse(Widget wdg) {
		mousegrab = wdg;
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
		if(keygrab == null) {
			if(!root.keydown(ev))
				root.globtype((char)0, ev);
		} else {
			keygrab.keydown(ev);
		}
	}
	
	public void keyup(KeyEvent ev) {
		if(keygrab == null)
			root.keyup(ev);
		else
			keygrab.keyup(ev);		
	}
	
	private Coord wdgxlate(Coord c, Widget wdg) {
		return(c.add(wdg.c.inv()).add(wdg.parent.rootpos().inv()));
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
}
