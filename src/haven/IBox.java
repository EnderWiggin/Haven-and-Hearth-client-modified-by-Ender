package haven;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class IBox {
	BufferedImage ctl, ctr, cbl, cbr;
	BufferedImage bl, br, bt, bb;
	
	public IBox(BufferedImage ctl, BufferedImage ctr, BufferedImage cbl, BufferedImage cbr, BufferedImage bl, BufferedImage br, BufferedImage bt, BufferedImage bb) {
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
		return(new Coord(bl.getWidth(), bt.getHeight()));
	}
	
	public Coord ctloff() {
		return(Utils.imgsz(ctl));
	}
	
	public Coord bsz() {
		return(Utils.imgsz(ctl).add(Utils.imgsz(cbr)));
	}
	
	public void draw(Graphics g, Coord tl, Coord sz) {
		for(int x = ctl.getWidth(); x < sz.x - ctr.getWidth(); x++)
			g.drawImage(bt, x + tl.x, tl.y, null);
		for(int x = cbl.getWidth(); x < sz.x - cbr.getWidth(); x++)
			g.drawImage(bb, x + tl.x, sz.y - bb.getHeight() + tl.y, null);
		for(int y = ctl.getHeight(); y < sz.y - cbl.getHeight(); y++)
			g.drawImage(bl, tl.x, y + tl.y, null);
		for(int y = ctr.getHeight(); y < sz.y - cbr.getHeight(); y++)
			g.drawImage(br, sz.x - br.getWidth() + tl.x, y + tl.y, null);
		g.drawImage(ctl, tl.x, tl.y, null);
		g.drawImage(ctr, sz.x - ctr.getWidth() + tl.x, tl.y, null);
		g.drawImage(cbl, tl.x, sz.y - cbl.getHeight() + tl.y, null);
		g.drawImage(cbr, sz.x - cbr.getWidth() + tl.x, sz.y - cbr.getHeight() + tl.y, null);
	}
}
