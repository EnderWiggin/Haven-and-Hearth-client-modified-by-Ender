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

public class AnimSprite extends ImageSprite {
    private Frame[] frames;
    private int fno, te;
    public static final Factory fact = new Factory() {
	    public Sprite create(Owner owner, Resource res, Message sdt) {
		if(res.layer(Resource.animc) == null)
		    return(null);
		return(new AnimSprite(owner, res, sdt));
	    }
	};
    
    private static class Frame {
	Collection<Part> parts = new LinkedList<Part>();
	int dur;
	Object id;
    }
    
    private AnimSprite(Owner owner, Resource res, Message sdt) {
	super(owner, res, sdt);
	boolean[] flags = decflags(sdt);
	Collection<Part> stp = new LinkedList<Part>();
	for(Resource.Image img : res.layers(Resource.imgc)) {
	    if((img.id < 0) || ((img.id < flags.length) && flags[img.id]))
		stp.add(new ImagePart(img));
	}
	frames = null;
	for(Resource.Anim anim : res.layers(Resource.animc)) {
	    if((anim.id < 0) || ((anim.id < flags.length) && flags[anim.id])) {
		if(frames == null) {
		    frames = new Frame[anim.f.length];
		} else {
		    if(anim.f.length != frames.length)
			throw(new ResourceException("Attempting to combine animations of different lengths", res));
		}
		for(int i = 0; i < frames.length; i++) {
		    Frame f;
		    if(frames[i] == null) {
			f = frames[i] = new Frame();
			f.dur = anim.d;
			for(Part p : stp)
			    f.parts.add(p);
			f.id = (res.name + ":" + res.ver + ":" + i).intern();
		    }
		    f = frames[i];
		    for(int o = 0; o < anim.f[i].length; o++)
			f.parts.add(new ImagePart(anim.f[i][o]));
		}
	    }
	}
	if(frames == null) {
	    frames = new Frame[1];
	    Frame f = frames[0] = new Frame();
	    f.dur = 10000;
	    f.parts = stp;
	    f.id = (res.name + ":" + res.ver).intern();
	}
	fno = 0;
	te = 0;
	curf = frames[0].parts;
    }
    
    public boolean tick(int dt) {
	boolean rv = false;
	te += dt;
	while(te > frames[fno].dur) {
	    te -= frames[fno].dur;
	    if(++fno >= frames.length) {
		fno = 0;
		rv = true;
	    }
	}
	curf = frames[fno].parts;
	return(rv);
    }
    
    public Object stateid() {
	return(frames[fno].id);
    }
}
