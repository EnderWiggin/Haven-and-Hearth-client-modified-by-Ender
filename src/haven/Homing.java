package haven;

public class Homing extends Moving {
	int tgt;
	Coord tc;
	int v;
	double dist;
	
	public Homing(Gob gob, int tgt, Coord tc, int v) {
		super(gob);
		this.tgt = tgt;
		this.tc = tc;
		this.v = v;
	}
	
	public Coord getc() {
		Coord tc = this.tc;
		Gob tgt = gob.glob.oc.getgob(this.tgt);
		if(tgt != null)
			tc = tgt.rc;
		Coord d = tc.add(gob.rc.inv());
		double e = gob.rc.dist(tc);
		return(gob.rc.add((int)((d.x / e) * dist), (int)((d.y / e) * dist)));
	}
	
	public void move(Coord c) {
		dist = 0;
	}
	
	public void ctick(int dt) {
		double da = ((double)dt / 1000) / 0.06;
		dist += (da * 0.9) * ((double)v / 100);
	}
}
