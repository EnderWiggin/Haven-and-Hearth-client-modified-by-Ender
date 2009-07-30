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
import java.util.*;

public class Listbox extends Widget {
    public List<Option> opts;
    public int chosen;
	
    static {
	Widget.addtype("lb", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    List<Option> opts = new LinkedList<Option>();
		    for(int i = 1; i < args.length; i += 2)
			opts.add(new Option((String)args[i], (String)args[i + 1]));
		    return(new Listbox(c, (Coord)args[0], parent, opts));
		}
	    });
    }

    public static class Option {
	public String name, disp;
	int y1, y2;
		
	public Option(String name, String disp) {
	    this.name = name;
	    this.disp = disp;
	}
    }
	
    public void draw(GOut g) {
	int y = 0, i = 0;
	for(Option o : opts) {
	    Color c;
	    if(i++ == chosen)
		c = FlowerMenu.pink;
	    else
		c = Color.BLACK;
	    Text t = Text.render(o.disp, c);
	    o.y1 = y;
	    g.image(t.tex(), new Coord(0, y));
	    y += t.sz().y;
	    o.y2 = y;
	}
    }
	
    public Listbox(Coord c, Coord sz, Widget parent, List<Option> opts) {
	super(c, sz, parent);
	this.opts = opts;
	chosen = 0;
	setcanfocus(true);
    }
	
    static List<Option> makelist(Option[] opts) {
	List<Option> ol = new LinkedList<Option>();
	for(Option o : opts)
	    ol.add(o);
	return(ol);
    }
	
    public Listbox(Coord c, Coord sz, Widget parent, Option[] opts) {
	this(c, sz, parent, makelist(opts));
    }
	
    public void sendchosen() {
	wdgmsg("chose", opts.get(chosen).name);
    }
	
    public boolean mousedown(Coord c, int button) {
	parent.setfocus(this);
	int i = 0;
	for(Option o : opts) {
	    if((c.y >= o.y1) && (c.y <= o.y2))
		break;
	    i++;
	}
	if(i < opts.size()) {
	    chosen = i;
	    sendchosen();
	}
	return(true);
    }
	
    public boolean keydown(KeyEvent e) { 
	if((e.getKeyCode() == KeyEvent.VK_DOWN) && (chosen < opts.size() - 1)) {
	    chosen++;
	    sendchosen();
	} else if((e.getKeyCode() == KeyEvent.VK_UP) && (chosen > 0)) {
	    chosen--;
	    sendchosen();
	}
	return(true);
    }
}
