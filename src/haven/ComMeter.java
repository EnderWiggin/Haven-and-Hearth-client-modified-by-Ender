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

public class ComMeter extends Widget {
    static Tex sword = Resource.loadtex("gfx/hud/combat/com/sword");
    static Tex scales[];
    int bal, intns;
    
    static {
        scales = new Tex[11];
        for(int i = 0; i <= 10; i++)
            scales[i] = Resource.loadtex(String.format("gfx/hud/combat/com/%02d", i));
        Widget.addtype("com", new WidgetFactory() {
            public Widget create(Coord c, Widget parent, Object[] args) {
                return(new ComMeter(c, parent));
            }
        });
    }
    
    public ComMeter(Coord c, Widget parent) {
        super(c, sword.sz(), parent);
    }
    
    public void draw(GOut g) {
        g.image(sword, Coord.z);
        g.image(scales[(-bal) + 5], Coord.z);
	g.atext(String.format("%d", intns), sword.sz().div(new Coord(2, 1)), 0.5, 1);
    }
    
    public void uimsg(String msg, Object... args) {
        if(msg == "upd") {
            bal = (Integer)args[0];
            intns = (Integer)args[1];
            return;
        }
        super.uimsg(msg, args);
    }
}
