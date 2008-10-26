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
		private Coord c;
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
