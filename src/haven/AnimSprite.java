package haven;

import java.util.*;

public class AnimSprite extends ImageSprite {
    private Frame[] frames;
    private int fno, te;
    public static final Factory fact = new Factory() {
	    public Sprite create(Owner owner, Resource res, Message sdt) {
		if(res.layer(Resource.animc) == null)
		    return(null);
		return(new AnimSprite(owner, res, sdt));
	    }
	};
    
    private static class Frame {
	Collection<Part> parts = new LinkedList<Part>();
	int dur;
	Object id;
    }
    
    private AnimSprite(Owner owner, Resource res, Message sdt) {
	super(owner, res, sdt);
	boolean[] flags = decflags(sdt);
	Collection<Part> stp = new LinkedList<Part>();
	for(Resource.Image img : res.layers(Resource.imgc)) {
	    if((img.id < 0) || ((img.id < flags.length) && flags[img.id]))
		stp.add(new ImagePart(img));
	}
	frames = null;
	for(Resource.Anim anim : res.layers(Resource.animc)) {
	    if((anim.id < 0) || ((anim.id < flags.length) && flags[anim.id])) {
		if(frames == null) {
		    frames = new Frame[anim.f.length];
		} else {
		    if(anim.f.length != frames.length)
			throw(new ResourceException("Attempting to combine animations of different lengths", res));
		}
		for(int i = 0; i < frames.length; i++) {
		    Frame f;
		    if(frames[i] == null) {
			f = frames[i] = new Frame();
			f.dur = anim.d;
			for(Part p : stp)
			    f.parts.add(p);
			f.id = (res.name + ":" + res.ver + ":" + i).intern();
		    }
		    f = frames[i];
		    for(int o = 0; o < anim.f[i].length; o++)
			f.parts.add(new ImagePart(anim.f[i][o]));
		}
	    }
	}
	if(frames == null) {
	    frames = new Frame[1];
	    Frame f = frames[0] = new Frame();
	    f.dur = 10000;
	    f.parts = stp;
	    f.id = (res.name + ":" + res.ver).intern();
	}
	fno = 0;
	te = 0;
	curf = frames[0].parts;
    }
    
    public boolean tick(int dt) {
	boolean rv = false;
	te += dt;
	while(te > frames[fno].dur) {
	    te -= frames[fno].dur;
	    if(++fno >= frames.length) {
		fno = 0;
		rv = true;
	    }
	}
	curf = frames[fno].parts;
	return(rv);
    }
    
    public Object stateid() {
	return(frames[fno].id);
    }
}
