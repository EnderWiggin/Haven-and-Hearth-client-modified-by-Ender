/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.util.*;

public class Tabs {
    Coord c, sz;
    Widget parent;
    ArrayList<Widget> tabs;
    ArrayList<Button> btns;
    HashMap<String, Widget> tabmap;
    Widget curtab;

    public Tabs(Coord c, Coord sz, Widget parent) {
	this.c = c;
	this.sz = sz;
	this.parent = parent;
	tabs   = new ArrayList<Widget>();
	btns   = new ArrayList<Button>();
	tabmap = new HashMap<String, Widget>();
    }

    public class TabButton extends Button {
	public Widget tab;

	public TabButton(Coord c, Integer w, String text) {
	    super(c, w, Tabs.this.parent, text);
	}

	public void click() {
	    changed(tabs.indexOf(curtab), tabs.indexOf(tab));
	    showtab(tab);
	}
    }

    private void addentry(Widget tab, Button btn) {
	tabs.add(tab);
	btns.add(btn);
	tab.hide();
	if(curtab == null)
	    showtab(tab);
	setupbutton(tab, btn);
    }

    private void setupbutton(Widget tab, Button btn) {
	if(btn != null && btn instanceof TabButton)
	    ((TabButton)btn).tab = tab;
    }

    private Widget createtab(Button btn) {
	Widget tab = new Widget(c, sz, this.parent);
	addentry(tab, btn);
	return(tab);
    }

    public Widget newtab(Button btn) {
	return(createtab(btn));
    }
    public Widget newtab(String name, Button btn) {
	tabmap.put(name, newtab(btn));
	return(tabmap.get(name));
    }
    public Widget newtab() {
	return(createtab(null));
    }
    public Widget newtab(String name) {
	tabmap.put(name, newtab());
	return(tabmap.get(name));
    }

    public void addtab(String name, Widget tab, Button btn) {
	addentry(tab, btn);
	tabmap.put(name, tab);
    }
    public void addtab(String name, Widget tab) {
	addentry(tab, null);
	tabmap.put(name, tab);
    }
    public void addtab(Widget tab, Button btn) {
	addentry(tab, btn);
    }
    public void addtab(Widget tab) {
	addentry(tab, null);
    }

    public void showtab(Widget tab) {
	if(curtab != null)
	    curtab.hide();
	curtab = tab;
	if(curtab != null)
	    curtab.show();
    }
    public void showtab(int tab) {
	showtab(tabs.get(tab));
    }
    public void showtab(String name) {
	showtab(tabmap.get(name));
    }

    public void setbutton(Widget tab, Button btn) {
	if(tabs.contains(tab)) {
	    btns.set(tabs.indexOf(tab), btn);
	    setupbutton(tab, btn);
	}
    }
    public void setbutton(int tab, Button btn) {
	if(tab > 0 && tab < tabs.size())
	    setbutton(tabs.get(tab), btn);
    }
    public void setbutton(String name, Button btn) {
	if(tabmap.containsKey(name))
	    setbutton(tabmap.get(name), btn);
    }

    public Widget get(String name) {
	return(tabmap.get(name));
    }

    public Button getbutton(int tab) {
	return(btns.get(tab));
    }

    public void changed(int from, int to) {}
}
