package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.*;

public abstract class ImageSprite extends Sprite {
    public Coord cc;
    public Collection<Part> curf = null;
    
    public class ImagePart extends Part {
	Resource.Image img;
	
	public ImagePart(Resource.Image img) {
	    super(img.z, img.subz);
	    this.img = img;
	}
	
	public void draw(BufferedImage b, Graphics g) {
	    Coord sc = sc().add(img.o);
	    if(img.gayp()) {
		Utils.drawgay(b, img.img, sc);
	    } else {
		g.drawImage(img.img, sc.x, sc.y, null);
	    }
	}
	
	public void draw(GOut g) {
	    g.image(img.tex(), sc().add(img.o));
	}
	
	public Coord sc() {
	    if(img.nooff)
		return(cc.add(ImageSprite.this.cc.inv()));
	    else
		return(cc.add(ImageSprite.this.cc.inv()).add(off));
	}

	public void setup(Coord cc, Coord off) {
	    super.setup(cc, off);
	    ul = sc().add(img.o);
	    lr = ul.add(img.sz);
	}
	
	public boolean checkhit(Coord c) {
	    if((c.x < img.o.x) || (c.y < img.o.y) || (c.x >= img.o.x + img.sz.x) || (c.y >= img.o.y + img.sz.y))
		return(false);
	    int cl = img.img.getRGB(c.x - img.o.x, c.y - img.o.y);
	    return(Utils.rgbm.getAlpha(cl) >= 128);
	}
    }
    
    public static boolean[] decflags(Message sdt) {
	if(sdt == null)
	    return(new boolean[0]);
	boolean[] ret = new boolean[sdt.blob.length * 8];
	int i = 0;
	while(!sdt.eom()) {
	    int b = sdt.uint8();
	    for(int o = 0; o < 8; o++, i++)
		ret[i] = (b & (1 << o)) != 0;
	}
	return(ret);
    }

    protected ImageSprite(Owner owner, Resource res, Message sdt) {
	super(owner, res);
	Resource.Neg neg = res.layer(Resource.negc);
	if(neg == null)
	    throw(new ResourceException("No negative found", res));
	this.cc = neg.cc;
    }
    
    public boolean checkhit(Coord c) {
	Collection<Part> f = this.curf;
	c = c.add(cc);
	synchronized(f) {
	    for(Part p : f) {
		if(p.checkhit(c))
		    return(true);
	    }
	}
	return(false);
    }

    public void setup(Drawer d, Coord cc, Coord off) {
	Collection<Part> f = this.curf;
	synchronized(f) {
	    setup(f, d, cc, off);
	}
    }
    
    public Object stateid() {
	return(curf);
    }
}
