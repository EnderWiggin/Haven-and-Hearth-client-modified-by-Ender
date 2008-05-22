package haven;

import haven.Party.Member;

import java.util.*;
import java.util.Map.Entry;

public class Partyview extends Widget {
	int ign;
	Party party = ui.sess.glob.party;
	Map<Integer, Party.Member> om = null;
	Party.Member ol = null;
	Map<Party.Member, Avaview> avs = new HashMap<Party.Member, Avaview>();
	private static final Map.Entry<?, ?>[] cp = new Map.Entry[0];
	
	static {
		Widget.addtype("pv", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Partyview(c, parent, (Integer)args[0]));
			}
		});
	}
	
	Partyview(Coord c, Widget parent, int ign) {
		super(c, new Coord(84, 140), parent);
		this.ign = ign;
		update();
	}
	
	private void update() {
		if(party.memb != om) {
			Collection<Party.Member> old = new HashSet<Party.Member>(avs.keySet());
			for(Party.Member m : (om = party.memb).values()) {
				if(m.gobid == ign)
					continue;
				Avaview w = avs.get(m);
				if(w == null) {
					w = new Avaview(Coord.z, this, m.gobid, new Coord(27, 27));
					avs.put(m, w);
				} else {
					old.remove(w);
					w.marked = false;
				}
			}
			for(Party.Member m : old) {
				ui.destroy(avs.get(m));
				avs.remove(m);
			}
			Map.Entry<Party.Member, Widget>[] wl = (Map.Entry<Party.Member, Widget>[])avs.entrySet().toArray(cp);
			Arrays.sort(wl, new Comparator<Map.Entry<Party.Member, Widget>>() {
				public int compare(Entry<Member, Widget> a, Entry<Member, Widget> b) {
					return(a.getKey().gobid - b.getKey().gobid);
				}
			});
			int i = 0;
			for(Map.Entry<Party.Member, Widget> e : wl) {
				e.getValue().c = new Coord((i % 2) * 43, (i / 2) * 43);
				i++;
			}
		}
		if(party.leader != ol) {
			for(Avaview w : avs.values())
				w.marked = false;
			Avaview w = avs.get(party.leader);
			if(w != null)
				w.marked = true;
		}
	}
	
	public void draw(GOut g) {
		update();
		super.draw(g);
	}
}
