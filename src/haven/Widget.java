package haven;

import java.awt.Graphics;
import java.util.*;
import java.awt.event.KeyEvent;

import java.awt.GraphicsConfiguration;

public class Widget {
	UI ui;
	Coord c, sz;
	Widget next, prev, child, lchild, parent;
	GraphicsConfiguration gc;
	static Map<String, WidgetFactory> types = new TreeMap<String, WidgetFactory>();
	
	static {
		System.out.println(Img.barda);
		System.out.println(TextEntry.barda);
		System.out.println(MapView.barda);
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
			if(parent.lchild == null)
				parent.lchild = this;
			if(parent.child != null)
				parent.child.prev = this;
			this.next = parent.child;
			parent.child = this;
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
		}
	}
	
	/*
	public void remove() {
		synchronized(ui) {
			if(next != null)
				next.prev = this.prev;
			if(prev != null)
				prev.next = next;
			if(parent.child == this)
				parent.child = next;
			if(parent.lchild == this)
				parent.lchild = prev;
		}
	}
	*/
	
	public void uimsg(String msg, Object... args) {
		
	}
	
	public void draw(Graphics g) {
		for(Widget wdg = child; wdg != null; wdg = wdg.next) {
			wdg.draw(g.create(wdg.c.x, wdg.c.y, wdg.sz.x, wdg.sz.y));
		}
	}
	
	public boolean mousedown(Coord c, int button) {
		for(Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
			if(c.isect(wdg.c, wdg.sz)) {
				if(wdg.mousedown(c.add(wdg.c.inv()), button)) {
					return(true);
				}
			}
		}
		return(false);
	}
	
	public boolean mouseup(Coord c, int button) {
		for(Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
			if(c.isect(wdg.c, wdg.sz)) {
				if(wdg.mouseup(c.add(wdg.c.inv()), button)) {
					return(true);
				}
			}
		}
		return(false);
	}
	
	public boolean type(char key) {
		for(Widget wdg = child; wdg != null; wdg = wdg.next) {
			if(wdg.type(key))
				return(true);
		}
		return(false);
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
}
