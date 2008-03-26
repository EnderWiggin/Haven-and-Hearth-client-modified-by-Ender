package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class IBox {
	Tex ctl, ctr, cbl, cbr;
	Tex bl, br, bt, bb;
	
	public IBox(Tex ctl, Tex ctr, Tex cbl, Tex cbr, Tex bl, Tex br, Tex bt, Tex bb) {
		this.ctl = ctl;
		this.ctr = ctr;
		this.cbl = cbl;
		this.cbr = cbr;
		this.bl = bl;
		this.br = br;
		this.bt = bt;
		this.bb = bb;
	}
	
	public Coord tloff() {
		return(new Coord(bl.sz().x, bt.sz().y));
	}
	
	public Coord ctloff() {
		return(ctl.sz());
	}
	
	public Coord bsz() {
		return(ctl.sz().add(cbr.sz()));
	}
	
	public void draw(GOut g, Coord tl, Coord sz) {
		for(int x = ctl.sz().x; x < sz.x - ctr.sz().x; x++)
			g.image(bt, tl.add(x, 0));
		for(int x = cbl.sz().x; x < sz.x - cbr.sz().x; x++)
			g.image(bb, new Coord(x + tl.x, sz.y - bb.sz().y + tl.y));
		for(int y = ctl.sz().y; y < sz.y - cbl.sz().y; y++)
			g.image(bl, tl.add(0, y));
		for(int y = ctr.sz().y; y < sz.y - cbr.sz().y; y++)
			g.image(br, new Coord(sz.x - br.sz().x + tl.x, y + tl.y));
		g.image(ctl, tl);
		g.image(ctr, sz.add(tl.x - ctr.sz().x, 0));
		g.image(cbl, tl.add(0, sz.y - cbl.sz().y));
		g.image(cbr, new Coord(sz.x - cbr.sz().x + tl.x, sz.y - cbr.sz().y + tl.y));
	}
}
