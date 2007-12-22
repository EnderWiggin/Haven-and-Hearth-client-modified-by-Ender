package haven;

import java.util.*;
import java.awt.event.KeyEvent;

public class UI {
	Widget root;
	private Widget keygrab, mousegrab;
	Map<Integer, Widget> widgets = new TreeMap<Integer, Widget>();
	Map<Widget, Integer> rwidgets = new HashMap<Widget, Integer>();
	Receiver rcvr;
	
	public interface Receiver {
		public void rcvmsg(int widget, String msg, Object... args);
	}
	
	public UI(RootWidget root) {
		root.setui(this);
		this.root = root;
		widgets.put(0, root);
		rwidgets.put(root, 0);
	}
	
	public void setreceiver(Receiver rcvr) {
		this.rcvr = rcvr;
	}
	
	public void newwidget(int id, String type, Coord c, int parent, Object... args) {
		try {
			synchronized(this) {
				Widget wdg = Widget.create(type, c, widgets.get(parent), args);
				widgets.put(id, wdg);
				rwidgets.put(wdg, id);
			}
		} catch(Throwable t) {
			/* XXX: Remove! */
			t.printStackTrace();
		}
	}
	
	public void grabmouse(Widget wdg) {
		mousegrab = wdg;
	}
	
	private void removeid(Widget wdg) {
		int id = rwidgets.get(wdg);
		widgets.remove(id);
		rwidgets.remove(wdg);
		for(Widget child = wdg.child; child != null; child = child.next)
			removeid(child);
	}
	
	public void destroy(int id) {
		if(widgets.containsKey(id)) {
			Widget wdg = widgets.get(id);
			
			if((mousegrab != null) && mousegrab.hasparent(wdg))
				mousegrab = null;
			if((keygrab != null) && keygrab.hasparent(wdg))
				keygrab = null;
			removeid(wdg);
			wdg.unlink();
		}
	}
	
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(rcvr != null)
			rcvr.rcvmsg(rwidgets.get(sender), msg, args);
	}
	
	public void uimsg(int id, String msg, Object... args) {
		if(widgets.containsKey(id))
			widgets.get(id).uimsg(msg.intern(), args);
	}
	
	public void type(KeyEvent ev) {
		if(keygrab == null)
			root.type(ev.getKeyChar(), ev);
		else
			keygrab.type(ev.getKeyChar(), ev);
	}
	
	public void keydown(KeyEvent ev) {
		if(keygrab == null)
			root.keydown(ev);
		else
			keygrab.keydown(ev);
	}
	
	public void keyup(KeyEvent ev) {
		if(keygrab == null)
			root.keyup(ev);
		else
			keygrab.keyup(ev);		
	}
	
	private Coord wdgxlate(Coord c, Widget wdg) {
		return(c.add(wdg.rootpos().inv()));
	}
	
	public void mousedown(Coord c, int button) {
		if(mousegrab == null)
			root.mousedown(c, button);
		else
			mousegrab.mousedown(wdgxlate(c, mousegrab), button);
	}
	
	public void mouseup(Coord c, int button) {
		if(mousegrab == null)
			root.mouseup(c, button);
		else
			mousegrab.mouseup(wdgxlate(c, mousegrab), button);
	}
	
	public void mousemove(Coord c) {
		if(mousegrab == null)
			root.mousemove(c);
		else
			mousegrab.mousemove(wdgxlate(c, mousegrab));
	}
}
