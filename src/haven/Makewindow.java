package haven;

import java.util.*;
import java.awt.image.BufferedImage;

public class Makewindow extends Window {
	Widget list, btn;
	String cr;
	List<Widget> inputs;
	List<Widget> outputs;
	static Coord boff = new Coord(7, 9);

	static {
		Widget.addtype("make", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				final List<Listbox.Option> opts = new LinkedList<Listbox.Option>();
				for(int i = 0; i < args.length; i += 2)
					opts.add(new Listbox.Option((String)args[i], (String)args[i + 1]));
				return(new Makewindow(c, parent, opts));
			}
		});
	}
	
	public Makewindow(Coord c, Widget parent, List<Listbox.Option> opts) {
		super(c, new Coord(0, 0), parent, "Crafting", new Coord(0, 0), boff);
		cr = opts.get(0).name;
		BufferedImage bup = Resource.loadimg("gfx/hud/btn-mk-up");
		new Img(Coord.z, Resource.loadtex("gfx/hud/mkbg"), this);
		list = new Listbox(new Coord(0, 0), new Coord(100, 200), this, opts);
		pack();
		btn = new IButton(asz.add(new Coord(5, 5)).add(Utils.imgsz(bup).inv()).add(boff), this, bup, Resource.loadimg("gfx/hud/btn-mk-dn"));
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "pop") {
			if(inputs != null) {
				for(Widget w : inputs)
					w.unlink();
				for(Widget w : outputs)
					w.unlink();
			}
			inputs = new LinkedList<Widget>();
			outputs = new LinkedList<Widget>();
			int i;
			Coord c = new Coord(110, 20);
			for(i = 0; args[i] instanceof String; i += 2) {
				Widget box = new Inventory(c, new Coord(1, 1), this);
				inputs.add(box);
				c = c.add(new Coord(31, 0));
				new Item(Coord.z, (String)args[i], box, null, (Integer)args[i + 1]);
			}
			c = new Coord(110, 80);
			for(i++; (i < args.length) && (args[i] instanceof String); i += 2) {
				Widget box = new Inventory(c, new Coord(1, 1), this);
				outputs.add(box);
				c = c.add(new Coord(31, 0));
				new Item(Coord.z, (String)args[i], box, null, (Integer)args[i + 1]);
			}
		}
	}
	
	public void draw(GOut g) {
		super.draw(g);
		g.text("Ingredients:", xlate(new Coord(110, 0), true));
		g.text("Products:", xlate(new Coord(110, 60), true));
	}
	
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == btn) {
			if(msg == "activate") {
				wdgmsg("make", cr);
				return;
			}
		} else if(sender == list) {
			if(msg == "chose") {
				cr = (String)args[0];
				wdgmsg(msg, cr);
				return;
			}
		}
		super.wdgmsg(sender, msg, args);
	}
}
