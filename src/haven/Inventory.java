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

import java.awt.image.BufferedImage;

public class Inventory extends Widget implements DTarget {
    public static Tex invsq = Resource.loadtex("gfx/hud/invsq");
    protected static BufferedImage[] tbtni = new BufferedImage[] {
	Resource.loadimg("gfx/hud/trashu"),
	Resource.loadimg("gfx/hud/trashd"),
	Resource.loadimg("gfx/hud/trashh")};
    Coord isz;
    private IButton trash;
    private boolean wait = false;
    
    static {
	Widget.addtype("inv", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Inventory(c, (Coord)args[0], parent));
		}
	    });
    }

    public void draw(GOut g) {
	Coord c = new Coord();
	Coord sz = invsq.sz().add(new Coord(-1, -1));
	for(c.y = 0; c.y < isz.y; c.y++) {
	    for(c.x = 0; c.x < isz.x; c.x++) {
		g.image(invsq, c.mul(sz));
	    }
	}
	super.draw(g);
    }
	
    public Inventory(Coord c, Coord sz, Widget parent) {
	super(c, invsq.sz().add(new Coord(-1, -1)).mul(sz).add(new Coord(17, 1)), parent);
	isz = sz;
	trash = new IButton(Coord.z, this, tbtni[0], tbtni[1], tbtni[2]);
	trash.visible = parent.canhastrash;
	recalcsz();
    }
    
    public boolean mousewheel(Coord c, int amount) {
	if(amount < 0)
	    wdgmsg("xfer", -1, ui.modflags());
	if(amount > 0)
	    wdgmsg("xfer", 1, ui.modflags());
	return(true);
    }
    
    public boolean drop(Coord cc, Coord ul, Item item) {
	wdgmsg("drop", ul.add(new Coord(15, 15)).div(invsq.sz()));
	return(true);
    }
	
    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "sz") {
	    isz = (Coord)args[0];
	    recalcsz();
	}
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == trash) {
	    if(wait){return;}
	    wait = true;
	    new ConfirmWnd(parent.c.add(c).add(trash.c), ui.root, getmsg(), new ConfirmWnd.Callback() {
		public void result(Boolean res) {
		    wait = false;
		    if(res){
			empty();
		    }
		}
	    });
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
    
    public void showtrash(boolean visible){
	trash.visible = visible;
	recalcsz();
    }
    
    private String getmsg(){
	if(parent instanceof Window){
	    String str = ((Window)parent).cap.text;
	    return "Drop all items from the "+str.toLowerCase()+" to ground?";
	}
	return "Drop all items to ground?";
    }
    
    private void empty(){
	for(Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
	    if(!wdg.visible)
		continue;
	    if(!(wdg instanceof Item)){
		continue;
	    }
	    wdg.wdgmsg("drop", Coord.z);
	}
    }
    
    private boolean needshift(){
	if(parent instanceof Window){
	    Window wnd = (Window)parent;
	    if(wnd.cap != null){
		String str = wnd.cap.text;
		if(str.equals("Oven")){
		    return true;
		}
		if(str.equals("Finery Forge")){
		    return true;
		}
		if(str.equals("Steel Crucible")){
		    return true;
		}
	    }
	}
	return false;
    }
    
    private void recalcsz(){
	sz = invsq.sz().add(new Coord(-1, -1)).mul(isz).add(new Coord(1, 1));
	if(trash.visible){
	    trash.c = sz.sub(0, invsq.sz().y);
	    hsz = sz.add(16,0);
	    if(needshift()){//small inventory, button should be shifted (Finery forge, oven, crucible)
		trash.c = trash.c.add(18,0);
		hsz = hsz.add(18,0);
	    }
	} else {
	    hsz = null;
	}
    }
}
