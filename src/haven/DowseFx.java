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

public class DowseFx extends FreeSprite{
	double a;
	static final int ms = 4000;
	static final int r = 100;
	int a1;
	int a2;
	
	public static final Factory fact = new Factory() {
	    public Sprite create(Owner owner, Resource res, Message sdt) {
			if(owner instanceof Gob){
				return(new DowseFx(owner, res, sdt));
			}
			return(null);
	    }
	};
	
	public DowseFx(haven.Sprite.Owner owner, Resource res, Message msg){
		super(owner, res, -15, 0);
		a = 0.0D;
		a2 = (msg.uint8() * 360) / 200;
		a1 = (msg.uint8() * 360) / 200;
		if(a2 > 380) a2 -= 460;
		a2 = -45 - a2;
		a1 = -45 - a1;
		
		int d = Math.max(Math.abs(a1 - a2) % 360, 5);
		int a0 = a1 + (d / 2) + 45;
		
		if(Config.showDirection)
			new TrackingWnd((Gob)owner, 0, (720 - a0)%360, d, a1, a2, msg);
	}

	public void draw(GOut g, Coord c){
		if(a < 0.25D)
		{
			g.chcolor(255, 0, 0, 128);
			g.fellipse(c, new Coord((int)((a / 0.25D) * 100D), (int)(((a / 0.25D) * 100D) / 2D)));
		} else
		if(a < 0.75D)
		{
			g.chcolor(255, 0, 0, (int)(((0.75D - a) / 0.5D) * 128D));
			g.fellipse(c, new Coord((int)((a / 0.25D) * 100D), (int)(((a / 0.25D) * 100D) / 2D)));
			g.chcolor(255, 0, 0, 128);
			g.fellipse(c, new Coord(100, 50), a1, a2);
		} else
		{
			g.chcolor(255, 0, 0, (int)(((1.0D - a) / 0.25D) * 128D));
			g.fellipse(c, new Coord(100, 50), a1, a2);
		}
		g.chcolor();
	}

	public boolean tick(int paramInt){
		a += (double)paramInt / 2000D;
		return a >= 1.0D;
	}
}