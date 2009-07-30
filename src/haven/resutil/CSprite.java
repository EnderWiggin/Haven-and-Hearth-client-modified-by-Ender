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
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class CSprite extends Sprite {
    Collection<Part> frame = new LinkedList<Part>();
    
    public abstract static class OffsetPart extends Part {
	public Coord moff, soff;
	
	public OffsetPart(int z, int subz, Coord moff, Coord soff) {
	    super(z, subz);
	    this.moff = moff;
	    this.soff = soff;
	}

	public OffsetPart(int z, int subz) {
	    this(z, subz, Coord.z, Coord.z);
	}

	public void setup(Coord cc, Coord off) {
	    super.setup(cc.add(moff), off.add(soff));
	}

	public void draw(BufferedImage buf, Graphics g) {}
    }
    
    public static class SSPart extends OffsetPart {
	public SimpleSprite spr;
	
	public SSPart(SimpleSprite spr, int z, int subz) {
	    super(z, subz);
	    this.spr = spr;
	}

	public SSPart(SimpleSprite spr) {
	    this(spr, spr.img.z, spr.img.subz);
	}
	
	public void setup(Coord cc, Coord off) {
	    super.setup(cc, off);
	    ul = sc().add(spr.ul());
	    lr = sc().add(spr.lr());
	}
	
	public void draw(GOut g) {
	    spr.draw(g, sc());
	}
	
	public boolean checkhit(Coord c) {
	    return(spr.checkhit(c));
	}
    }
    
    public static class TexPart extends OffsetPart {
	public Tex tex;
	
	public TexPart(Tex tex, int z, int subz, Coord moff, Coord soff) {
	    super(z, subz, moff, soff);
	    this.tex = tex;
	}
	
	public void setup(Coord cc, Coord off) {
	    super.setup(cc, off);
	    ul = sc();
	    lr = sc().add(tex.sz());
	}
	
	public void draw(GOut g) {
	    g.image(tex, sc());
	}
	
	public boolean checkhit(Coord c) {
	    if(!(this.tex instanceof TexI))
		return(false);
	    c = c.add(moff.inv()).add(soff.inv());
	    TexI img = (TexI)this.tex;
	    if((c.x < 0) || (c.y < 0) || (c.x >= img.sz().x) || (c.y >= img.sz().y))
		return(false);
	    int cl = img.getRGB(c);
	    return(Utils.rgbm.getAlpha(cl) >= 128);
	}
    }
    
    protected CSprite(Owner owner, Resource res) {
	super(owner, res);
    }

    public boolean checkhit(Coord c) {
	for(Part p : frame) {
	    if(p.checkhit(c))
		return(true);
	}
	return(false);
    }

    public void setup(Drawer d, Coord cc, Coord off) {
	setup(frame, d, cc, off);
    }

    public Object stateid() {
	return(this);
    }
    
    public void addnegative() {
	for(Resource.Image img : res.layers(Resource.imgc)) {
	    if(img.id < 0)
		add(img);
	}
    }
    
    public void add(SimpleSprite ss) {
	frame.add(new SSPart(ss));
    }

    public void add(Resource.Image img) {
	add(new SimpleSprite(img, res.layer(Resource.negc).cc));
    }
    
    public void add(Tex tex, int z, Coord moff, Coord soff) {
	frame.add(new TexPart(tex, z, 0, moff, soff));
    }
}
