package haven;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class MapView extends Widget {
	public static int barda = 2;
	List<Image> tiles = new LinkedList<Image>();
	Map<Coord, Grid> req = new TreeMap<Coord, Grid>();
	Map<Coord, Grid> grids = new TreeMap<Coord, Grid>();
	Image tree;
	Coord mc;
	public static final Coord tilesz = new Coord(8, 8);
	public static final Coord cmaps = new Coord(100, 100);
	
	static {
		Widget.addtype("mapview", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				System.out.println(args[1]);
				return(new MapView(c, (Coord)args[0], parent, (Coord)args[1]));
			}
		});
	}
	
	private class Grid {
		public int tiles[][];
		public long lastreq = 0;
		
		public Grid() {
			tiles = new int[cmaps.x][cmaps.y];
			for(int y = 0; y < 50; y++) {
				for(int x = 0; x < 50; x++)
					tiles[x][y] = -1;
			}
		}
		
		public int gettile(Coord tc) {
			return(tiles[tc.x][tc.y]);
		}
	}
	
	public MapView(Coord c, Coord sz, Widget parent, Coord mc) {
		super(c, sz, parent);
		for(int i = 0; i <= 13; i++) {
			tiles.add(Resource.loadimg(String.format("tiles/dirt-%02d.gif", i)));
		}
		tree = Resource.loadimg("trees/tree3.gif");
		this.mc = mc;
		Session.current.mapdispatch = this;
	}
	
	static Coord m2s(Coord c) {
		return(new Coord((c.x * 2) - (c.y * 2), c.x + c.y));
	}
	
	static Coord s2m(Coord c) {
		return(new Coord((c.x / 4) + (c.y / 2), (c.y / 2) - (c.x / 4)));
	}
	
	static Coord viewoffset(Coord sz, Coord vc) {
		return(m2s(vc).inv().add(new Coord(sz.x / 2, sz.y / 2)));
	}
	
	private Image gettile(Coord tc) {
		Grid g;
		synchronized(grids) {
			g = grids.get(tc.div(cmaps));
		}
		if(g == null)
			return(null);
		return(tiles.get(g.gettile(tc.mod(cmaps))));
	}
	
	public boolean mousedown(Coord c, int button) {
		Coord mc = s2m(c.add(viewoffset(sz, this.mc).inv()));
		ui.wdgmsg(this, "click", mc, button);
		return(true);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "move") {
			mc = (Coord)args[0];
		}
	}
	
	public boolean drawmap(Graphics g) {
		int x, y, i;
		int stw, sth;
		Coord oc, tc, ctc, sc;
		
		stw = (tilesz.x * 4) - 2;
		sth = tilesz.y * 2;
		oc = viewoffset(sz, mc);
		tc = mc.div(tilesz);
		tc.x += -(sz.x / (2 * stw)) - (sz.y / (2 * sth)) - 2;
		tc.y += (sz.x / (2 * stw)) - (sz.y / (2 * sth));
		for(y = 0; y < (sz.y / sth) + 2; y++) {
			for(x = 0; x < (sz.x / stw) + 3; x++) {
				for(i = 0; i < 2; i++) {
					ctc = tc.add(new Coord(x + y, -x + y + i));
					sc = m2s(ctc.mul(tilesz)).add(oc);
					sc.x -= (tilesz.x - 1) * 2;
					Image tile = gettile(ctc);
					if(tile == null)
						return(false);
					g.drawImage(tile, sc.x, sc.y, null);
				}
			}
		}
		
		ArrayList<Drawable> sprites = new ArrayList<Drawable>();
		synchronized(Session.current.oc.objs) {
			for(Drawable d : Session.current.oc.objs.values()) {
				Coord dc = m2s(d.c).add(oc);
				d.sc = dc;
				Coord ulc = dc.add(d.getoffset().inv());
				Coord lrc = ulc.add(d.getsize());
				if((lrc.x > 0) && (lrc.y > 0) && (ulc.x <= sz.x) && (ulc.y <= sz.y))
					sprites.add(d);
			}
		}
		Collections.sort(sprites, new Comparator<Drawable>() {
			public int compare(Drawable a, Drawable b) {
				return(a.c.y - b.c.y);
			}
		});
		for(Drawable d : sprites) {
			d.draw(g, d.sc);
		}
		return(true);
	}
	
	public void mapdata(Message msg) {
		Coord c = msg.coord();
		synchronized(req) {
			synchronized(grids) {
				if(req.containsKey(c)) {
					Grid g = req.get(c);
					for(int y = 0; y < cmaps.y; y++) {
						for(int x = 0; x < cmaps.x; x++)
							g.tiles[x][y] = msg.uint8();
					}
					req.remove(c);
					grids.put(c, g);
				}
			}
		}
	}
	
	public void draw(Graphics g) {
		Coord gc = mc.div(tilesz).div(cmaps);
		for(int y = -1; y <= 1; y++) {
			for(int x = -1; x <= 1; x++) {
				Coord cgc = gc.add(new Coord(x, y));
				if((grids.get(cgc) == null) && (req.get(cgc) == null))
					req.put(cgc, new Grid());
			}
		}
		long now = System.currentTimeMillis();
		synchronized(req) {
			for(Map.Entry<Coord, Grid> e : req.entrySet()) {
				Coord c = e.getKey();
				Grid gr = e.getValue();
				if(now - gr.lastreq > 1000) {
					gr.lastreq = now;
					Message msg = new Message(Session.MSG_MAPREQ);
					msg.addcoord(c);
					Session.current.sendmsg(msg);
				}
			}
		}
		if(!drawmap(g)) {
			String text = "Loading...";
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, sz.x, sz.y);
			g.setColor(Color.WHITE);
			FontMetrics m = g.getFontMetrics();
			Rectangle2D b = m.getStringBounds(text, g);
			g.drawString(text, sz.x / 2 - (int)b.getWidth() / 2, sz.y / 2 - m.getAscent());
		}
	}
}