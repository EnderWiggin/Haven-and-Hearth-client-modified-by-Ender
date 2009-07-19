package haven;

public class ResDrawable extends Drawable {
    final Indir<Resource> res;
    final Message sdt;
    Sprite spr = null;
    int delay = 0;
	
    public ResDrawable(Gob gob, Indir<Resource> res, Message sdt) {
	super(gob);
	this.res = res;
	this.sdt = sdt;
	init();
    }
	
    public ResDrawable(Gob gob, Resource res) {
	this(gob, res.indir(), new Message(0));
    }
	
    public void init() {
	if(spr != null)
	    return;
	if(res.get() == null)
	    return;
	spr = Sprite.create(gob, res.get(), sdt.clone());
    }
	
    public boolean checkhit(Coord c) {
	init();
	if(spr != null)
	    return(spr.checkhit(c));
	return(false);
    }
	
    public void setup(Sprite.Drawer d, Coord cc, Coord off) {
	init();
	if(spr != null)
	    spr.setup(d, cc, off);
    }
	
    public void ctick(int dt) {
	if(spr == null) {
	    delay += dt;
	} else {
	    spr.tick(delay + dt);
	    delay = 0;
	}
    }
}
