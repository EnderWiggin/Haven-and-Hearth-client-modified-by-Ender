package haven;

public class Coord implements Comparable<Coord> {
	public int x, y;
	
	public Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Coord(java.awt.Dimension d) {
		this(d.width, d.height);
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof Coord))
			return(false);
		Coord c = (Coord)o;
		return((c.x == x) && (c.y == y));
	}
	
	public int compareTo(Coord c) {
		if(c.y != y)
			return(c.y - y);
		if(c.x != x)
			return(c.x - x);
		return(0);
	}
	
	public Coord add(Coord b) {
		return(new Coord(x + b.x, y + b.y));
	}
	
	public Coord mul(int f) {
		return(new Coord(x * f, y * f));
	}
	
	public Coord inv() {
		return(new Coord(-x, -y));
	}
	
	public Coord mul(Coord f) {
		return(new Coord(x * f.x, y * f.y));
	}
	
	public Coord div(Coord d) {
		int v, w;
		
		v = x / d.x;
		w = y / d.y;
		if(x < 0)
			v--;
		if(y < 0)
			w--;
		return(new Coord(v, w));
	}
	
	public Coord mod(Coord d) {
		int v, w;
		
		v = x % d.x;
		w = y % d.y;
		if(v < 0)
			v += d.x;
		if(w < 0)
			w += d.y;
		return(new Coord(v, w));
	}
	
	public boolean isect(Coord c, Coord s) {
		return((x >= c.x) && (y >= c.y) && (x < c.x + s.x) && (y < c.y + s.y));
	}
	
	public String toString() {
		return("(" + x + ", " + y + ")");
	}
}
