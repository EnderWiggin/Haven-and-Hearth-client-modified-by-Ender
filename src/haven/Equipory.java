package haven;

import java.util.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Reader;

public class Equipory extends Window implements DTarget {
	List<Inventory> epoints;
	List<Item> equed;
	List<Layer> layers;
	TexIM bg;
	static List<String> prios = null;
	
	private class Layer {
		String res;
		BufferedImage img;
		int prio;
		
		Layer(String res, BufferedImage img, int prio) {
			this.res = res;
			this.img = img;
			this.prio = prio;
		}
		
		public String toString() {
			return(res);
		}
	}
	
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
	
	private void loadprios() {
		prios = new ArrayList<String>();
		Reader r = Resource.gettext("gfx/hud/equip/prio");
		Scanner s = new Scanner(r);
		try {
			while(true)
				prios.add(s.nextLine());
		} catch(NoSuchElementException e) {}
	}
	
	public void addlayer(String res) {
		String name;
		name = res.substring(res.lastIndexOf('/') + 1);
		name = name.substring(0, name.indexOf('.'));
		int prio = prios.indexOf(name);
		int i;
		for(i = 0; i < layers.size(); i++) {
			if(layers.get(i).prio > prio)
				break;
		}
		layers.add(i, new Layer(res, Resource.loadimg(res), prio));
	}
	
	public Equipory(Coord c, Widget parent) {
		super(c, new Coord(276, 249), parent);
		if(prios == null)
			loadprios();
		epoints = new ArrayList<Inventory>();
		equed = new ArrayList<Item>(ecoords.length);
		layers = new ArrayList<Layer>();
		addlayer("gfx/hud/equip/bg.gif");
		addlayer("gfx/hud/equip/body.gif");
		Coord sz = Utils.imgsz(layers.get(0).img);
		bg = new TexIM(sz);
		renderbg();
		new Img(new Coord(32, 0), bg, this);
		for(int i = 0; i < ecoords.length; i++) {
			epoints.add(new Inventory(ecoords[i], new Coord(1, 1), this));
			equed.add(null);
		}
	}
	
	private void renderbg() {
		Graphics g = bg.graphics();
		for(Layer l : layers)
			g.drawImage(l.img, 0, 0, null);
		bg.update();
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
		} else if(msg == "al") {
			for(Object o : args)
				addlayer((String)o);
		} else if(msg == "sl") {
			layers.clear();
			addlayer("gfx/hud/equip/bg.gif");
			addlayer("gfx/hud/equip/body.gif");
			for(Object o : args)
				addlayer((String)o);
			synchronized(bg) {
				renderbg();
			}
		} else if(msg == "rl") {
			for(Object o : args) {
				String res = (String)o;
				for(Layer l : layers) {
					if(l.res.equals(res)) {
						layers.remove(l);
						break;
					}
				}
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
