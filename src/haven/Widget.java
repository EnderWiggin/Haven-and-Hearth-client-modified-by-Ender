package haven;

import java.awt.Graphics;
import java.util.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.awt.GraphicsConfiguration;

public class Widget implements Graphical {
	UI ui;
	Coord c, sz;
	Widget next, prev, child, lchild, parent;
	boolean focustab = false, focusctl = false, hasfocus = false;
	private boolean canfocus = false;
	boolean canactivate = false;
	Widget focused;
	static Map<String, WidgetFactory> types = new TreeMap<String, WidgetFactory>();
	static Class<?>[] barda = {Img.class, TextEntry.class, MapView.class, FlowerMenu.class,
		Window.class, Button.class, Inventory.class, Item.class, Listbox.class,
		Makewindow.class, Chatwindow.class, Textlog.class, Equipory.class, IButton.class,
		Landwindow.class, Skillwindow.class};
	
	static {
		try {
			for(Class<?> c : barda)
				Class.forName(c.getName(), true, c.getClassLoader());
		} catch(ClassNotFoundException e) {
			throw(new Error(e));
		}
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
		if(focusctl && (focused != null)) {
			focused.hasfocus = true;
			focused.gotfocus();
		}
	}
	
	public void destroy() {
	}
	
	public void lostfocus() {
		if(focusctl && (focused != null)) {
			focused.hasfocus = false;
			focused.lostfocus();
		}
	}
	
	public void setfocus(Widget w) {
		if(focusctl) {
			if(w != focused) {
				Widget last = focused;
				focused = w;
				if(last != null)
					last.hasfocus = false;
				w.hasfocus = true;
				if(last != null)
					last.lostfocus();
				w.gotfocus();
				if((ui != null) && ui.rwidgets.containsKey(w))
					wdgmsg("focus", ui.rwidgets.get(w));
			}
			if(parent != null)
				parent.setfocus(this);
		} else {
			parent.setfocus(w);
		}
	}
	
	public void setcanfocus(boolean canfocus) {
		this.canfocus = canfocus;
		if(parent != null) {
			if(canfocus) {
				parent.newfocusable(this);
			} else {
				parent.delfocusable(this);
			}
		}
	}
	
	public void newfocusable(Widget w) {
		if(focusctl) {
			if(focused == null)
				setfocus(w);
		} else {
			parent.newfocusable(w);
		}
	}
	
	public void delfocusable(Widget w) {
		if(focusctl) {
			if(focused == w)
				focused = null;
		} else {
			parent.delfocusable(w);
		}
	}
	
	public void setfocusctl(boolean focusctl) {
		if(this.focusctl = focusctl) {
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
			setcanfocus(true);
		}
	}
	
	public void setfocustab(boolean focustab) {
		if(focustab && !focusctl)
			setfocusctl(true);
		this.focustab = focustab;
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "tabfocus") {
			setfocustab(((Integer)args[0] == 1));
		} else if(msg == "act") {
			canactivate = (Integer)args[0] == 1;
		} else if(msg == "focus") {
			Widget w = ui.widgets.get((Integer)args[0]); 
			if(w != null) {
				if(w.canfocus)
					setfocus(w);
			}
		} else {
			System.err.println("Unhandled widget message: " + msg);
		}
	}
	
	public void wdgmsg(String msg, Object... args) {
		wdgmsg(this, msg, args);
	}
	
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(parent == null)
			ui.wdgmsg(sender, msg, args);
		else
			parent.wdgmsg(sender, msg, args);
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
	
	public boolean mousewheel(Coord c, int amount) {
		for(Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
			Coord cc = xlate(wdg.c, true);
			if(c.isect(cc, wdg.sz)) {
				if(wdg.mousewheel(c.add(cc.inv()), amount)) {
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
		if(focusctl) {
			if(focused != null) {
				if(focused.type(key, ev))
					return(true);
				if(focustab) {
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
		if(focusctl) {
			if(focused != null) {
				if(focused.keydown(ev))
					return(true);
				return(false);
			} else {
				return(false);
			}
		} else {
			for(Widget wdg = child; wdg != null; wdg = wdg.next) {
				if(wdg.keydown(ev))
					return(true);
			}
		}
		return(false);
	}
	
	public boolean keyup(KeyEvent ev) {
		if(focusctl) {
			if(focused != null) {
				if(focused.keyup(ev))
					return(true);
				return(false);
			} else {
				return(false);
			}
		} else {
			for(Widget wdg = child; wdg != null; wdg = wdg.next) {
				if(wdg.keyup(ev))	
					return(true);
			}
		}
		return(false);
	}
	
	public void raise() {
		synchronized(ui) {
			unlink();
			link();
		}
	}
	
	public GraphicsConfiguration getconf() {
		return(parent.getconf());
	}
}
