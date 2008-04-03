package haven;

import java.util.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Reader;

public class Equipory extends Window implements DTarget {
	List<Inventory> epoints;
	List<Item> equed;
	static final Tex bg = Resource.loadtex("gfx/hud/equip/bg.gif");
	int avagob = -1;
	
	static Coord ecoords[] = {
		new Coord(0, 0),
		new Coord(244, 0),
		new Coord(0, 31),
		new Coord(244, 31),
		new Coord(0, 62),
		new Coord(244, 62),
		new Coord(0, 93),
		new Coord(244, 93),
		new Coord(0, 124),
		new Coord(244, 124),
		new Coord(0, 155),
		new Coord(244, 155),
		new Coord(0, 186),
		new Coord(244, 186),
		new Coord(0, 217),
		new Coord(244, 217),
	};
	
	static {
		Widget.addtype("epry", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Equipory(c, parent));
			}
		});
	}
	
	public Equipory(Coord c, Widget parent) {
		super(c, new Coord(276, 249), parent);
		epoints = new ArrayList<Inventory>();
		equed = new ArrayList<Item>(ecoords.length);
		new Img(new Coord(32, 0), bg, this);
		for(int i = 0; i < ecoords.length; i++) {
			epoints.add(new Inventory(ecoords[i], new Coord(1, 1), this));
			equed.add(null);
		}
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "set") {
			synchronized(ui) {
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
		} else if(msg == "ava") {
			avagob = (Integer)args[0];
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
	
	public void draw(GOut g) {
		super.draw(g);
		Coord avac = xlate(new Coord(32, 0), true);
		g.image(bg, avac);
		if(avagob != -1) {
			Gob gob = ui.sess.glob.oc.getgob(avagob);
			if(gob != null) {
				Avatar ava = gob.getattr(Avatar.class);
				if(ava != null)
					g.image(ava.tex(), avac);
			}
		}
	}
}
