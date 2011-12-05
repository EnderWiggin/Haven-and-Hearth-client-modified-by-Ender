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

public class ComMeter extends Widget {
    static final String POSKEY = "commeter_pos";
    static Tex sword = Resource.loadtex("gfx/hud/combat/com/noffdeff");
    static Tex sbg = Resource.loadtex("gfx/hud/combat/com/bgoffdeff");
    static Tex ip = Resource.loadtex("gfx/hud/combat/ip");
    static Text.Foundry intf = new Text.Foundry(new Font("Serif", Font.BOLD, 16));
    static Text.Foundry fnd = new Text.Foundry(new Font("SansSerif", Font.PLAIN, 10));
    static Coord
	moc = new Coord(54, 61),
	mdc = new Coord(54, 71),
	ooc = new Coord(80, 61),
	odc = new Coord(80, 71);
    static Coord manc = new Coord(4,20);
    static Coord batk = new Coord(100,20);
    static Coord catk = new Coord(103,23);
    static Coord intc = new Coord(66,25);
    static Coord balc = new Coord(66,40);
    static Coord ipcf = new Coord(50,64);
    static Coord ipcr = new Coord(75,64);
    static Coord bgc = new Coord(68,35);
    static Coord bgr = new Coord(27,27);
    static Color offcol = new Color(255, 0, 0), defcol = new Color(0, 0, 255);
    
    Fightview fv;
    boolean dm;
    Coord doff;
    
    public ComMeter(Coord c, Widget parent, Fightview fv) {
        super(c, sword.sz(), parent);
	this.c = new Coord(Config.window_props.getProperty(POSKEY,this.c.toString()));
	this.fv = fv;
    }
    
    public void draw(GOut g) {
	Fightview.Relation rel = fv.current;
	Resource res;
	long now = System.currentTimeMillis();
	int end;
	g.image(sbg,Coord.z);
	if(now < fv.atkc){
	    end = (int)(360*((float)(now-fv.atks)/fv.atkcc));
	    g.chcolor(255,0,128,255);
	    g.fellipse(bgc,bgr,0,end);
	    g.chcolor();
	} else
	    fv.atks = -1;
	if(fv.batk != null && (res=fv.batk.get()) != null){
	    g.image(res.layer(Resource.imgc).tex(),batk);
	}
	if(fv.iatk != null && (res=fv.iatk.get()) != null){
	    g.image(res.layer(Resource.imgc).tex(),catk);
	}
	if(fv.blk != null && (res=fv.blk.get()) != null){
	    g.image(res.layer(Resource.imgc).tex(),manc);
	}
        g.image(sword, Coord.z);
	if(fv.off >= 200) {
	    g.chcolor(offcol);
	    g.frect(moc, new Coord(-fv.off / 200, 5));
	}
	if(fv.def >= 200) {
	    g.chcolor(defcol);
	    g.frect(mdc, new Coord(-fv.def / 200, 5));
	}
	g.chcolor();
	g.aimage(fnd.render(String.format("%d", fv.off/100)).tex(), moc.sub(25, -2), 0.5, 0.5);
	g.aimage(fnd.render(String.format("%d", fv.def/100)).tex(), mdc.sub(25, -2), 0.5, 0.5);
	if(rel != null) {
	    g.aimage(intf.render(String.format("%d", rel.intns)).tex(), intc, 0.5, 0.5);
	    g.aimage(intf.render(String.format("%d", rel.bal)).tex(), balc, 0.5, 0.5);
	    if(rel.off >= 200) {
		g.chcolor(offcol);
		g.frect(ooc, new Coord(rel.off / 200, 5));
	    }
	    if(rel.def >= 200) {
		g.chcolor(defcol);
		g.frect(odc, new Coord(rel.def / 200, 5));
	    }
	    g.chcolor();
	    g.aimage(fnd.render(String.format("%d", rel.off/100)).tex(), ooc.add(25, 2), 0.5, 0.5);
	    g.aimage(fnd.render(String.format("%d", rel.def/100)).tex(), odc.add(25, 2), 0.5, 0.5);
	}
	g.image(ip,ipcf);
	g.image(ip,ipcr);
	g.chcolor(0,0,0,255);
	if(rel != null){
	    g.aimage(fnd.render(String.format("%d",rel.oip)).tex(),ipcr.add(6,4),0.5,0.5);
	    g.aimage(fnd.render(String.format("%d",rel.ip)).tex(),ipcf.add(6,4),0.5,0.5);
    }
    }
	
    public boolean mousedown(Coord c, int button) {
		super.mousedown(c,button);
    	parent.setfocus(this);
    	raise();
    	if(button == 1) {
    	    ui.grabmouse(this);
    	    dm = true;
    	    doff = c;
    	}
    	return(true);
    }
    	
    public boolean mouseup(Coord c, int button) {
		if(dm) {
		    ui.grabmouse(null);
		    dm = false;
		    Config.setWindowOpt(POSKEY, this.c.toString());
		}
		else
			super.mouseup(c, button);
		return(true);
    }
	
    public void mousemove(Coord c) {

		if(dm) {
		    this.c = this.c.add(c.add(doff.inv()));
		}else
			super.mousemove(c);
    }
}
