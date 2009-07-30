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

import java.awt.Color;
import java.awt.event.KeyEvent;
import haven.Resource.AButton;
import java.util.*;

public class MenuGrid extends Widget {
    public final static Tex bg = Resource.loadtex("gfx/hud/invsq");
    public final static Coord bgsz = bg.sz().add(-1, -1);
    public final static Resource bk = Resource.load("gfx/hud/sc-back");
    private static Coord gsz = new Coord(4, 4);
    private Resource cur, pressed, dragging, layout[][] = new Resource[gsz.x][gsz.y];
    private Map<Character, Resource> hotmap = new TreeMap<Character, Resource>();
    private Resource hover = null;
	
    static {
	Widget.addtype("scm", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new MenuGrid(c, parent));
		}
	    });
    }
	
    public class PaginaException extends RuntimeException {
	public Resource res;
	
	public PaginaException(Resource r) {
	    super("Invalid pagina: " + r.name);
	    res = r;
	}
    }

    private Resource[] cons(Resource p) {
	Resource[] cp = new Resource[0];
	Resource[] all;
	{
	    Collection<Resource> ta = new HashSet<Resource>();
	    Collection<Resource> open;
	    synchronized(ui.sess.glob.paginae) {
		open = new HashSet<Resource>(ui.sess.glob.paginae);
	    }
	    while(!open.isEmpty()) {
		for(Resource r : open.toArray(cp)) {
		    if(!r.loading) {
			AButton ad = r.layer(Resource.action);
			if(ad == null)
			    throw(new PaginaException(r));
			if((ad.parent != null) && !ta.contains(ad.parent))
			    open.add(ad.parent);
			ta.add(r);
			open.remove(r);
		    }
		}
	    }
	    all = ta.toArray(cp);
	}
	Collection<Resource> tobe = new HashSet<Resource>();
	for(Resource r : all) {
	    if(r.layer(Resource.action).parent == p)
		tobe.add(r);
	}
	return(tobe.toArray(cp));
    }
	
    public MenuGrid(Coord c, Widget parent) {
	super(c, bgsz.mul(gsz).add(1, 1), parent);
	cons(null);
    }
	
    private static Comparator<Resource> sorter = new Comparator<Resource>() {
	public int compare(Resource a, Resource b) {
	    AButton aa = a.layer(Resource.action), ab = b.layer(Resource.action);
	    if((aa.ad.length == 0) && (ab.ad.length > 0))
		return(-1);
	    if((aa.ad.length > 0) && (ab.ad.length == 0))
		return(1);
	    return(aa.name.compareTo(ab.name));
	}
    };

    private void updlayout() {
	Resource[] cur = cons(this.cur);
	Arrays.sort(cur, sorter);
	int i = 0;
	hotmap.clear();
	for(int y = 0; y < gsz.y; y++) {
	    for(int x = 0; x < gsz.x; x++) {
		Resource btn = null;
		if((this.cur != null) && (x == gsz.x - 1) && (y == gsz.y - 1)) {
		    btn = bk;
		} else if(i < cur.length) {
		    Resource.AButton ad = cur[i].layer(Resource.action);
		    hotmap.put(Character.toUpperCase(ad.hk), cur[i]);
		    btn = cur[i++];
		}
		layout[x][y] = btn;
	    }
	}
    }
	
    public void draw(GOut g) {
	updlayout();
	for(int y = 0; y < gsz.y; y++) {
	    for(int x = 0; x < gsz.x; x++) {
		Coord p = bgsz.mul(new Coord(x, y));
		g.image(bg, p);
		Resource btn = layout[x][y];
		if(btn != null) {
		    Tex btex = btn.layer(Resource.imgc).tex();
		    g.image(btex, p.add(1, 1));
		    if(btn == pressed) {
			g.chcolor(new Color(0, 0, 0, 128));
			g.frect(p.add(1, 1), btex.sz());
			g.chcolor();
		    }
		}
	    }
	}
	if(pressed == null && hover != null) {
	    Resource.AButton ad = hover.layer(Resource.action);
	    String tt = ad.name;
	    if(ad.hk != 0)
		tt += " [" + ad.hk + "]";
	    ui.tooltip = tt;
	}
	if(dragging != null) {
	    final Tex dt = dragging.layer(Resource.imgc).tex();
	    ui.drawafter(new UI.AfterDraw() {
		    public void draw(GOut g) {
			g.image(dt, ui.mc.add(dt.sz().div(2).inv()));
		    }
		});
	}
    }
	
    private Resource bhit(Coord c) {
	Coord bc = c.div(bgsz);
	if((bc.x >= 0) && (bc.y >= 0) && (bc.x < gsz.x) && (bc.y < gsz.y))
	    return(layout[bc.x][bc.y]);
	else
	    return(null);
    }
	
    private void updhover(Coord c) {
	hover = bhit(c);
	if((hover != null) && (hover.layer(Resource.action) == null))
	    hover = null;
    }
	
    public boolean mousedown(Coord c, int button) {
	Resource h = bhit(c);
	if((button == 1) && (h != null)) {
	    pressed = h;
	    ui.grabmouse(this);
	}
	updhover(c);
	return(true);
    }
	
    public void mousemove(Coord c) {
	updhover(c);
	if((dragging == null) && (pressed != null)) {
	    Resource h = bhit(c);
	    if(h != pressed)
		dragging = pressed;
	}
    }
	
    private void use(Resource r) {
	if(cons(r).length > 0) {
	    cur = r;
	} else if(r == bk) {
	    cur = cur.layer(Resource.action).parent;
	} else {
	    wdgmsg("act", (Object[])r.layer(Resource.action).ad);
	}
    }
	
    public boolean mouseup(Coord c, int button) {
	Resource h = bhit(c);
	if(button == 1) {
	    if(dragging != null) {
		ui.dropthing(ui.root, ui.mc, dragging);
		dragging = pressed = null;
	    } else if(pressed != null) {
		if(pressed == h)
		    use(h);
		pressed = null;
	    }
	    ui.grabmouse(null);
	}
	updlayout();
	updhover(c);
	return(true);
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "goto") {
	    String res = (String)args[0];
	    if(res.equals(""))
		cur = null;
	    else
		cur = Resource.load(res);
	}
    }
	
    public boolean globtype(char k, KeyEvent ev) {
	if((k == 27) && (this.cur != null)) {
	    this.cur = null;
	    updlayout();
	    return(true);
	}
	Resource r = hotmap.get(Character.toUpperCase(k));
	if(r != null) {
	    use(r);
	    return(true);
	}
	return(false);
    }
}
