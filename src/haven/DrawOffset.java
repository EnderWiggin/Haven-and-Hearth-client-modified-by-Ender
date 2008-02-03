package haven;

public class DrawOffset extends GAttrib {
    Coord off;
    
    public DrawOffset(Gob gob, Coord off) {
	super(gob);
	this.off = off;
    }
}
