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

import haven.Gob.DmgInfo;

import java.awt.Color;
import java.util.LinkedList;

public class Fightview extends Widget {
    static Tex bg = Resource.loadtex("gfx/hud/bosq");
    static int height = 5;
    static int ymarg = 5;
    static Coord avasz = new Coord(27, 27);
    static Coord cavac = new Coord(MainFrame.innerSize.width - 100, 10);
    static Coord cgivec = new Coord(MainFrame.innerSize.width - 135, 10);
    static Coord meterc = new Coord(MainFrame.centerPoint.x - 85, 10);
    LinkedList<Relation> lsrel = new LinkedList<Relation>();
    public Relation current = null;
    public Indir<Resource> blk, batk, iatk;
    public long atkc = -1;
    public long atks = -1;
    public long atkcc = -1;
    public int off, def;
    private GiveButton curgive;
    private Avaview curava;
    private Widget comwdg, comwin;
    public static Fightview instance;
    public static long changed = 0;
    
    public class Relation {
        int gobid;
        public int bal, intns;
        public int off, def;
	public int ip, oip;
        Avaview ava;
	GiveButton give;
        
        public Relation(int gobid) {
            this.gobid = gobid;
            this.ava = new Avaview(Coord.z, Fightview.this, gobid, avasz);
	    this.give = new GiveButton(Coord.z, Fightview.this, 0, new Coord(15, 15));
        }
	
        public Tex name(){
            Gob gob = ui.sess.glob.oc.getgob(gobid);
	    if(gob != null){
		KinInfo k = gob.getattr(KinInfo.class);
		if(k != null){
		    return k.rendered();
		}
	    }
	    return null;
        }
        
        public Color color(){
            Gob gob = ui.sess.glob.oc.getgob(gobid);
	    if(gob != null){
		KinInfo k = gob.getattr(KinInfo.class);
		if(k != null){
		    return BuddyWnd.gc[k.group];
		}
	    }
	    return Color.WHITE;
        }
        
	public void give(int state) {
	    if(this == current)
		curgive.state = state;
	    this.give.state = state;
	}
	
	public void show(boolean state) {
	    ava.visible = state;
	    give.visible = state;
	}
	
	public void remove() {
	    ui.destroy(ava);
	    ui.destroy(give);
	}
    }
    
    static {
        Widget.addtype("frv", new WidgetFactory() {
            public Widget create(Coord c, Widget parent, Object[] args) {
                return(new Fightview(new Coord(MainFrame.innerSize.width - 10, c.y), parent));
            }
        });
    }
    
    public Fightview(Coord c, Widget parent) {
        super(c.add(-bg.sz().x, 0), new Coord(bg.sz().x, (bg.sz().y + ymarg) * height), parent);
	SlenHud s = ui.slen;
	curgive = new GiveButton(cgivec, ui.root, 0) {
		public void wdgmsg(String name, Object... args) {
		    if(name == "click")
			Fightview.this.wdgmsg("give", current.gobid, args[0]);
		}
	    };
	curava = new Avaview(cavac, ui.root, -1) {
		public void wdgmsg(String name, Object... args) {
		    if(name == "click")
			Fightview.this.wdgmsg("click", current.gobid, args[0]);
		}
	    };
	curava.showname = true;
	comwdg = new ComMeter(meterc, ui.root, this);
	comwin = new ComWin(s, this);
	instance = this;
    }
    
    public void destroy() {
	instance = null;
	ui.destroy(curgive);
	ui.destroy(curava);
	ui.destroy(comwdg);
	ui.destroy(comwin);
	super.destroy();
    }
    
    public void draw(GOut g) {
        curava.c.x = MainFrame.innerSize.width - 100;
        curgive.c.x = MainFrame.innerSize.width - 135;
        c.x = MainFrame.innerSize.width - 10 - bg.sz().x;
        int y = 0;
        for(Relation rel : lsrel) {
            if(rel == current) {
		rel.show(false);
                continue;
	    }
            Color col = rel.color();
            rel.ava.color = col;
            g.chcolor(col);
            g.image(bg, new Coord(0, y));
            g.chcolor();
            rel.ava.c = new Coord(25, ((bg.sz().y - rel.ava.sz.y) / 2) + y);
	    rel.give.c = new Coord(5, 4 + y);
	    rel.show(true);
	    Tex name = rel.name();
	    if(name != null){
		g.image(name, new Coord(65, y-2));
	    }
	    String str = String.format("$img[gfx/hud/combat/bal]%d/%d $img[gfx/hud/combat/ip]%d/%d\n",rel.bal, rel.intns, rel.ip, rel.oip);
	    str += "$img[gfx/hud/combat/off]"+((int)rel.off/100);
	    str += " $img[gfx/hud/combat/def]"+((int)rel.def/100);
	    Tex text = RichText.render(str, 0).tex();
	    g.image(text, new Coord(65, y + 11));
	    text.dispose();
            //g.text(String.format("%d %d %d/%d", rel.bal, rel.intns, new Coord(65, y + 10));
            y += bg.sz().y + ymarg;
        }
        super.draw(g);
        //draw DMG over cur ava
        Gob gob = ui.sess.glob.oc.getgob(current.gobid);
        if(gob != null){
            Coord cc = curava.c.add(3, curava.sz.y-3).sub(c);
            for(DmgInfo i:gob.dmgmap.values()){
        	g.aimage(i.img, cc, 0, 1);
        	cc.y -= i.img.sz().y +2;
            }
        }
        
    }
    
    @SuppressWarnings("serial")
    public static class Notfound extends RuntimeException {
        public final int id;
        
        public Notfound(int id) {
            super("No relation for Gob ID " + id + " found");
            this.id = id;
        }
    }
    
    private Relation getrel(int gobid) {
        for(Relation rel : lsrel) {
            if(rel.gobid == gobid)
                return(rel);
        }
        throw(new Notfound(gobid));
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
        if(sender instanceof Avaview) {
            for(Relation rel : lsrel) {
                if(rel.ava == sender)
                    wdgmsg("click", rel.gobid, args[0]);
            }
            return;
        }
	if(sender instanceof GiveButton) {
            for(Relation rel : lsrel) {
                if(rel.give == sender)
                    wdgmsg("give", rel.gobid, args[0]);
            }
            return;
	}
        super.wdgmsg(sender, msg, args);
    }
    
    private Indir<Resource> n2r(int num) {
	if(num < 0)
	    return(null);
	return(ui.sess.getres(num));
    }

    public void uimsg(String msg, Object... args) {
	changed = System.currentTimeMillis();
        if(msg == "new") {
            Relation rel = new Relation((Integer)args[0]);
            rel.ava.showname = false;
            rel.bal = (Integer)args[1];
            rel.intns = (Integer)args[2];
	    rel.give((Integer)args[3]);
            rel.ip = (Integer)args[4];
            rel.oip = (Integer)args[5];
            rel.off = (Integer)args[6];
            rel.def = (Integer)args[7];
            lsrel.addFirst(rel);
            return;
        } else if(msg == "del") {
            Relation rel = getrel((Integer)args[0]);
	    rel.remove();
            lsrel.remove(rel);
            return;
        } else if(msg == "upd") {
            Relation rel = getrel((Integer)args[0]);
            rel.bal = (Integer)args[1];
            rel.intns = (Integer)args[2];
	    rel.give((Integer)args[3]);
	    rel.ip = (Integer)args[4];
	    rel.oip = (Integer)args[5];
            return;
	} else if(msg == "updod") {
	    Relation rel = getrel((Integer)args[0]);
	    rel.off = (Integer)args[1];
	    rel.def = (Integer)args[2];
	    return;
        } else if(msg == "cur") {
			try {
				Relation rel = getrel((Integer)args[0]);
				if(current != null && rel.gobid == current.gobid) return; // new
				makeCurrent(rel); // new
			} catch(Notfound e) {
				current = null;
			}
           /* try {
                Relation rel = getrel((Integer)args[0]);
                lsrel.remove(rel);
                lsrel.addFirst(rel);
		current = rel;
		curgive.state = rel.give.state;
		curava.avagob = rel.gobid;
		curava.color = rel.color();
            } catch(Notfound e) {
		current = null;
		   }*/
            return;
        } else if(msg == "atkc") {
	    long now = System.currentTimeMillis();
	    atkc = now + (((Integer)args[0]) * 60);
	    if(atks == -1)
		atks = now;
	    atkcc = atkc-atks;
	    return;
	} else if(msg == "blk") {
	    blk = n2r((Integer)args[0]);
	    return;
	} else if(msg == "atk") {
	    batk = n2r((Integer)args[0]);
	    iatk = n2r((Integer)args[1]);
	    return;
        } else if(msg == "offdef") {
	    off = (Integer)args[0];
	    def = (Integer)args[1];
	    return;
	}
        super.uimsg(msg, args);
    }
	
	/////////
	

	public void makeCurrent(Relation rel){ // new
		lsrel.remove(rel);
		lsrel.addFirst(rel);
		current = rel;
		curgive.state = rel.give.state;
		curava.avagob = rel.gobid;
		curava.color = rel.color();
	}
	
	public void currentUp(){ // new
		if(lsrel.size() > 1){
			Relation relFirst = lsrel.get(0);
			Relation relSecond = lsrel.get(1);
			
			lsrel.remove(relFirst);
			lsrel.addLast(relFirst);
			current = relSecond;
			curgive.state = relSecond.give.state;
			curava.avagob = relSecond.gobid;
			curava.color = relSecond.color();
		}
	}
	
	public void currentDown(){ // new
		if(lsrel.size() > 1){
			Relation relLast = lsrel.get(lsrel.size() - 1);
			
			lsrel.remove(relLast);
			lsrel.addFirst(relLast);
			current = relLast;
			curgive.state = relLast.give.state;
			curava.avagob = relLast.gobid;
			curava.color = relLast.color();
		}
	}
	
	public Relation getRelation(int gobid){ // new
		for(Relation rel : lsrel){
			if(rel.gobid == gobid)
				return rel;
		}
		return null;
	}
	
	//String memAttack = null;
	public void attackCurrent(/*String attackName*/){// new
		/*boolean memorized = false;
		
		if(batk != null && attackName != null && attackName.equals(memAttack) ){
			memorized = true;
		}
		
		memAttack = attackName;
		//System.out.println(memorized);
		if(current != null && !memorized){
			current.ava.mousedown(Coord.z, 1);
		}*/
		boolean suppress = false;
		if(batk != null) suppress = true;
		
		if(current != null && !suppress){
			current.ava.mousedown(Coord.z, 1);
		}
	}
}
