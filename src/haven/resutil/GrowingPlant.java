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

import java.util.*;

public class GrowingPlant extends CSprite {
    public static class Factory implements Sprite.Factory {
	public Tex[][] strands;
	public int num;
	public Resource.Neg neg;
	
	public Factory(int stages, int variants, int num, boolean rev) {
	    Resource res = Utils.myres(this.getClass());
	    this.neg = res.layer(Resource.negc);
	    this.num = num;
	    strands = new Tex[stages][variants];
	    if(rev) {
		for(Resource.Image img : res.layers(Resource.imgc)) {
		    if(img.id != -1)
			strands[img.id / variants][img.id % variants] = img.tex();
		}
	    } else {
		for(Resource.Image img : res.layers(Resource.imgc)) {
		    if(img.id != -1)
			strands[img.id % stages][img.id / stages] = img.tex();
		}
	    }
	}
	
	public Factory(int stages, int variants, int num) {
	    this(stages, variants, num, false);
	}
	
	public Sprite create(Owner owner, Resource res, Message sdt) {
	    int m = sdt.uint8();
	    GrowingPlant spr = new GrowingPlant(owner, res);
	    spr.addnegative();
	    Random rnd = owner.mkrandoom();
	    int n = Config.simple_plants?1:num;
	    for(int i = 0; i < n; i++) {
		Coord c;
		if(Config.simple_plants){
		    c = neg.bc.add(neg.bs).sub(5, 5);
		} else {
		    c = new Coord(rnd.nextInt(neg.bs.x), rnd.nextInt(neg.bs.y)).add(neg.bc);
		}
		Tex s = strands[m][rnd.nextInt(strands[m].length)];
		spr.add(s, 0, MapView.m2s(c), new Coord(s.sz().x / 2, s.sz().y).inv());
	    }
	    return(spr);
	}
    }
    
    protected GrowingPlant(Owner owner, Resource res) {
	super(owner, res);
    }
}
