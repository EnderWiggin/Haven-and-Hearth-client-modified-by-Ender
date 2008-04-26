package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.util.*;

public class Skillwindow extends Window {
	Label exptext;
	int skx = 0;
	Coord sksz = new Coord(24, 24);
	List<Img> psk = new LinkedList<Img>();
	List<Img> nsk = new LinkedList<Img>();
	Map<Img, String> skillz = new HashMap<Img, String>();
	Img sel = null;
	IButton btn;
	
	static {
		Widget.addtype("skill", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Skillwindow(c, parent, (Integer)args[0]));
			}
		});
	}
	
	public Skillwindow(Coord c, Widget parent, int exp) {
		super(c, new Coord(300, 200), parent);
		exptext = new Label(new Coord(0, 184), this, "Learning points: " + exp);
		new Label(new Coord(0, 0), this, "Skills you have:");
		new Label(new Coord(0, 100), this, "Skills available to you:");
		btn = new IButton(new Coord(200, 170), this, Resource.loadimg("gfx/hud/buttons/learnu"), Resource.loadimg("gfx/hud/buttons/learnd"));
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "exp") {
			exptext.settext("Learning points: " + (Integer)args[0]);
		} else if(msg == "psk") {
			for(Img img : psk)
				img.unlink();
			psk = new LinkedList<Img>();
			Coord cp = new Coord(5, 16);
			for(Object resnmcp : args) {
				String resnm = (String)resnmcp;
				BufferedImage oski = Resource.loadimg("gfx/hud/skills/" + resnm);
				BufferedImage ski = TexI.mkbuf(sksz);
				Graphics g = ski.getGraphics();
				g.drawImage(oski, 0, 0, sksz.x, sksz.y, null);
				psk.add(new Img(new Coord(cp), new TexI(ski), this));
				if((cp.x += sksz.x + 2) > sz.x - sksz.x) {
					cp.y += sksz.y + 2;
					cp.x = 5;
				}
			}
		} else if(msg == "nsk") {
			sel = null;
			for(Img img : nsk)
				img.unlink();
			nsk = new LinkedList<Img>();
			skillz = new HashMap<Img, String>();
			Coord cp = new Coord(5, 116);
			for(Object resnmcp : args) {
				String resnm = (String)resnmcp;
				BufferedImage oski = Resource.loadimg("gfx/hud/skills/" + resnm);
				BufferedImage ski = TexI.mkbuf(sksz);
				Graphics g = ski.getGraphics();
				g.drawImage(oski, 0, 0, sksz.x, sksz.y, null);
				Img key;
				nsk.add(key = new Img(new Coord(cp), new TexI(ski), this) {
					public boolean mousedown(Coord c, int button) {
						if(button == 1)
							sel = this;
						return(true);
					}
				});
				skillz.put(key, resnm);
				if((cp.x += sksz.x + 2) > sz.x - sksz.x) {
					cp.y += sksz.y + 2;
					cp.x = 5;
				}
			}
		}
	}
	
	public void cdraw(GOut g) {
		super.cdraw(g);
		if(sel != null) {
			g.chcolor(java.awt.Color.YELLOW);
			g.rect(sel.c.add(new Coord(-1, -1)), sel.sz.add(new Coord(2, 2)));
		}
	}
	
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == this) {
			super.wdgmsg(this, msg, args);
			return;
		}
		if(sender == btn) {
			if(sel != null)
				super.wdgmsg("buy", skillz.get(sel));
		}
	}
}
