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
import java.awt.image.BufferedImage;
import java.util.*;

public abstract class ImageSprite extends Sprite {
    public Coord cc;
    public Collection<Part> curf = null;
    
    public class ImagePart extends Part {
	Resource.Image img;
	Tex ol = null;
	
	public ImagePart(Resource.Image img) {
	    super(img.z, img.subz);
	    this.img = img;
	}
	
	public void draw(BufferedImage b, Graphics g) {
	    Coord sc = sc().add(img.o);
	    if(img.gayp()) {
		Utils.drawgay(b, img.img, sc);
	    } else {
		g.drawImage(img.img, sc.x, sc.y, null);
	    }
	}
	
	public void draw(GOut g) {
	    g.image(img.tex(), sc().add(img.o));
	}
	
	public void drawol(GOut g) {
	    if(ol == null)
		ol = new TexI(Utils.outline(img.img, java.awt.Color.WHITE));
	    g.image(ol, sc().add(img.o).add(-1, -1));
	}
	
	public Coord sc() {
	    if(img.nooff)
		return(cc.add(ImageSprite.this.cc.inv()));
	    else
		return(cc.add(ImageSprite.this.cc.inv()).add(off));
	}

	public void setup(Coord cc, Coord off) {
	    super.setup(cc, off);
	    ul = sc().add(img.o);
	    lr = ul.add(img.sz);
	}
	
	public boolean checkhit(Coord c) {
	    c = c.add(ImageSprite.this.cc);
	    if((c.x < img.o.x) || (c.y < img.o.y) || (c.x >= img.o.x + img.sz.x) || (c.y >= img.o.y + img.sz.y))
		return(false);
	    int cl = img.img.getRGB(c.x - img.o.x, c.y - img.o.y);
	    return(Utils.rgbm.getAlpha(cl) >= 128);
	}
    }
    
    public static boolean[] decflags(Message sdt) {
	if(sdt == null)
	    return(new boolean[0]);
	boolean[] ret = new boolean[sdt.blob.length * 8];
	int i = 0;
	while(!sdt.eom()) {
	    int b = sdt.uint8();
	    for(int o = 0; o < 8; o++, i++)
		ret[i] = (b & (1 << o)) != 0;
	}
	return(ret);
    }

    protected ImageSprite(Owner owner, Resource res, Message sdt) {
	super(owner, res);
	Resource.Neg neg = res.layer(Resource.negc);
	if(neg == null)
	    throw(new ResourceException("No negative found", res));
	this.cc = neg.cc;
    }
    
    public boolean checkhit(Coord c) {
	Collection<Part> f = this.curf;
	synchronized(f) {
	    for(Part p : f) {
		if(p.checkhit(c))
		    return(true);
	    }
	}
	return(false);
    }

    public void setup(Drawer d, Coord cc, Coord off) {
	Collection<Part> f = this.curf;
	synchronized(f) {
	    setup(f, d, cc, off);
	}
    }
    
    public Object stateid() {
	return(curf);
    }
}
