package haven;

public class Following extends Moving {
    int tgt;
    Coord doff;
	
    public Following(Gob gob, int tgt, Coord doff) {
	super(gob);
	this.tgt = tgt;
	this.doff = doff;
    }
	
    public Coord getc() {
	Gob tgt = gob.glob.oc.getgob(this.tgt);
	if(tgt == null)
	    return(gob.rc);
	return(tgt.getc().add(1, 1));
    }
}
