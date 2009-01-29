package haven;

import java.util.*;

public class StaticSprite extends ImageSprite {
    private final Object id;
    
    public static final Factory fact = new Factory() {
	    public Sprite create(Owner owner, Resource res, Message sdt) {
		if(res.layer(Resource.animc) != null)
		    return(null);
		return(new StaticSprite(owner, res, sdt));
	    }
	};

    private StaticSprite(Owner owner, Resource res, Message sdt) {
	super(owner, res, sdt);
	Collection<Part> f = new LinkedList<Part>();
	boolean[] flags = decflags(sdt);
	for(Resource.Image img : res.layers(Resource.imgc)) {
	    if((img.id < 0) || ((flags.length > img.id) && flags[img.id]))
		f.add(new ImagePart(img));
	}
	this.curf = f;
	this.id = res;
    }
    
    public Object stateid() {
	return(id);
    }
}
