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

import java.awt.Color;
import java.awt.Font;

public class ComWin extends HWindow {
    static Tex iptex = Resource.loadtex("gfx/hud/combat/ip");
    static Tex sword = Resource.loadtex("gfx/hud/combat/off");
    static Tex shield = Resource.loadtex("gfx/hud/combat/def");
    static Tex bal = Resource.loadtex("gfx/hud/combat/bal");
    static Tex intns = Resource.loadtex("gfx/hud/combat/intns");
    
    static Text.Foundry fnd = new Text.Foundry(new Font("SansSerif", Font.BOLD, 12));
    Fightview fv;

    static Coord
        moc = new Coord(305, 10),
        mdc = new Coord(305, 30),
        ooc = new Coord(325, 10),
        odc = new Coord(325, 30),
        inic = new Coord(220, 60),
        balc = new Coord(270, 60),
        intc = new Coord(320, 60),
        swc = new Coord(315, 17),
        shc = new Coord(315, 37);
    
    static Color 
    	offcol = new Color(255, 0, 0),
    	offemty = new Color(128,64,64),
    	defcol = new Color(0, 0, 255),
    	defemty = new Color(64,64,128);
    
    public ComWin(Widget parent, Fightview fv) {
	super(parent, "Combat", false);
	this.fv = fv;
	(new Label(new Coord(10, 5), this, "Attack:")).setcolor(Color.BLACK);
	new Label(new Coord(10, 55), this, "Maneuver:").setcolor(Color.BLACK);
    }
    
    public void draw(GOut g) {
	super.draw(g);
	Resource res;
	boolean hasbatk = (fv.batk != null) && (fv.batk.get() != null);
	boolean hasiatk = (fv.iatk != null) && (fv.iatk.get() != null);
	if(hasbatk) {
	    res = fv.batk.get();
	    g.image(res.layer(Resource.imgc).tex(), new Coord(15, 20));
	    if(!hasiatk) {
		g.chcolor(0, 0, 0, 255);
		g.atext(res.layer(Resource.action).name, new Coord(50, 35), 0, 0.5);
		g.chcolor();
	    }
	}
	if(hasiatk) {
	    res = fv.iatk.get();
	    Coord c;
	    if(hasbatk)
		c = new Coord(18, 23);
	    else
		c = new Coord(15, 20);
	    g.image(res.layer(Resource.imgc).tex(), c);
	    g.chcolor(0, 0, 0, 255);
	    g.atext(res.layer(Resource.action).name, new Coord(50, 35), 0, 0.5);
	    g.chcolor();
	}
	if((fv.blk != null) && ((res = fv.blk.get()) != null)) {
	    g.image(res.layer(Resource.imgc).tex(), new Coord(15, 70));
	    g.chcolor(0, 0, 0, 255);
	    Resource.AButton act = res.layer(Resource.action);
	    String name = "";
	    if(act != null){
		name = act.name;
	    }
	    g.atext(name, new Coord(50, 85), 0, 0.5);
	    g.chcolor();
	}
	Fightview.Relation rel = fv.current;
	
	g.chcolor(offemty);
	g.frect(moc, new Coord(-100, 14));
	g.frect(ooc, new Coord(100, 14));
	g.chcolor(defemty);
	g.frect(mdc, new Coord(-100, 14));
	g.frect(odc, new Coord(100, 14));
	
	if(fv.off >= 100) {
	    g.chcolor(offcol);
	    g.frect(moc, new Coord(-fv.off / 100, 14));
	}
	if(fv.def >= 100) {
	    g.chcolor(defcol);
	    g.frect(mdc, new Coord(-fv.def / 100, 14));
	}
	g.chcolor();
	g.aimage(sword, swc, 0.5, 0.5);
	g.aimage(shield, shc, 0.5, 0.5);
	
	g.aimage(fnd.render(String.format("%d", fv.off/100)).tex(), moc.sub(50, -7), 0.5, 0.5);
	g.aimage(fnd.render(String.format("%d", fv.def/100)).tex(), mdc.sub(50, -7), 0.5, 0.5);
	if(rel != null) {
	    if(rel.off >= 200) {
		g.chcolor(offcol);
		g.frect(ooc, new Coord(rel.off / 100, 14));
	    }
	    if(rel.def >= 200) {
		g.chcolor(defcol);
		g.frect(odc, new Coord(rel.def / 100, 14));
	    }
	    g.chcolor();
	    g.aimage(fnd.render(String.format("%d", rel.off/100)).tex(), ooc.add(50, 7), 0.5, 0.5);
	    g.aimage(fnd.render(String.format("%d", rel.def/100)).tex(), odc.add(50, 7), 0.5, 0.5);
	    
	    
	    g.chcolor();
	    g.aimage(iptex, inic, 1.5, 0.5);
	    g.aimage(bal, balc, 1.5, 0.5);
	    g.aimage(intns, intc, 1.5, 0.5);
	    
	    g.chcolor(0, 0, 0, 255);
	    g.atext(rel.ip+"/"+rel.oip, inic, 0, 0.5);
	    g.atext(String.format("%d", rel.intns), intc, 0, 0.5);
	    g.atext(String.format("%d", rel.bal), balc, 0, 0.5);
	    g.chcolor();
	}
	
	
	long now = System.currentTimeMillis();
	if(now < fv.atkc) {
	    g.chcolor(255, 0, 128, 255);
	    g.frect(new Coord(200, 70), new Coord((int)(fv.atkc - now) / 100, 20));
	    g.chcolor();
	}
    }
}
