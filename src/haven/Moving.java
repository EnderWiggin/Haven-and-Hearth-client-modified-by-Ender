package haven;

public abstract class Moving extends GAttrib {
    public Moving(Gob gob) {
	super(gob);
    }
	
    public void move(Coord c) {}
	
    public abstract Coord getc();
}
