package haven;

import java.awt.Color;
import java.awt.event.KeyEvent;
import haven.Resource.AButton;
import java.util.*;

public class MenuGrid extends Widget {
	public final static Tex bg = Resource.loadtex("gfx/hud/invsq");
	public final static Coord bgsz = bg.sz().add(-1, -1);
	public final static Resource bk = Resource.load("gfx/hud/sc-back");
	private static Coord gsz = new Coord(4, 4);
	private Resource cur, pressed, layout[][] = new Resource[gsz.x][gsz.y];
	private Map<Character, Resource> hotmap = new TreeMap<Character, Resource>();
	private Resource hover = null;
	
	static {
		Widget.addtype("scm", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new MenuGrid(c, parent));
			}
		});
	}
	
	private Resource[] cons(Resource p) {
		Resource[] cp = new Resource[0];
		Resource[] all;
		{
			Collection<Resource> ta = new HashSet<Resource>();
			Collection<Resource> open = new HashSet<Resource>(ui.sess.glob.paginae);
			while(!open.isEmpty()) {
				for(Resource r : open.toArray(cp)) {
					if(!r.loading) {
						AButton ad = r.layer(Resource.action);
						if((ad.parent != null) && !ta.contains(ad.parent))
							open.add(ad.parent);
						ta.add(r);
						open.remove(r);
					}
				}
			}
			all = ta.toArray(cp);
		}
		Collection<Resource> tobe = new HashSet<Resource>();
		for(Resource r : all) {
			if(r.layer(Resource.action).parent == p)
				tobe.add(r);
		}
		return(tobe.toArray(cp));
	}
	
	public MenuGrid(Coord c, Widget parent) {
		super(c, bgsz.mul(gsz).add(1, 1), parent);
		cons(null);
	}
	
	private void updlayout() {
		Resource[] cur = cons(this.cur);
		Arrays.sort(cur);
		int i = 0;
		hotmap.clear();
		for(int y = 0; y < gsz.y; y++) {
			for(int x = 0; x < gsz.x; x++) {
				Resource btn = null;
				if((this.cur != null) && (x == gsz.x - 1) && (y == gsz.y - 1)) {
					btn = bk;
				} else if(i < cur.length) {
					Resource.AButton ad = cur[i].layer(Resource.action);
					hotmap.put(Character.toUpperCase(ad.hk), cur[i]);
					btn = cur[i++];
				}
				layout[x][y] = btn;
			}
		}
	}
	
	public void draw(GOut g) {
		updlayout();
		for(int y = 0; y < gsz.y; y++) {
			for(int x = 0; x < gsz.x; x++) {
				Coord p = bgsz.mul(new Coord(x, y));
				g.image(bg, p);
				Resource btn = layout[x][y];
				if(btn != null) {
					Tex btex = btn.layer(Resource.imgc).tex();
					g.image(btex, p.add(1, 1));
					if(btn == pressed) {
						g.chcolor(new Color(0, 0, 0, 128));
						g.frect(p.add(1, 1), btex.sz());
						g.chcolor();
					}
				}
			}
		}
		if(pressed == null && hover != null) {
			Resource.AButton ad = hover.layer(Resource.action);
			String tt = ad.name;
			if(ad.hk != 0)
				tt += " [" + ad.hk + "]";
			ui.tooltip = tt;
		}
	}
	
	private Resource bhit(Coord c) {
		Coord bc = c.div(bgsz);
		if((bc.x >= 0) && (bc.y >= 0) && (bc.x < gsz.x) && (bc.y < gsz.y))
			return(layout[bc.x][bc.y]);
		else
			return(null);
	}
	
	private void updhover(Coord c) {
		hover = bhit(c);
		if((hover != null) && (hover.layer(Resource.action) == null))
			hover = null;
	}
	
	public boolean mousedown(Coord c, int button) {
		Resource h = bhit(c);
		if((button == 1) && (h != null)) {
			pressed = h;
			ui.grabmouse(this);
		}
		updhover(c);
		return(true);
	}
	
	public void mousemove(Coord c) {
		updhover(c);
	}
	
	private void use(Resource r) {
		if(cons(r).length > 0) {
			cur = r;
		} else if(r == bk) {
			cur = null;
		} else {
			wdgmsg("act", (Object[])r.layer(Resource.action).ad);
		}
	}
	
	public boolean mouseup(Coord c, int button) {
		Resource h = bhit(c);
		if((pressed != null) && (button == 1)) {
			if(pressed == h)
				use(h);
			ui.grabmouse(null);
			pressed = null;
		}
		updlayout();
		updhover(c);
		return(true);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "goto") {
			String res = (String)args[0];
			if(res.equals(""))
				cur = null;
			else
				cur = Resource.load(res);
		}
	}
	
	public boolean globtype(char k, KeyEvent ev) {
		if((k == 27) && (this.cur != null)) {
			this.cur = null;
			updlayout();
			return(true);
		}
		Resource r = hotmap.get(Character.toUpperCase(k));
		if(r != null) {
			use(r);
			return(true);
		}
		return(false);
	}
}
