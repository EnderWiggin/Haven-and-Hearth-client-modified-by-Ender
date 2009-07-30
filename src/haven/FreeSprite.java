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

import java.util.*;

public abstract class FreeSprite extends Sprite {
    /*
    public Coord cc = Coord.z;
    public Coord sz = Coord.z;
    */
    private final Collection<Part> layers = new LinkedList<Part>();
    
    public interface Layer {
	public void draw(GOut g, Coord sc);
    }
    
    private class LPart extends Part {
	Layer lay;
	
	public LPart(Layer lay, int z, int subz) {
	    super(z, subz);
	    this.lay = lay;
	}
	
	public void draw(GOut g) {
	    lay.draw(g, sc());
	}
	
	public void draw(java.awt.image.BufferedImage img, java.awt.Graphics g) {
	}
    }

    protected FreeSprite(Owner owner, Resource res, int z, int subz) {
	super(owner, res);
	add(new Layer() {
		public void draw(GOut g, Coord sc) {
		    FreeSprite.this.draw(g, sc);
		}
	    }, z, subz);
    }
    
    protected FreeSprite(Owner owner, Resource res) {
	this(owner, res, 0, 0);
    }
    
    public void add(Layer lay, int z, int subz) {
	layers.add(new LPart(lay, z, subz));
    }
    
    public boolean checkhit(Coord c) {
	return(false);
    }

    public void setup(Drawer d, Coord cc, Coord off) {
	setup(layers, d, cc, off);
    }

    public Object stateid() {
	return(this);
    }
    
    public abstract void draw(GOut g, Coord sc);
}
