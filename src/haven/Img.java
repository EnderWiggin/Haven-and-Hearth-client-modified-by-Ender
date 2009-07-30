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

public class Img extends Widget {
    private Tex img;
    public boolean hit = false;
	
    static {
	Widget.addtype("img", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    Tex tex;
		    if(args.length > 1) {
			Resource res = Resource.load((String)args[0], (Integer)args[1]);
			res.loadwait();
			tex = res.layer(Resource.imgc).tex();
		    } else {
			tex = Resource.loadtex((String)args[0]);
		    }
		    Img ret = new Img(c, tex, parent);
		    if(args.length > 2)
			ret.hit = (Integer)args[2] != 0;
		    return(ret);
		}
	    });
    }
	
    public void draw(GOut g) {
	synchronized(img) {
	    g.image(img, Coord.z);
	}
    }
	
    public Img(Coord c, Tex img, Widget parent) {
	super(c, img.sz(), parent);
	this.img = img;
    }
	
    public void uimsg(String name, Object... args) {
	if(name == "ch") {
	    img = Resource.loadtex((String)args[0]);
	}
    }
    
    public boolean mousedown(Coord c, int button) {
	if(hit) {
	    wdgmsg("click", c, button, ui.modflags());
	    return(true);
	}
	return(false);
    }
}
