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

import java.util.*;
import java.awt.Color;

public class Party {
    Map<Integer, Member> memb = new TreeMap<Integer, Member>();
    Member leader = null;
    public static final int PD_LIST = 0;
    public static final int PD_LEADER = 1;
    public static final int PD_MEMBER = 2;
    public static final Color[] pc = new Color[] {
	new Color(255, 0, 0),
	new Color(0, 0, 255),
	new Color(0, 255, 0),
	new Color(255, 255, 0),
	new Color(255, 0, 128)
    };
    public static final Color dc = Color.BLACK;
    private Glob glob;
	
    public Party(Glob glob) {
	this.glob = glob;
    }
	
    public class Member {
	int gobid;
	private Coord c = Coord.z;
	Color col;
		
	public Coord getc() {
	    Gob gob;
	    if((gob = glob.oc.getgob(gobid)) != null)
		return(gob.getc());
	    return(c);
	}
    }
	
    public static Color getcolor(int i) {
	if(i < pc.length)
	    return(pc[i]);
	return(dc);
    }
	
    public Member bycol(Color c) {
	for(Member m : memb.values()) {
	    if(m.col == c)
		return(m);
	}
	return(null);
    }
	
    private void setlc() {
	if((leader != null) && (leader.col != pc[0])) {
	    Member other = bycol(pc[0]);
	    other.col = leader.col;
	    leader.col = pc[0];
	}
    }
	
    public void msg(Message msg) {
	while(!msg.eom()) {
	    int type = msg.uint8();
	    if(type == PD_LIST) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		while(true) {
		    int id = msg.int32();
		    if(id < 0)
			break;
		    ids.add(id);
		}
		Map<Integer, Member> memb = new TreeMap<Integer, Member>();
		int i = 0;
		for(int id : ids) {
		    Member m = new Member();
		    m.gobid = id;
		    m.col = getcolor(i++);
		    memb.put(id, m);
		}
		int lid = (leader == null)?-1:leader.gobid;
		this.memb = memb;
		leader = memb.get(lid);
		setlc();
	    } else if(type == PD_LEADER) {
		Member m = memb.get(msg.int32());
		if(m != null) {
		    leader = m;
		    setlc();
		}
	    } else if(type == PD_MEMBER) {
		Member m = memb.get(msg.int32());
		Coord c = msg.coord();
		if(m != null)
		    m.c = c;
	    }
	}
    }
}
