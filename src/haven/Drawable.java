package haven;

public abstract class Drawable extends GAttrib {
	public Drawable(Gob gob) {
		super(gob);
	}
	
	public abstract Coord getsize();
	public abstract Coord getoffset();
	public abstract boolean checkhit(Coord c);
	public abstract void setup(Sprite.Drawer d, Coord cc, Coord off);
}
