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

public class ComMeter extends Widget {
    static Tex sword = Resource.loadtex("gfx/hud/combat/com/offdeff");
    static Text.Foundry intf = new Text.Foundry("Serif", 16);
    static Coord
	moc = new Coord(54, 61),
	mdc = new Coord(54, 71),
	ooc = new Coord(80, 61),
	odc = new Coord(80, 71);
    static Coord intc = new Coord(66, 33);
    static Color offcol = new Color(255, 0, 0), defcol = new Color(0, 0, 255);
    static Tex scales[];
    Fightview fv;
    
    static {
        scales = new Tex[11];
        for(int i = 0; i <= 10; i++)
            scales[i] = Resource.loadtex(String.format("gfx/hud/combat/com/%02d", i));
    }
    
    public ComMeter(Coord c, Widget parent, Fightview fv) {
        super(c, sword.sz(), parent);
	this.fv = fv;
    }
    
    public void draw(GOut g) {
	Fightview.Relation rel = fv.current;
	if(rel != null)
	    g.image(scales[(-rel.bal) + 5], Coord.z);
        g.image(sword, Coord.z);
	if(fv.off >= 200) {
	    g.chcolor(offcol);
	    g.frect(moc, new Coord(-fv.off / 200, 5));
	}
	if(fv.def >= 200) {
	    g.chcolor(defcol);
	    g.frect(mdc, new Coord(-fv.def / 200, 5));
	}
	g.chcolor();
	if(rel != null) {
	    g.aimage(intf.render(String.format("%d", rel.intns)).tex(), intc, 0.5, 0.5);
	    if(rel.off >= 200) {
		g.chcolor(offcol);
		g.frect(ooc, new Coord(rel.off / 200, 5));
	    }
	    if(rel.def >= 200) {
		g.chcolor(defcol);
		g.frect(odc, new Coord(rel.def / 200, 5));
	    }
	    g.chcolor();
	}
    }
}
