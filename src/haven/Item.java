package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Color;

public class Item extends Widget implements DTarget {
	static Coord shoff = new Coord(1, 3);
	static Resource missing = Resource.load("gfx/invobjs/missing");
	boolean dm = false;
	Coord doff;
	int num = -1;
	Indir<Resource> res;
	Tex sh;
	
	static {
		Widget.addtype("item", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				int res = (Integer)args[0];
				int num = -1;
				int ca = 2;
				Coord drag = null;
				if((Integer)args[1] != 0)
					drag = (Coord)args[ca++];
				if(args.length > ca)
					num = (Integer)args[ca++];
				return(new Item(c, res, parent, drag, num));
			}
		});
		missing.loadwait();
	}
	
	private void fixsize() {
		if(res.get() != null) {
			Tex tex = res.get().layer(Resource.imgc).tex();
			sz = tex.sz().add(shoff);
		} else {
			sz = new Coord(30, 30);
		}
	}

	public void draw(GOut g) {
		if(res.get() == null) {
			sh = null;
			sz = new Coord(30, 30);
			g.image(missing.layer(Resource.imgc).tex(), Coord.z, sz);
		} else {
			Tex tex = res.get().layer(Resource.imgc).tex();
			fixsize();
			if(dm) {
				if(sh == null)
					sh = makesh(res.get());
				g.image(sh, shoff);
			}
			g.image(tex, Coord.z);
			if(num >= 0) {
				g.chcolor(Color.BLACK);
				g.atext(Integer.toString(num), tex.sz(), 1, 1);
			}
		}
	}

	static Tex makesh(Resource res) {
		BufferedImage img = res.layer(Resource.imgc).img;
		Coord sz = Utils.imgsz(img);
		BufferedImage sh = new BufferedImage(sz.x, sz.y, BufferedImage.TYPE_INT_ARGB);
		for(int y = 0; y < sz.y; y++) {
			for(int x = 0; x < sz.x; x++) {
				long c = img.getRGB(x, y) & 0x00000000ffffffffL;
				int a = (int)((c & 0xff000000) >> 24);
				sh.setRGB(x, y, (a / 2) << 24);
				System.out.println(String.format("%08x %x %08x", c, a, (a / 2) << 24));
			}
		}
		return(new TexI(sh));
	}
	
	public Item(Coord c, Indir<Resource> res, Widget parent, Coord drag, int num) {
		super(c, Coord.z, parent);
		this.res = res;
		fixsize();
		this.num = num;
		if(drag == null) {
			dm = false;
		} else {
			dm = true;
			doff = drag;
			ui.grabmouse(this);
			this.c = ui.mc.add(doff.inv());
		}
	}

	public Item(Coord c, int res, Widget parent, Coord drag, int num) {
		this(c, parent.ui.sess.getres(res), parent, drag, num);
	}

	public Item(Coord c, Indir<Resource> res, Widget parent, Coord drag) {
		this(c, res, parent, drag, -1);
	}
	
	public Item(Coord c, int res, Widget parent, Coord drag) {
		this(c, parent.ui.sess.getres(res), parent, drag);
	}

	public boolean dropon(Widget w, Coord c) {
		for(Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
			if(wdg == this)
				continue;
			Coord cc = w.xlate(wdg.c, true);
			if(c.isect(cc, wdg.sz)) {
				if(dropon(wdg, c.add(cc.inv())))
					return(true);
			}
		}
		if(w instanceof DTarget) {
			if(((DTarget)w).drop(c, c.add(doff.inv())))
				return(true);
		}
		return(false);
	}
	
	public boolean interact(Widget w, Coord c) {
		for(Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
			if(wdg == this)
				continue;
			Coord cc = w.xlate(wdg.c, true);
			if(c.isect(cc, wdg.sz)) {
				if(interact(wdg, c.add(cc.inv())))
					return(true);
			}
		}
		if(w instanceof DTarget) {
			if(((DTarget)w).iteminteract(c, c.add(doff.inv())))
				return(true);
		}
		return(false);
	}
	
	public void uimsg(String name, Object... args)  {
		if(name == "num") {
			num = (Integer)args[0];
		} else if(name == "chres") {
			res = ui.sess.getres((Integer)args[0]);
			sh = null;
		}
	}
	
	public boolean mousedown(Coord c, int button) {
		if(!dm) {
			if(button == 1) {
				if(ui.modshift)
					wdgmsg("transfer", c);
				else
					wdgmsg("take", c);
				return(true);
			} else if(button == 3) {
				wdgmsg("iact", c);
				return(true);
			}
		} else {
			if(button == 1) {
				dropon(parent, c.add(this.c));
			} else if(button == 3) {
				interact(parent, c.add(this.c));
			}
			return(true);
		}
		return(false);
	}
	
	public void mousemove(Coord c) {
		if(dm)
			this.c = this.c.add(c.add(doff.inv()));
	}
	
	public boolean drop(Coord cc, Coord ul) {
		return(false);
	}
	
	public boolean iteminteract(Coord cc, Coord ul) {
		wdgmsg("itemact");
		return(true);
	}
}
