package haven;

public class Lumin extends GAttrib {
	Coord off;
	int sz, str;
	
	public Lumin(Gob g, Coord off, int sz, int str) {
		super(g);
		this.off = off;
		this.sz = sz;
		this.str = str;
	}
}
