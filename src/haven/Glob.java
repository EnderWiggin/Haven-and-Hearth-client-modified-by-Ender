package haven;

public class Glob {
	long time;
	Astronomy ast;
	OCache oc = new OCache();
	MCache map;
	Session sess;
	
	public Glob(Session sess) {
		this.sess = sess;
		map = new MCache(sess);
	}
	
	private static double defix(int i) {
		return(((double)i) / 1e9);
	}
	
	public void blob(Message msg) {
		time = msg.int32();
		double dt = defix(msg.int32());
		double mp = defix(msg.int32());
		double yt = defix(msg.int32());
		boolean night = (dt < 0.25) || (dt > 0.75);
		ast = new Astronomy(dt, mp, yt, night);
	}
}
