package haven;

import java.awt.Graphics;
import java.util.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.awt.GraphicsConfiguration;

public class Widget {
	UI ui;
	Coord c, sz;
	Widget next, prev, child, lchild, parent;
	GraphicsConfiguration gc;
	boolean tabfocus = false, canfocus = false, hasfocus = false;
	boolean canactivate = false;
	Widget focused;
	static Map<String, WidgetFactory> types = new TreeMap<String, WidgetFactory>();
	
	static {
		System.out.println(Img.barda);
		System.out.println(TextEntry.barda);
		System.out.println(MapView.barda);
		System.out.println(FlowerMenu.barda);
		System.out.println(Window.barda);
		System.out.println(Button.barda);
		System.out.println(Inventory.barda);
		System.out.println(Item.barda);
		addtype("cnt", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Widget(c, (Coord)args[0], parent));
			}
		});
	}
	
	public static void addtype(String name, WidgetFactory fct) {
		types.put(name, fct);
	}
	
	public static Widget create(String name, Coord c, Widget parent, Object[] args) {
		return(types.get(name).create(c, parent, args));
	}
	
	public Widget(UI ui, Coord c, Coord sz) {
		this.ui = ui;
		this.c = c;
		this.sz = sz;
	}
	
	public Widget(Coord c, Coord sz, Widget parent) {
		synchronized(parent.ui) {
			this.ui = parent.ui;
			this.gc = parent.gc;
			this.c = c;
			this.sz = sz;
			this.parent = parent;
			link();
		}
	}
	
	public void link() {
		synchronized(ui) {
			if(parent.lchild != null)
				parent.lchild.next = this;
			if(parent.child == null)
				parent.child = this;
			this.prev = parent.lchild;
			parent.lchild = this;
		}
	}
	
	public void unlink() {
		synchronized(ui) {
			if(next != null)
				next.prev = prev;
			if(prev != null)
				prev.next = next;
			if(parent.child == this)
				parent.child = next;
			if(parent.lchild == this)
				parent.lchild = prev;
			next = null;
			prev = null;
		}
	}
	
	public Coord xlate(Coord c, boolean in) {
		return(c);
	}
	
	public Coord rootpos() {
		if(parent == null)
			return(new Coord(0, 0));
		return(xlate(parent.rootpos().add(c), true));
	}
	
	public boolean hasparent(Widget w2) {
		for(Widget w = this; w != null; w = w.parent) {
			if(w == w2)
				return(true);
		}
		return(false);
	}
	
	public void gotfocus() {
	}
	
	public void lostfocus() {
	}
	
	public void setfocus(Widget w) {
		if(tabfocus) {
			if(w != focused) {
				Widget last = focused;
				focused = w;
				if(last != null)
					last.hasfocus = false;
				w.hasfocus = true;
				if(last != null)
					last.lostfocus();
				w.gotfocus();
				wdgmsg("focus", ui.rwidgets.get(w));
			}
		} else {
			parent.setfocus(w);
		}
	}
	
	public void settabfocus(boolean tabfocus) {
		this.tabfocus = tabfocus;
		if(tabfocus) {
			/* XXX: Might need to check subwidgets recursively */
			focused = null;
			for(Widget w = child; w != null; w = w.next) {
				if(w.canfocus) {
					focused = w;
					focused.hasfocus = true;
					w.gotfocus();
					break;
				}
			}
		}
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "tabfocus") {
			settabfocus(((Integer)args[0] == 1));
		} else if(msg == "act") {
			canactivate = (Integer)args[0] == 1;
		} else if(msg == "focus") {
			Widget w = ui.widgets.get((Integer)args[0]); 
			if(w != null) {
				if(w.canfocus)
					setfocus(w);
			}
		}
	}
	
	public void wdgmsg(String msg, Object... args) {
		ui.wdgmsg(this, msg, args);
	}
	
	public void draw(Graphics g) {
		for(Widget wdg = child; wdg != null; wdg = wdg.next) {
			Coord cc = xlate(wdg.c, true);
			wdg.draw(g.create(cc.x, cc.y, wdg.sz.x, wdg.sz.y));
		}
	}
	
	public boolean mousedown(Coord c, int button) {
		for(Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
			Coord cc = xlate(wdg.c, true);
			if(c.isect(cc, wdg.sz)) {
				if(wdg.mousedown(c.add(cc.inv()), button)) {
					return(true);
				}
			}
		}
		return(false);
	}
	
	public boolean mouseup(Coord c, int button) {
		for(Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
			Coord cc = xlate(wdg.c, true);
			if(c.isect(cc, wdg.sz)) {
				if(wdg.mouseup(c.add(cc.inv()), button)) {
					return(true);
				}
			}
		}
		return(false);
	}
	
	public void mousemove(Coord c) {
		for(Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
			Coord cc = xlate(wdg.c, true);
			if(c.isect(cc, wdg.sz))
				wdg.mousemove(c.add(cc.inv()));
		}
	}
	
	public boolean type(char key, KeyEvent ev) {
		if(canactivate) {
			if(key == 10) {
				wdgmsg("activate");
				return(true);
			}
		}
		if(tabfocus) {
			if(focused != null) {
				if(focused.type(key, ev))
					return(true);
				if(key == '\t') {
					Widget f = focused;
					while(true) {
						if((ev.getModifiers() & InputEvent.SHIFT_MASK) == 0)
							f = (f.next == null)?child:f.next;
						else
							f = (f.prev == null)?lchild:f.prev;
						if(f.canfocus)
							break;
					}
					setfocus(f);
					return(true);
				} else {
					return(false);
				}
			} else {
				return(false);
			}
		} else {
			for(Widget wdg = child; wdg != null; wdg = wdg.next) {
				if(wdg.type(key, ev))
					return(true);
			}
			return(false);
		}
	}
	
	public boolean keydown(KeyEvent ev) {
		for(Widget wdg = child; wdg != null; wdg = wdg.next) {
			if(wdg.keydown(ev))
				return(true);
		}
		return(false);
	}
	
	public boolean keyup(KeyEvent ev) {
		for(Widget wdg = child; wdg != null; wdg = wdg.next) {
			if(wdg.keyup(ev))
				return(true);
		}
		return(false);
	}
	
	public void raise() {
		synchronized(ui) {
			unlink();
			link();
		}
	}
}
