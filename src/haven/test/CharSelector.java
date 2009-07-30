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

package haven.test;

import haven.*;

public class CharSelector extends Robot {
    Runnable cb;
    String chr;
    Listbox chrlist;
    Button selbtn;
    
    public CharSelector(TestClient c, String chr, Runnable cb) {
	super(c);
	this.chr = chr;
	this.cb = cb;
    }
    
    public void check() {
	if(chrlist == null)
	    return;
	if(selbtn == null)
	    return;
	
	if(chr == null) {
	    chr = chrlist.opts.get(0).name;
	} else {
	    String nm = null;
	    for(Listbox.Option opt : chrlist.opts) {
		if(opt.disp.equals(chr)) {
		    nm = opt.name;
		    break;
		}
	    }
	    if(nm == null)
		throw(new RobotException(this, "requested character not found: " + chr));
	    chr = nm;
	}
	chrlist.wdgmsg("chose", chr);
	selbtn.wdgmsg("activate");
    }
    
    public void newwdg(int id, Widget w, Object... args) {
	if(w instanceof Listbox) {
	    chrlist = (Listbox)w;
	} else if(w instanceof Button) {
	    if(((Button)w).text.text.equals("I choose you!"))
		selbtn = (Button)w;
	}
	check();
    }
    
    public void dstwdg(int id, Widget w) {
	if(w == chrlist) {
	    destroy();
	    succeed();
	}
    }
    
    public void uimsg(int id, Widget w, String msg, Object... args) {
    }
    
    public void succeed() {
	if(cb != null)
	    cb.run();
    }
}
