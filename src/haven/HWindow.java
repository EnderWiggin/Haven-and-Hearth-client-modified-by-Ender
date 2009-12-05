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

import java.awt.image.BufferedImage;

public class HWindow extends Widget {
    public String title;
    public IButton cbtn;
    static BufferedImage[] cbtni = new BufferedImage[] {
	Resource.loadimg("gfx/hud/cbtn"),
	Resource.loadimg("gfx/hud/cbtnd"),
	Resource.loadimg("gfx/hud/cbtnh")}; 
    SlenHud shp;
    int urgent;
	
    static {
	Widget.addtype("hwnd", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    String t = (String)args[0];
		    boolean cl = false;
		    if(args.length > 1)
			cl = (Integer)args[1] != 0;
		    return(new HWindow(parent, t, cl));
		}
	    });
    }
	
    public HWindow(Widget parent, String title, boolean closable) {
	super(new Coord(234, 29), new Coord(430, 100), parent);
	this.title = title;
	shp = (SlenHud)parent;
	shp.addwnd(this);
	if(closable)
	    cbtn = new IButton(new Coord(sz.x - cbtni[0].getWidth(), 0), this, cbtni[0], cbtni[1], cbtni[2]);
    }
	
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == cbtn) {
	    wdgmsg("close");
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
	
    public void destroy() {
	super.destroy();
	shp.remwnd(this);
    }
    
    public void makeurgent(int level) {
	shp.updurgency(this, level);
    }
}
