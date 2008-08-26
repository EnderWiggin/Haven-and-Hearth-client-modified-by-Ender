package haven.resutil;

import haven.Resource;
import haven.Sprite;
import haven.Gob;

public class DynSprite extends Sprite {
    protected DynSprite(Gob gob, Resource res, int frames) {
	super(gob, res, frames);
    }
    
    protected static Resource myres(Class<? extends DynSprite> c) {
	ClassLoader cl = c.getClassLoader();
	if(cl instanceof Resource.ResClassLoader) {
	    return(((Resource.ResClassLoader)cl).getres());
	} else {
	    return(null);
	}
    }
}
