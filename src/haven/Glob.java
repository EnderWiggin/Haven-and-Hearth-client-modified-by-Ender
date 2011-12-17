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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;
import java.util.TreeSet;

import ender.timer.Timer;

public class Glob {
    public static final int GMSG_TIME = 0;
    public static final int GMSG_ASTRO = 1;
    public static final int GMSG_LIGHT = 2;
	
    public Astronomy ast;
    public OCache oc = new OCache(this);
    public MCache map;
    public Session sess;
    public Party party;
    public Collection<Resource> paginae = new TreeSet<Resource>();
    public Map<String, CAttr> cattr = new HashMap<String, CAttr>();
    public Map<Integer, Buff> buffs = new TreeMap<Integer, Buff>();
    public java.awt.Color amblight = null;
    
    public Glob(Session sess) {
	
	this.sess = sess;
	map = new MCache(sess);
	party = new Party(this);
	//adding extended commands
	paginae.add(Resource.load("paginae/act/add"));
	paginae.add(Resource.load("paginae/add/rad"));
	paginae.add(Resource.load("paginae/add/radar"));
	paginae.add(Resource.load("paginae/add/shhd"));
	paginae.add(Resource.load("paginae/add/wiki"));
	paginae.add(Resource.load("paginae/add/animal"));
	paginae.add(Resource.load("paginae/add/plants"));
	paginae.add(Resource.load("paginae/add/global"));
	paginae.add(Resource.load("paginae/add/timer"));
	paginae.add(Resource.load("paginae/add/study"));
	paginae.add(Resource.load("paginae/add/hide"));
	paginae.add(Resource.load("paginae/add/hide/tree"));
	paginae.add(Resource.load("paginae/add/hide/flav"));
	paginae.add(Resource.load("paginae/add/hide/wall"));
	paginae.add(Resource.load("paginae/add/hide/gate"));
	paginae.add(Resource.load("paginae/add/hide/bush"));
	paginae.add(Resource.load("paginae/add/hide/thik"));
	paginae.add(Resource.load("paginae/add/hide/cabi"));
	paginae.add(Resource.load("paginae/add/hide/mans"));
	paginae.add(Resource.load("paginae/add/hide/plan"));
	paginae.add(Resource.load("paginae/add/hide/ston"));
    }
    
    public static class CAttr extends Observable {
	String nm;
	int base, comp;
	
	public CAttr(String nm, int base, int comp) {
	    this.nm = nm.intern();
	    this.base = base;
	    this.comp = comp;
	}
	
	public void update(int base, int comp) {
	    if((base == this.base) && (comp == this.comp))
		return;
	    int delta = (this.base>0)?base - this.base:0;
	    if(delta > 0){
		String str = "Your "+nm.toUpperCase()+" raised by "+delta+" points";
		UI.instance.cons.out.println(str);
		UI.instance.slen.error(str);
	    }
	    this.base = base;
	    this.comp = comp;
	    setChanged();
	    notifyObservers(null);
	}
    }
	
    private static double defix(int i) {
	return(((double)i) / 1e9);
    }
	
    public void blob(Message msg) {
	while(!msg.eom()) {
	    switch(msg.uint8()) {
	    case GMSG_TIME:
		Timer.server = msg.int32();;
		Timer.local = System.currentTimeMillis()/1000;
		break;
	    case GMSG_ASTRO:
		double dt = defix(msg.int32());
		double mp = defix(msg.int32());
		double yt = defix(msg.int32());
		boolean night = (dt < 0.25) || (dt > 0.75);
		ast = new Astronomy(dt, mp, yt, night);
		break;
	    case GMSG_LIGHT:
		amblight = msg.color();
		break;
	    }
	}
    }
	
    public void paginae(Message msg) {
	synchronized(paginae) {
	    while(!msg.eom()) {
		int act = msg.uint8();
		if(act == '+') {
		    String nm = msg.string();
		    int ver = msg.uint16();
		    Resource res = Resource.load(nm, ver);
		    paginae.add(res);
		    UI ui = UI.instance;
		    if(ui.slen != null){
			res.loadwait();
			String str = String.format("You have gained access to '%s'", res.layer(Resource.action).name);
			ui.cons.out.println(str);
			ui.slen.error(str);
		    }
		} else if(act == '-') {
		    String nm = msg.string();
		    int ver = msg.uint16();
		    paginae.remove(Resource.load(nm, ver)); 
		}
	    }
	}
    }
    
    public void cattr(Message msg) {
	synchronized(cattr) {
	    while(!msg.eom()) {
		String nm = msg.string();
		int base = msg.int32();
		int comp = msg.int32();
		CAttr a = cattr.get(nm);
		if(a == null) {
		    a = new CAttr(nm, base, comp);
		    cattr.put(nm, a);
		} else {
		    a.update(base, comp);
		}
	    }
	}
    }
    
    public void buffmsg(Message msg) {
	String name = msg.string().intern();
	synchronized(buffs) {
	    if(name == "clear") {
		buffs.clear();
	    } else if(name == "set") {
		int id = msg.int32();
		Indir<Resource> res = sess.getres(msg.uint16());
		String tt = msg.string();
		int ameter = msg.int32();
		int nmeter = msg.int32();
		int cmeter = msg.int32();
		int cticks = msg.int32();
		boolean major = msg.uint8() != 0;
		Buff buff;
		if((buff = buffs.get(id)) == null) {
		    buff = new Buff(id, res);
		} else {
		    buff.res = res;
		}
		if(tt.equals(""))
		    buff.tt = null;
		else
		    buff.tt = tt;
		buff.ameter = ameter;
		buff.nmeter = nmeter;
		buff.ntext = null;
		buff.cmeter = cmeter;
		buff.cticks = cticks;
		buff.major = major;
		buff.gettime = System.currentTimeMillis();
		buffs.put(id, buff);
	    } else if(name == "rm") {
		int id = msg.int32();
		buffs.remove(id);
	    }
	}
    }
}
