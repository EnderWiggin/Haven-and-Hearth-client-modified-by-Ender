package haven;

public class Glob {
	long time;
	double dt, mp, yt;
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
		dt = defix(msg.int32());
		mp = defix(msg.int32());
		yt = defix(msg.int32());
	}
}
