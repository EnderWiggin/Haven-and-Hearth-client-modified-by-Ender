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

import static java.lang.Math.PI;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Cal extends SSWidget {
    public static final double hbr = 23;
    static BufferedImage bg = Resource.loadimg("gfx/hud/calendar/setting");
    private static final int dx = Utils.imgsz(bg).x / 2;
    static BufferedImage dlnd = Resource.loadimg("gfx/hud/calendar/dayscape");
    static BufferedImage dsky = Resource.loadimg("gfx/hud/calendar/daysky");
    static BufferedImage nlnd = Resource.loadimg("gfx/hud/calendar/nightscape");
    static BufferedImage nsky = Resource.loadimg("gfx/hud/calendar/nightsky");
    static BufferedImage sun = Resource.loadimg("gfx/hud/calendar/sun");
    static BufferedImage moon[];
    long update = 0;
    Text ttip = null;
    Astronomy current;
	
    static {
	moon = new BufferedImage[8];
	for(int i = 0; i < moon.length; i++)
	    moon[i] = Resource.loadimg(String.format("gfx/hud/calendar/m%02d", i));
	Widget.addtype("cal", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Cal(c, parent));
		}
	    });
    }
	
    private void render() {
	Astronomy a = current = ui.sess.glob.ast;
	clear();
	Graphics g = graphics();
	g.drawImage(bg, 0, 0, null);
	g.drawImage(a.night?nsky:dsky, 0, 0, null);
	int mp = (int)(a.mp * (double)moon.length);
	String tt = String.format("Day %d,   %02d:%02d\nMoon: %s", a.day, a.hh, a.mm, Astronomy.phase[mp]);
	ttip = RichText.render(tt, 200);
	BufferedImage moon = Cal.moon[mp];
	Coord mc = Coord.sc((a.dt + 0.25) * 2 * PI, hbr).add(sz.sub(Utils.imgsz(moon)).div(2));
	Coord sc = Coord.sc((a.dt + 0.75) * 2 * PI, hbr).add(sz.sub(Utils.imgsz(sun)).div(2));
	g.drawImage(moon, mc.x, mc.y, null);
	g.drawImage(sun, sc.x, sc.y, null);
	g.drawImage(a.night?nlnd:dlnd, 0, 0, null);
	update();
	update = System.currentTimeMillis();
	ui.sess.resUpdates();
    }
	
    public Cal(Coord c, Widget parent) {
	super(c, Utils.imgsz(bg), parent);
	render();
    }
	
    public void draw(GOut g) {
        c.x = MainFrame.centerPoint.x - dx;
	if(!current.equals(ui.sess.glob.ast))
	    render();
	super.draw(g);
    }
    
    public Object tooltip(Coord c, boolean again) {
	return ttip;
    }
}
