package haven;

public class Astronomy {
	double dt, mp, yt;
	boolean night;
	
	public boolean equals(Object o) {
		if(!(o instanceof Astronomy))
			return(false);
		Astronomy a = (Astronomy)o;
		if(a.dt != dt)
			return(false);
		if(a.mp != mp)
			return(false);
		if(a.yt != yt)
			return(false);
		if(a.night != night)
			return(false);
		return(true);
	}
	
	public Astronomy(double dt, double mp, double yt, boolean night) {
		this.dt = dt;
		this.mp = mp;
		this.yt = yt;
		this.night = night;
	}
}
