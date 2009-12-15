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

package haven.resutil;

import haven.*;

public abstract class BollSprite extends Sprite {
    public Boll bollar = null;
    
    public abstract static class Boll extends Part {
	private Boll n, p;
	public double x, y, z;
	
	public Boll(int pz, int subz, double x, double y, double z) {
	    super(pz, subz);
	    this.x = x;
	    this.y = y;
	    this.z = z;
	}
	
	public Boll(double x, double y, double z) {
	    this(0, 0, x, y, z);
	}
	
	public Boll() {
	    this(0, 0, 0, 0, 0);
	}
	
	public abstract boolean tick(int dt);
	public abstract void draw(GOut g, Coord sc);
	
	public void setup(Coord cc, Coord off) {
	    super.setup(cc.add((int)((x * 2) - (y * 2)), (int)(x + y)), off.add(0, (int)z));
	}

	public void draw(GOut g) {
	    draw(g, sc());
	}
	
	public void draw(java.awt.image.BufferedImage img, java.awt.Graphics g) {
	}
    }
    
    protected BollSprite(Owner owner, Resource res) {
	super(owner, res);
    }
    
    public void add(Boll boll) {
	if(bollar != null)
	    bollar.p = boll;
	boll.n = bollar;
	bollar = boll;
    }
    
    public void remove(Boll boll) {
	if(boll.n != null)
	    boll.n.p = boll.p;
	if(boll.p != null)
	    boll.p.n = boll.n;
	if(boll == bollar)
	    bollar = boll.n;
    }
    
    public abstract boolean tick2(int dt);
    
    public boolean tick(int dt) {
	Boll n;
	for(Boll boll = bollar; boll != null; boll = n) {
	    n = boll.n;
	    if(boll.tick(dt))
		remove(boll);
	}
	return(tick2(dt));
    }
    
    public boolean checkhit(Coord c) {
	return(false);
    }
    
    public void setup(Drawer d, Coord cc, Coord off) {
	for(Boll boll = bollar; boll != null; boll = boll.n) {
	    boll.setup(cc, off);
	    d.addpart(boll);
	}
    }

    public Object stateid() {
	return(this);
    }
}
