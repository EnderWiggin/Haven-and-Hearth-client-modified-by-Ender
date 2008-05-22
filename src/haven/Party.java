package haven;

import java.util.*;

public class Party {
	Map<Integer, Member> memb = new TreeMap<Integer, Member>();
	Member leader = null;
	public static final int PD_LIST = 0;
	public static final int PD_LEADER = 1;
	public static final int PD_MEMBER = 2;
	
	public class Member {
		int gobid;
		Coord c;
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
				for(int id : ids) {
					Member m = new Member();
					m.gobid = id;
					memb.put(id, m);
				}
				this.memb = memb;
			} else if(type == PD_LEADER) {
				leader = memb.get(msg.int32());
			} else if(type == PD_MEMBER) {
				Member m = memb.get(msg.int32());
				m.c = msg.coord();
			}
		}
	}
}
