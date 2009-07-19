package haven;

public class TexSI extends Tex {
    Tex parent;
    Coord ul;
	
    public TexSI(Tex parent, Coord ul, Coord sz) {
	super(sz);
	this.parent = parent;
	this.ul = ul;
    }
	
    public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {
	parent.render(g, c, this.ul.add(ul), this.ul.add(br), sz);
    }
}
