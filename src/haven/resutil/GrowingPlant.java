package haven.resutil;

import haven.*;
import java.util.*;

public class GrowingPlant extends CSprite {
    public static class Factory implements Sprite.Factory {
	public Tex[][] strands;
	public int num;
	public Resource.Neg neg;
	
	public Factory(int stages, int variants, int num, boolean rev) {
	    Resource res = Utils.myres(this.getClass());
	    this.neg = res.layer(Resource.negc);
	    this.num = num;
	    strands = new Tex[stages][variants];
	    if(rev) {
		for(Resource.Image img : res.layers(Resource.imgc)) {
		    if(img.id != -1)
			strands[img.id / variants][img.id % variants] = img.tex();
		}
	    } else {
		for(Resource.Image img : res.layers(Resource.imgc)) {
		    if(img.id != -1)
			strands[img.id % stages][img.id / stages] = img.tex();
		}
	    }
	}
	
	public Factory(int stages, int variants, int num) {
	    this(stages, variants, num, false);
	}
	
	public Sprite create(Owner owner, Resource res, Message sdt) {
	    int m = sdt.uint8();
	    GrowingPlant spr = new GrowingPlant(owner, res);
	    spr.addnegative();
	    Random rnd = owner.mkrandoom();
	    for(int i = 0; i < num; i++) {
		Coord c = new Coord(rnd.nextInt(neg.bs.x), rnd.nextInt(neg.bs.y)).add(neg.bc);
		Tex s = strands[m][rnd.nextInt(strands[m].length)];
		spr.add(s, 0, MapView.m2s(c), new Coord(s.sz().x / 2, s.sz().y).inv());
	    }
	    return(spr);
	}
    }
    
    protected GrowingPlant(Owner owner, Resource res) {
	super(owner, res);
    }
}
