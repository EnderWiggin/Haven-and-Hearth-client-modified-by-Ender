package haven;

public class LinMove extends Moving {
	Coord s, t;
	int c;
	double a;
    
	public LinMove(Gob gob, Coord s, Coord t, int c) {
		super(gob);
		this.s = s;
		this.t = t;
		this.c = c;
		this.a = 0;
	}
    
	public Coord getc() {
		double dx, dy;
		dx = t.x - s.x;
		dy = t.y - s.y;
		Coord m = new Coord((int)(dx * a), (int)(dy * a));
		return(s.add(m));
	}
    
	/*
	public void tick() {
		if(l < c)
			l++;
	}
	*/
	
	public void ctick(int dt) {
		double da = ((double)dt / 1000) / (((double)c) * 0.06);
		a += da * 0.9;
	}
	
	public void setl(int l) {
		double a = ((double)l) / ((double)c);
		if(a > this.a)
			this.a = a;
	}
}
