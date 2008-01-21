package haven;

import java.util.*;

public class Equipory extends Window implements DTarget {
	List<Inventory> epoints;
	List<Item> equed;
	static Coord ecoords[] = {
		new Coord(150, 10),
		new Coord(150, 40),
		new Coord(120, 40),
		new Coord(180, 40),
		new Coord(150, 70),
		new Coord(150, 100)
	};
	
	static {
		Widget.addtype("epry", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Equipory(c, parent));
			}
		});
	}
	
	public Equipory(Coord c, Widget parent) {
		super(c, new Coord(300, 200), parent);
		epoints = new ArrayList<Inventory>();
		equed = new ArrayList<Item>(ecoords.length);
		for(int i = 0; i < ecoords.length; i++) {
			epoints.add(new Inventory(ecoords[i], new Coord(1, 1), this));
			equed.add(null);
		}
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "set") {
			for(int i = 0; i < equed.size(); i++) {
				if(equed.get(i) != null)
					equed.get(i).unlink();
				String res = (String)args[i];
				if(!res.equals(""))
					equed.set(i, new Item(Coord.z, res, epoints.get(i), null));
				else
					equed.set(i, null);
			}
		}
	}
	
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == this) {
			super.wdgmsg(sender, msg, args);
			return;
		}
		int ep;
		if((ep = epoints.indexOf(sender)) != -1) {
			if(msg == "drop")
				wdgmsg("drop", ep);
		}
		if((ep = equed.indexOf(sender)) != -1) {
			if(msg == "take")
				wdgmsg("take", ep, args[0]);
		}
	}
	
	public void drop(Coord cc, Coord ul) {
		wdgmsg("drop", -1);
	}
}
