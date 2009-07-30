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

import static haven.MCache.cmaps;
import static haven.MCache.tilesz;
import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class MiniMap extends Widget {
    public static final URL mmbase;
    static Map<String, Tex> grids = new WeakHashMap<String, Tex>();
    static Set<String> loading = new HashSet<String>();
    static Loader loader = new Loader();
    public static final Tex bg = Resource.loadtex("gfx/hud/mmap/ptex");
    public static final Tex nomap = Resource.loadtex("gfx/hud/mmap/nomap");
    public static final Resource plx = Resource.load("gfx/hud/mmap/x");
    MapView mv;
    
    static {
	try {
	    mmbase = new URL("http://www.havenandhearth.com/mm/");
	} catch(MalformedURLException e) {
	    throw(new Error(e));
	}
    }
    
    static class Loader implements Runnable {
	Thread me = null;
	
	public void run() {
	    try {
		while(true) {
		    String grid;
		    synchronized(grids) {
			grid = null;
			for(String cg : loading) {
			    grid = cg;
			    break;
			}
		    }
		    if(grid == null)
			break;
		    try {
			URL url = new URL(mmbase, grid + ".png");
			URLConnection c = url.openConnection();
			c.addRequestProperty("User-Agent", "Haven/1.0");
			InputStream in = c.getInputStream();
			BufferedImage img;
			try {
			    img = ImageIO.read(in);
			} finally {
			    in.close();
			}
			Tex tex = new TexI(img);
			synchronized(grids) {
			    grids.put(grid, tex);
			    loading.remove(grid);
			}
		    } catch(IOException e) {
			synchronized(grids) {
			    grids.put(grid, null);
			    loading.remove(grid);
			}
		    }
		}
	    } finally {
		synchronized(this) {
		    me = null;
		}
	    }
	}
	
	void start() {
	    synchronized(this) {
		if(me == null) {
		    me = new Thread(Utils.tg(), this, "Minimap loader");
		    me.setDaemon(true);
		    me.start();
		}
	    }
	}
	
	void req(MCache.Grid grid) {
	    synchronized(grids) {
		if(loading.contains(grid.mnm))
		    return;
		loading.add(grid.mnm);
		start();
	    }
	}
    }
    
    public MiniMap(Coord c, Coord sz, Widget parent, MapView mv) {
	super(c, sz, parent);
	this.mv = mv;
    }
    
    public void draw(GOut g) {
	Coord tc = mv.mc.div(tilesz);
	Coord ulg = tc.div(cmaps);
	while((ulg.x * cmaps.x) - tc.x + (sz.x / 2) > 0)
	    ulg.x--;
	while((ulg.y * cmaps.y) - tc.y + (sz.y / 2) > 0)
	    ulg.y--;
	boolean missing = false;
	g.image(bg, Coord.z);
	outer:
	for(int y = ulg.y; (y * cmaps.y) - tc.y + (sz.y / 2) < sz.y; y++) {
	    for(int x = ulg.x; (x * cmaps.x) - tc.x + (sz.x / 2) < sz.x; x++) {
		Coord cg = new Coord(x, y);
		MCache.Grid grid;
		synchronized(ui.sess.glob.map.req) {
		    synchronized(ui.sess.glob.map.grids) {
			grid = ui.sess.glob.map.grids.get(cg);
			if(grid == null)
			    ui.sess.glob.map.request(cg);
		    }
		}
		if(grid == null)
		    continue;
		if(grid.mnm == null) {
		    missing = true;
		    break outer;
		}
		Tex tex;
		synchronized(grids) {
		    if(grids.containsKey(grid.mnm)) {
			tex = grids.get(grid.mnm);
		    } else {
			loader.req(grid);
			continue;
		    }
		}
		if(tex == null)
		    continue;
		g.image(tex, cg.mul(cmaps).add(tc.inv()).add(sz.div(2)));
	    }
	}
	if(missing) {
	    g.image(nomap, Coord.z);
	} else {
	    if(!plx.loading) {
		synchronized(ui.sess.glob.party.memb) {
		    for(Party.Member m : ui.sess.glob.party.memb.values()) {
			Coord ptc = m.getc().div(tilesz);
			ptc = ptc.add(tc.inv()).add(sz.div(2));
			g.chcolor(m.col.getRed(), m.col.getGreen(), m.col.getBlue(), 128);
			g.image(plx.layer(Resource.imgc).tex(), ptc.add(plx.layer(Resource.negc).cc.inv()));
			g.chcolor();
		    }
		}
	    }
	}
	super.draw(g);
    }
}
