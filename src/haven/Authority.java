package haven;

public class Authority extends GAttrib {
    int er, ir;
    boolean my;
	
    public Authority(Gob g, int er, int ir, boolean my) {
	super(g);
	this.er = er;
	this.ir = ir;
	this.my = my;
    }
    
    private static Coord ssz(int r) {
	return(new Coord((int)(r * 4 * Math.sqrt(0.5)), (int)(r * 2 * Math.sqrt(0.5))));
    }

    public Sprite.Part mkpart() {
	return(new Sprite.Part(-15) {
		public void draw(java.awt.image.BufferedImage buf, java.awt.Graphics g) {}
		
		public void draw(GOut g) {
		    g.chcolor(my?0:255, 0, my?255:0, 32);
		    g.fellipse(gob.sc, ssz(er));
		    g.fellipse(gob.sc, ssz(ir));
		    g.chcolor();
		}
		
		public void setup(Coord cc, Coord off) {
		    super.setup(cc, off);
		    ul = cc.add(ssz(er).inv());
		    lr = cc.add(ssz(er));
		}
	    });
    }
}
