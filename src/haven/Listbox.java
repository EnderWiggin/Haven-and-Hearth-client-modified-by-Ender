package haven;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.*;

public class Listbox extends SSWidget {
	static Color bg = new Color(203, 171, 139);
	List<Option> opts;
	int chosen;
	
	static {
		Widget.addtype("lb", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				List<Option> opts = new LinkedList<Option>();
				for(int i = 1; i < args.length; i += 2)
					opts.add(new Option((String)args[i], (String)args[i + 1]));
				return(new Listbox(c, (Coord)args[0], parent, opts));
			}
		});
	}

	static class Option {
		String name, disp;
		int y1, y2;
		
		public Option(String name, String disp) {
			this.name = name;
			this.disp = disp;
		}
	}
	
	void render() {
		Graphics g = graphics();
		g.setColor(bg);
		g.fillRect(0, 0, sz.x, sz.y);
		int y = 0, i = 0;
		for(Option o : opts) {
			if(i++ == chosen)
				g.setColor(FlowerMenu.pink);
			else
				g.setColor(Color.BLACK);
			o.y1 = y;
			y += Utils.drawtext(g, o.disp, new Coord(0, y));
			o.y2 = y;
		}
	}
	
	public Listbox(Coord c, Coord sz, Widget parent, List<Option> opts) {
		super(c, sz, parent, false);
		this.opts = opts;
		chosen = 0;
		setcanfocus(true);
		render();
	}
	
	static List<Option> makelist(Option[] opts) {
		List<Option> ol = new LinkedList<Option>();
		for(Option o : opts)
			ol.add(o);
		return(ol);
	}
	
	public Listbox(Coord c, Coord sz, Widget parent, Option[] opts) {
		this(c, sz, parent, makelist(opts));
	}
	
	public void sendchosen() {
		wdgmsg("chose", opts.get(chosen).name);
	}
	
	public boolean mousedown(Coord c, int button) {
		parent.setfocus(this);
		int i = 0;
		for(Option o : opts) {
			if((c.y >= o.y1) && (c.y <= o.y2))
				break;
			i++;
		}
		if(i < opts.size()) {
			chosen = i;
			sendchosen();
		}
		render();
		return(true);
	}
	
	public boolean keydown(KeyEvent e) { 
		if((e.getKeyCode() == KeyEvent.VK_DOWN) && (chosen < opts.size() - 1)) {
			chosen++;
			sendchosen();
		} else if((e.getKeyCode() == KeyEvent.VK_UP) && (chosen > 0)) {
			chosen--;
			sendchosen();
		}
		render();
		return(true);
	}
}
