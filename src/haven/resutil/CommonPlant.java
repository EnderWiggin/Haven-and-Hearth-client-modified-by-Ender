package haven.resutil;

import haven.*;
import java.util.*;

public class CommonPlant extends CSprite {
    public static class Factory implements Sprite.Factory {
	private static final Tex[] typebarda = new Tex[0];
	public Tex[] strands;
	public int num;
	public Resource.Neg neg;
	
	public Factory(int num) {
	    Resource res = Utils.myres(this.getClass());
	    this.neg = res.layer(Resource.negc);
	    this.num = num;
	    ArrayList<Tex> strands = new ArrayList<Tex>();
	    for(Resource.Image img : res.layers(Resource.imgc)) {
		if(img.id != -1)
		    strands.add(img.tex());
	    }
	    this.strands = strands.toArray(typebarda);
	}
	
	public Sprite create(Owner owner, Resource res, Message sdt) {
	    CommonPlant spr = new CommonPlant(owner, res);
	    spr.addnegative();
	    Random rnd = owner.mkrandoom();
	    for(int i = 0; i < num; i++) {
		Coord c = new Coord(rnd.nextInt(neg.bs.x), rnd.nextInt(neg.bs.y)).add(neg.bc);
		Tex s = strands[rnd.nextInt(strands.length)];
		spr.add(s, 0, MapView.m2s(c), new Coord(s.sz().x / 2, s.sz().y).inv());
	    }
	    return(spr);
	}
    }
    
    protected CommonPlant(Owner owner, Resource res) {
	super(owner, res);
    }
}
