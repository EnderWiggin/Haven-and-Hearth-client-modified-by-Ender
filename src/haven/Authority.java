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
}
