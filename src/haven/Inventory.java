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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Inventory extends Widget implements DTarget {
    public static final Tex invsq;  // InvisibleSquare = 1x1 cell
    public static final Coord invSqSize; //size of invsq
    public static final Coord invSqSizeSubOne; //size of invsq.sub(1,1)
    private static final Comparator<Item> cmp_asc = new Comparator<Item>() {
	@Override
	public int compare(Item o1, Item o2) {
	    return o1.q - o2.q;
	}
    };
    private static final Comparator<Item> cmp_desc = new Comparator<Item>() {
	@Override
	public int compare(Item o1, Item o2) {
	    return o2.q - o1.q;
	}
    };
    protected static BufferedImage[] tbtni = new BufferedImage[] {
	Resource.loadimg("gfx/hud/trashu"),
	Resource.loadimg("gfx/hud/trashd"),
	Resource.loadimg("gfx/hud/trashh")};
    public Coord isz;
    private final IButton trash;
    private final AtomicBoolean wait = new AtomicBoolean(false);

    static {
        invsq = Resource.loadtex("gfx/hud/invsq"); // InvisibleSquare = 1x1 cell
        invSqSize = invsq.sz();  //32x32
        invSqSizeSubOne = Inventory.invSqSize.sub(1, 1);
    }

    static {
	Widget.addtype("inv", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Inventory(c, (Coord)args[0], parent));
		}
	    });
    }

    public void draw(GOut g) {
	Coord c = new Coord();
	Coord sz = invSqSizeSubOne;
	for(c.y = 0; c.y < isz.y; c.y++) {
	    for(c.x = 0; c.x < isz.x; c.x++) {
		g.image(invsq, c.mul(sz));
	    }
	}
	super.draw(g);
    }
	
    public Inventory(Coord c, Coord sz, Widget parent) {
	super(c, invSqSizeSubOne.mul(sz).add(new Coord(17, 1)), parent);
	isz = sz;
	if (parent.canhastrash) {
	    trash = new IButton(Coord.z, this, tbtni[0], tbtni[1], tbtni[2]);
	    trash.visible = true;
	} else {
	    trash = null;
	}
	recalcsz();
    }
    
    public boolean mousewheel(Coord c, int amount) {
	int mod = ui.modflags();
	if((mod & 6) == 6) { //
	    mod = 7;
	}
	if(amount < 0)
	    wdgmsg("xfer", -1, mod);
	if(amount > 0)
	    wdgmsg("xfer", 1, mod);
	return(true);
    }
    
    public boolean drop(Coord cc, Coord ul) {
	wdgmsg("drop", ul.add(new Coord(15, 15)).div(invSqSize));
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
		if(checkTrashButton(sender)) {
			if(wait.get()){return;}
			wait.set(true);
			new ConfirmWnd(parent.c.add(c).add(trash.c), ui.root, getmsg(), new ConfirmWnd.Callback() {
			public void result(Boolean res) {
				wait.set(false);
				if(res){
				empty();
				}
			}
			});
		} else if(msg.equals("transfer-same")){
			process(getSame((String) args[0], (Boolean) args[1]), "transfer");
		} else if(msg.equals("drop-same")){
			process(getSame((String) args[0], (Boolean) args[1]), "drop");
		} else {
			super.wdgmsg(sender, msg, args);
		}
    }
    
    private void process(List<Item> items, String action) {
	for (Item item : items){
	    item.wdgmsg(action, Coord.z);
	}
    }

    private List<Item> getSame(String name, boolean ascending) {
	List<Item> items = new ArrayList<Item>();
	for (Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
	    if (wdg.visible && wdg instanceof Item) {
		if (((Item) wdg).name().equals(name))
		    items.add((Item) wdg);
	    }
	}
	Collections.sort(items, ascending?cmp_asc:cmp_desc);
	return items;
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
	    if(wdg.visible && wdg instanceof Item){
	    wdg.wdgmsg("drop", Coord.z);
        }
	}
    }
    
    private boolean needshift(){
	if(parent instanceof Window){
	    Window wnd = (Window)parent;
	    if(wnd.cap != null){
		String str = wnd.cap.text;
		if(str.equals("Oven") || str.equals("Finery Forge") || str.equals("Steel Crucible")){
		    return true;
		}
	    }
	}
	return false;
    }

    private void recalcsz(){
	sz = invSqSizeSubOne.mul(isz).add(new Coord(1, 1));
	if((trash!=null) && (trash.visible)){
	    trash.c = sz.sub(0, invSqSize.y);
	    hsz = sz.add(16,0);
	    if(needshift()){//small inventory, button should be shifted (Finery forge, oven, crucible)
		trash.c.x+=18;
		hsz.x+=18;
	    }
	} else {
	    hsz = null;
	}
    }

    protected boolean checkTrashButton(Widget w) {
        return trash != null && w == trash;
    }
}
