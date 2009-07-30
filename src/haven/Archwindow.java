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

import static haven.MCache.tilesz;
import java.util.*;

public class Archwindow extends Window implements MapView.Grabber {
	Collection<Gob> vob = new LinkedList<Gob>();
	Coord sc;
	OCache oc;
	
	static {
		Widget.addtype("arch", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Archwindow(c, parent));
			}
		});
	}
	
	public Archwindow(Coord c, Widget parent) {
		super(c, new Coord(150, 100), parent, "Architecture");
		oc = ui.sess.glob.oc;
		ui.mainview.grab(this);
		oc.ladd(vob);
	}
	
	public void destroy() {
		ui.mainview.release(this);
		oc.lrem(vob);
		super.destroy();
	}
	
	void makevob(Coord mc) {
		vob.clear();
		Coord wc = sc.mul(tilesz);
		if(Math.abs(wc.x - mc.x) > Math.abs(wc.y - mc.y)) {
			 Coord ec = mc.div(tilesz);
			 ec.y = sc.y;
			 int s;
			 if(ec.x < sc.x)
				 s = -1;
			 else
				 s = 1;
			 wc = sc.add(Coord.z);
			 while(true) {
				 Gob g = new Gob(ui.sess.glob, wc.mul(tilesz));
				 g.setattr(new ResDrawable(g, Resource.load("gfx/arch/walls/wood-we")));
				 vob.add(g);
				 if(wc.x == ec.x)
					 break;
				 wc.x += s;
			 }
		} else {
			 Coord ec = mc.div(tilesz);
			 ec.x = sc.x;
			 int s;
			 if(ec.y < sc.y)
				 s = -1;
			 else
				 s = 1;
			 wc = sc.add(Coord.z);
			 while(true) {
				 Gob g = new Gob(ui.sess.glob, wc.mul(tilesz), 0, 0);
				 g.setattr(new ResDrawable(g, Resource.load("gfx/arch/walls/wood-ns")));
				 vob.add(g);
				 if(wc.y == ec.y)
					 break;
				 wc.y += s;
			 }
		}
	}
	
	public void mmousedown(Coord c, int button) {
		if((sc != null) || (button != 1))
			return;
		sc = c.div(tilesz);
		makevob(c);
	}
	
	public void mmouseup(Coord c, int button) {
		sc = null;
	}
	
	public void mmousemove(Coord c) {
		if(sc != null)
			makevob(c);
	}
}
