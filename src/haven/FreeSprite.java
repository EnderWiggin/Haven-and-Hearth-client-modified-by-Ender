package haven;

import java.util.*;

public abstract class FreeSprite extends Sprite {
    /*
    public Coord cc = Coord.z;
    public Coord sz = Coord.z;
    */
    private final Collection<Part> layers = new LinkedList<Part>();
    
    public interface Layer {
	public void draw(GOut g, Coord sc);
    }
    
    private class LPart extends Part {
	Layer lay;
	
	public LPart(Layer lay, int z, int subz) {
	    super(z, subz);
	    this.lay = lay;
	}
	
	public void draw(GOut g) {
	    lay.draw(g, sc());
	}
	
	public void draw(java.awt.image.BufferedImage img, java.awt.Graphics g) {
	}
    }

    protected FreeSprite(Owner owner, Resource res, int z, int subz) {
	super(owner, res);
	add(new Layer() {
		public void draw(GOut g, Coord sc) {
		    FreeSprite.this.draw(g, sc);
		}
	    }, z, subz);
    }
    
    protected FreeSprite(Owner owner, Resource res) {
	this(owner, res, 0, 0);
    }
    
    public void add(Layer lay, int z, int subz) {
	layers.add(new LPart(lay, z, subz));
    }
    
    public boolean checkhit(Coord c) {
	return(false);
    }

    public void setup(Drawer d, Coord cc, Coord off) {
	setup(layers, d, cc, off);
    }

    public Object stateid() {
	return(this);
    }
    
    public abstract void draw(GOut g, Coord sc);
}
