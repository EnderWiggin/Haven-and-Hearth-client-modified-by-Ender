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

import haven.Text.Foundry;

import java.awt.Font;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Equipory extends Window implements DTarget {
    static final Pattern patt = Pattern.compile("Armor class: (\\d+)/(\\d+)", Pattern.CASE_INSENSITIVE);
    List<Inventory> epoints;
    List<Item> equed;
    static final Tex bg = Resource.loadtex("gfx/hud/equip/bg");
    int avagob = -1;
    private Label aclbl;
	
    static Coord ecoords[] = {
	new Coord(0, 0),
	new Coord(244, 0),
	new Coord(0, 31),
	new Coord(244, 31),
	new Coord(0, 62),
	new Coord(244, 62),
	new Coord(0, 93),
	new Coord(244, 93),
	new Coord(0, 124),
	new Coord(244, 124),
	new Coord(0, 155),
	new Coord(244, 155),
	new Coord(0, 186),
	new Coord(244, 186),
	new Coord(0, 217),
	new Coord(244, 217),
    };
	
    static {
	Widget.addtype("epry", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Equipory(c, parent));
		}
	    });
    }
	
    public Equipory(Coord c, Widget parent) {
	super(c, new Coord(0, 0), parent, "Equipment");
	canhastrash = false;
	epoints = new ArrayList<Inventory>();
	equed = new ArrayList<Item>(ecoords.length);
	//new Img(new Coord(32, 0), bg, this);
	for(int i = 0; i < ecoords.length; i++) {
	    epoints.add(new Inventory(ecoords[i], new Coord(1, 1), this));
	    equed.add(null);
	}
	pack();
	Foundry fnd = new Foundry(new Font("SansSerif", Font.PLAIN, 12));
	int x = new Label(new Coord(0, 249), this, "Armor class: ", fnd).sz.x;
	aclbl = new Label(new Coord(x, 249), this, "0/0", fnd);
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "set") {
	    synchronized(ui) {
		int i = 0, o = 0;
		while(i < equed.size()) {
		    if(equed.get(i) != null)
			equed.get(i).unlink();
		    int res = (Integer)args[o++];
		    if(res >= 0) {
			int q = (Integer)args[o++];
			Item ni = new Item(Coord.z, res, q, epoints.get(i), null);
			equed.set(i++, ni);
			if((o < args.length) && (args[o] instanceof String))
			    ni.settip((String)args[o++]);
		    } else {
			equed.set(i++, null);
		    }
		}
	    }
	    calcAC();
	} else if(msg == "setres") {
	    int i = (Integer)args[0];
	    Indir<Resource> res = ui.sess.getres((Integer)args[1]);
	    equed.get(i).chres(res, (Integer)args[2]);
	    calcAC();
	} else if(msg == "settt") {
	    int i = (Integer)args[0];
	    String tt = (String)args[1];
	    equed.get(i).settip(tt);
	    calcAC();
	} else if(msg == "ava") {
	    avagob = (Integer)args[0];
	}
    }
    
    private void calcAC(){
	int abs = 0, def = 0;
	for(Item itm : equed){
	    if(itm != null){
		String t = itm.tooltip;
		if(t != null){
		    try{
			Matcher m =patt.matcher(t); 
			if(m.find()){
			    def += Integer.parseInt(m.group(1));
			    abs += Integer.parseInt(m.group(2));
			}
		    } catch(IllegalStateException e){
			System.out.println(e.getMessage());
		    }
		}
	    }
	}
	aclbl.settext(def+"/"+abs+" ("+(def+abs)+")");
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	int ep;
	if((ep = epoints.indexOf(sender)) != -1) {
	    if(msg == "drop") {
		wdgmsg("drop", ep);
		return;
	    } else if(msg == "xfer") {
		return;
	    }
	}
	if((ep = equed.indexOf(sender)) != -1) {
	    if(msg == "take")
		wdgmsg("take", ep, args[0]);
	    else if(msg == "itemact")
		wdgmsg("itemact", ep);
	    else if(msg == "transfer")
		wdgmsg("transfer", ep, args[0]);
	    else if(msg == "iact")
		wdgmsg("iact", ep, args[0]);
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }
	
    public boolean drop(Coord cc, Coord ul) {
	wdgmsg("drop", -1);
	return(true);
    }
	
    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }
	
    public void cdraw(GOut g) {
	if(folded)
	    return;
	Coord avac = new Coord(32, 0);
	g.image(bg, avac);
	if(avagob != -1) {
	    Gob gob = ui.sess.glob.oc.getgob(avagob);
	    if(gob != null) {
		Avatar ava = gob.getattr(Avatar.class);
		if(ava != null)
		    g.image(ava.rend, avac);
	    }
	}
    }
}
