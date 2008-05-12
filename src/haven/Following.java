package haven;

public class Following extends Moving {
	Gob tgt;
	Coord doff;
	
	public Following(Gob gob, Gob tgt, Coord doff) {
		super(gob);
		this.tgt = tgt;
		this.doff = doff;
	}
	
	public Coord getc() {
		return(tgt.getc().add(1, 1));
	}
}
