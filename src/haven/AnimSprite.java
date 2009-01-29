package haven;

import java.util.*;

public class AnimSprite extends ImageSprite {
    private Frame[] frames;
    private int fno, te;
    public static final Factory fact = new Factory() {
	    public Sprite create(Owner owner, Resource res, Message sdt) {
		Resource.Anim anim = res.layer(Resource.animc);
		if(anim == null)
		    return(null);
		return(new AnimSprite(owner, res, sdt, anim));
	    }
	};
    
    private static class Frame {
	Collection<Part> parts = new LinkedList<Part>();
	int dur;
	Object id;
    }
    
    private AnimSprite(Owner owner, Resource res, Message sdt, Resource.Anim anim) {
	super(owner, res, sdt);
	frames = new Frame[anim.f.length];
	for(int i = 0; i < frames.length; i++) {
	    Frame f = frames[i] = new Frame();
	    f.dur = anim.d;
	    for(int o = 0; o < anim.f[i].length; o++)
		f.parts.add(new ImagePart(anim.f[i][o]));
	    f.id = (res.name + ":" + res.ver + ":" + i).intern();
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
