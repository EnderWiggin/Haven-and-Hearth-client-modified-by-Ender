package haven;

import java.awt.Graphics;

public class SimpleSprite {
    public final Resource.Image img;
    public final Coord cc;
    
    public SimpleSprite(Resource.Image img, Coord cc) {
	this.img = img;
	this.cc = cc;
    }

    public SimpleSprite(Resource res, int id, Coord cc) {
	find: {
	    for(Resource.Image img : res.layers(Resource.imgc)) {
		if(img.id == id) {
		    this.img = img;
		    break find;
		}
	    }
	    throw(new RuntimeException("Could not find image with id " + id + " in resource " + res.name));
	}
	this.cc = cc;
    }

    public SimpleSprite(Resource res, int id) {
	this(res, id, res.layer(Resource.negc).cc);
    }
    
    public SimpleSprite(Resource res) {
	this(res, -1);
    }
    
    public final void draw(GOut g, Coord cc) {
	g.image(img.tex(), cc.add(ul()));
    }

    public final void draw(Graphics g, Coord cc) {
	Coord c = cc.add(ul());
	g.drawImage(img.img, c.x, c.y, null);
    }
    
    public final Coord ul() {
	return(cc.inv().add(img.o));
    }
    
    public final Coord lr() {
	return(ul().add(img.sz));
    }

    public boolean checkhit(Coord c) {
	c = c.add(ul().inv());
	if((c.x < 0) || (c.y < 0) || (c.x >= img.sz.x) || (c.y >= img.sz.y))
	    return(false);
	int cl = img.img.getRGB(c.x, c.y);
	return(Utils.rgbm.getAlpha(cl) >= 128);
    }
}
