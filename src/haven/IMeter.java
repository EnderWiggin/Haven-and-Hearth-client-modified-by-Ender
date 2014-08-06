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
import java.util.LinkedList;
import java.util.List;
import java.awt.Font;

public class IMeter extends Widget {
    static Coord off = new Coord(13, 7);
    static Coord fsz = new Coord(63, 18);
    static Coord msz = new Coord(49, 4);
    public Resource bg;
    public List<Meter> meters;
	static Text.Foundry fnd = new Text.Foundry(new Font("SansSerif", Font.PLAIN, 9));
    
    static {
	Widget.addtype("im", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    Resource bg = Resource.load((String)args[0]);
		    List<Meter> meters = new LinkedList<Meter>();
		    for(int i = 1; i < args.length; i += 2)
			meters.add(new Meter((Color)args[i], (Integer)args[i + 1]));
		    return(new IMeter(c, parent, bg, meters));
		}
	    });
    }
    
    public IMeter(Coord c, Widget parent, Resource bg, List<Meter> meters) {
	super(c, fsz, parent);
	this.bg = bg;
	this.meters = meters;
    }
    
    public static class Meter {
	public Color c;
	public int a;
	
	public Meter(Color c, int a) {
	    this.c = c;
	    this.a = a;
	}
    }
    
    public void draw(GOut g) {
	if(!bg.loading) {
	    Tex bg = this.bg.layer(Resource.imgc).tex();
	    g.chcolor(0, 0, 0, 255);
	    g.frect(off, msz);
	    g.chcolor();
	    for(Meter m : meters) {
			int w = msz.x;
			w = (w * m.a) / 100;
			g.chcolor(m.c);
			g.frect(off, new Coord(w, msz.y));
	    }
		if(Config.displayNumbers) {
			try{
			String []tip = ((String)tooltip).split(":");
			g.aimage(fnd.render(tip[1]).tex(), new Coord(10,0), 0, 0.2);
			}catch(Exception e){}
		}
		g.chcolor();
	    g.image(bg, Coord.z);
	}
    }
    
    public void uimsg(String msg, Object... args) {
	if(msg == "set") {
	    List<Meter> meters = new LinkedList<Meter>();
	    for(int i = 0; i < args.length; i += 2)
		meters.add(new Meter((Color)args[i], (Integer)args[i + 1]));
	    this.meters = meters;
	} else if(msg == "tt") {
	    tooltip = args[0];
	} else {
	    super.uimsg(msg, args);
	}
    }
}
