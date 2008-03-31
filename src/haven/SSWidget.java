package haven;

import java.awt.Graphics;

public class SSWidget extends Widget {
	private TexIM surf;
	
	public SSWidget(Coord c, Coord sz, Widget parent) {
		super(c, sz, parent);
		surf = new TexIM(sz);
	}
	
	public void draw(GOut g) {
		g.image(surf, Coord.z);
	}
	
	public Graphics graphics() {
		Graphics g = surf.graphics();
		return(g);
	}
	
	public void update() {
		surf.update();
	}
	
	public void clear() {
		surf.clear();
	}
}
