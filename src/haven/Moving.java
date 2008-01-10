package haven;

public abstract class Moving extends GAttrib {
	public Moving(Gob gob) {
		super(gob);
	}
	
	public abstract Coord getc();
}
