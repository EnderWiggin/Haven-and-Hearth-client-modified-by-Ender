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

public class Speedget extends Widget {
    public static final Tex imgs[][];
    public static final Coord tsz;
    public static final Color pspdc = new Color(160,255,160);
    private int cur, max, pspd;
    
    static {
	imgs = new Tex[4][3];
	String[] names = {"crawl", "walk", "run", "sprint"};
	String[] vars = {"dis", "off", "on"};
	int w = 0;
	for(int i = 0; i < 4; i++) {
	    for(int o = 0; o < 3; o++)
		imgs[i][o] = Resource.loadtex("gfx/hud/meter/rmeter/" + names[i] + "-" + vars[o]);
	    w += imgs[i][0].sz().x;
	}
	tsz = new Coord(w, imgs[0][0].sz().y);
	
	Widget.addtype("speedget", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    int cur = (Integer)args[0];
		    int max = (Integer)args[1];
		    return(new Speedget(c, parent, cur, max));
		}
	    });
    }

    public Speedget(Coord c, Widget parent, int cur, int max) {
	super(c, tsz, parent);
	pspd = this.cur = cur;
	this.max = max;
	ui.spd = this;
    }
    
    public void draw(GOut g) {
	int x = 0;
	for(int i = 0; i < 4; i++) {
	    Tex t;
	    if(i == pspd){
		g.chcolor(pspdc);
	    } else {
		g.chcolor();
	    }
	    if(i == cur)
		t = imgs[i][2];
	    else if(i > max)
		t = imgs[i][0];
	    else
		t = imgs[i][1];
	    g.image(t, new Coord(x, 0));
	    x += t.sz().x;
	}
    }
    
    public void uimsg(String msg, Object... args){
	if(msg == "cur")
	    cur = (Integer)args[0];
	else if(msg == "max"){
	    max = (Integer)args[0];
	    if((max > cur)&&(cur < pspd)){
		setspeed(Math.min(pspd, max), false);
	    }
	}
    }
    
    public boolean mousedown(Coord c, int button) {
	int x = 0;
	for(int i = 0; i < 4; i++) {
	    x += imgs[i][0].sz().x;
	    if(c.x < x) {
		setspeed(i, true);
		break;
	    }
	}
	return(true);
    }
    
    public void setspeed(int speed, boolean player){
		wdgmsg("set", speed);
		if(player){
			pspd = speed;
			Config.speed = speed;
			Config.saveOptions();
		}
    }
    
    public boolean mousewheel(Coord c, int amount) {
	if(max >= 0)
	    setspeed((cur + max + 1 + amount) % (max + 1), true);
	return(true);
    }
}
