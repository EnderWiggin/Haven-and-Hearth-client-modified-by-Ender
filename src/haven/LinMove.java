package haven;

public class LinMove extends Moving {
    Coord s, t;
    int c, l;
    
    public LinMove(Gob gob, Coord s, Coord t, int c) {
	super(gob);
	this.s = s;
	this.t = t;
	this.c = c;
	this.l = 0;
    }
    
    public Coord getc() {
	double dx, dy, a;
	dx = t.x - s.x;
	dy = t.y - s.y;
	a = (double)l / (double)c;
	Coord m = new Coord((int)(dx * a), (int)(dy * a));
	return(s.add(m));
    }
    
    @Override
    public void tick() {
	l++;
    }
}
