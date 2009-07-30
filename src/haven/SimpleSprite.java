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

import java.awt.Graphics;

public class SimpleSprite {
    public final Resource.Image img;
    public final Coord cc;
    
    public SimpleSprite(Resource.Image img, Coord cc) {
	this.img = img;
	this.cc = cc;
    }

    public SimpleSprite(Resource res, int id, Coord cc) {
	find: {
	    for(Resource.Image img : res.layers(Resource.imgc)) {
		if(img.id == id) {
		    this.img = img;
		    break find;
		}
	    }
	    throw(new RuntimeException("Could not find image with id " + id + " in resource " + res.name));
	}
	this.cc = cc;
    }

    public SimpleSprite(Resource res, int id) {
	this(res, id, res.layer(Resource.negc).cc);
    }
    
    public SimpleSprite(Resource res) {
	this(res, -1);
    }
    
    public final void draw(GOut g, Coord cc) {
	g.image(img.tex(), cc.add(ul()));
    }

    public final void draw(Graphics g, Coord cc) {
	Coord c = cc.add(ul());
	g.drawImage(img.img, c.x, c.y, null);
    }
    
    public final Coord ul() {
	return(cc.inv().add(img.o));
    }
    
    public final Coord lr() {
	return(ul().add(img.sz));
    }

    public boolean checkhit(Coord c) {
	c = c.add(ul().inv());
	if((c.x < 0) || (c.y < 0) || (c.x >= img.sz.x) || (c.y >= img.sz.y))
	    return(false);
	int cl = img.img.getRGB(c.x, c.y);
	return(Utils.rgbm.getAlpha(cl) >= 128);
    }
}
