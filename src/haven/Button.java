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
import java.awt.image.BufferedImage;

public class Button extends Widget {
    static Tex bl = Resource.loadtex("gfx/hud/buttons/tbtn/left");
    static Tex br = Resource.loadtex("gfx/hud/buttons/tbtn/right");
    static Tex bt = Resource.loadtex("gfx/hud/buttons/tbtn/top");
    static Tex bb = Resource.loadtex("gfx/hud/buttons/tbtn/bottom");
    static Tex dt = Resource.loadtex("gfx/hud/buttons/tbtn/dtex");
    static Tex ut = Resource.loadtex("gfx/hud/buttons/tbtn/utex");
    public Text text;
    public BufferedImage cont;
    static Text.Foundry tf = new Text.Foundry(new Font("Serif", Font.PLAIN, 12), Color.YELLOW);
    boolean a = false;
    public Color color = Color.YELLOW;
	
    static {
	Widget.addtype("btn", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Button(c, (Integer)args[0], parent, (String)args[1]));
		}
	    });
	Widget.addtype("ltbtn", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(wrapped(c, (Integer)args[0], parent, (String)args[1]));
		}
	    });
    }
	
    public static Button wrapped(Coord c, int w, Widget parent, String text) {
	Button ret = new Button(c, w, parent, tf.renderwrap(text, w - 10));
	return(ret);
    }
        
    public Button(Coord c, Integer w, Widget parent, String text) {
	super(c, new Coord(w, 19), parent);
	this.text = tf.render(text);
	this.cont = this.text.img;
    }
        
    public Button(Coord c, Integer w, Widget parent, Text text) {
	super(c, new Coord(w, 19), parent);
	this.text = text;
	this.cont = text.img;
    }
	
    public Button(Coord c, Integer w, Widget parent, BufferedImage cont) {
	super(c, new Coord(w, 19), parent);
	this.cont = cont;
    }
	
    public void draw(GOut g) {
	synchronized(this) {
	    //Graphics g = graphics();
	    g.image(a?dt:ut, new Coord(3, 3), new Coord(sz.x - 6, 13));
	    g.image(bl, new Coord());
	    g.image(br, new Coord(sz.x - br.sz().x, 0));
	    g.image(bt, new Coord(3, 0), new Coord(sz.x - 6, bt.sz().y));
	    g.image(bb, new Coord(3, sz.y - bb.sz().y), new Coord(sz.x - 6, bb.sz().y));
	    Coord tc = sz.div(2).add(Utils.imgsz(cont).div(2).inv());
	    if(a)
		tc = tc.add(1, 1);
	    g.image(cont, new Coord(tc.x, tc.y));
	}
    }
	
    public void change(String text, Color col) {
	if(col == null)
	    col = Color.YELLOW;
	color = col;
	this.text = tf.render(text, col);
	this.cont = this.text.img;
    }
    
    public void change(String text) {
	change(text, null);
    }
    
    public void click() {
	try{
		if(ui.modflags() == 1 && text.text.equals("Feast!")){
			addons.MainScript.autoFeast();
			return;
		}
	}catch(Exception e){}
	wdgmsg("activate");
    }
    
    public void uimsg(String msg, Object... args) {
	if(msg == "ch") {
	    if(args.length > 1)
		change((String)args[0], (Color)args[1]);
	    else
		change((String)args[0]);
	}
    }
    
    public boolean mousedown(Coord c, int button) {
	if(button != 1)
	    return(false);
	a = true;
	ui.grabmouse(this);
	return(true);
    }
	
    public boolean mouseup(Coord c, int button) {
	if(a && button == 1) {
	    a = false;
	    ui.grabmouse(null);
	    if(c.isect(new Coord(0, 0), sz))
		click();
	    return(true);
	}
	return(false);
    }
}
