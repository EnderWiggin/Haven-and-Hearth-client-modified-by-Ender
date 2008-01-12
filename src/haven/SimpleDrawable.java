package haven;

public abstract class SimpleDrawable extends Drawable {
	String res;
	
	public SimpleDrawable(Gob gob, String res) {
		super(gob);
		this.res = res;
	}
}
