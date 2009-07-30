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

public class LinMove extends Moving {
	Coord s, t;
	int c;
	double a;
    
	public LinMove(Gob gob, Coord s, Coord t, int c) {
		super(gob);
		this.s = s;
		this.t = t;
		this.c = c;
		this.a = 0;
	}
    
	public Coord getc() {
		double dx, dy;
		dx = t.x - s.x;
		dy = t.y - s.y;
		Coord m = new Coord((int)(dx * a), (int)(dy * a));
		return(s.add(m));
	}
    
	/*
	public void tick() {
		if(l < c)
			l++;
	}
	*/
	
	public void ctick(int dt) {
		double da = ((double)dt / 1000) / (((double)c) * 0.06);
		a += da * 0.9;
		if(a > 1)
			a = 1;
	}
	
	public void setl(int l) {
		double a = ((double)l) / ((double)c);
		if(a > this.a)
			this.a = a;
	}
}
