package haven;

import java.util.*;

public class Glob {
	public long time;
	public Astronomy ast;
	public OCache oc = new OCache(this);
	public MCache map;
	public Session sess;
	public Party party;
	public int glut, fcap, stamina, stamcap;
	public Collection<Resource> paginae = new TreeSet<Resource>();
	
	public Glob(Session sess) {
		this.sess = sess;
		map = new MCache(sess);
		party = new Party(this);
	}
	
	private static double defix(int i) {
		return(((double)i) / 1e9);
	}
	
	public void blob(Message msg) {
		time = msg.int32();
		double dt = defix(msg.int32());
		double mp = defix(msg.int32());
		double yt = defix(msg.int32());
		glut = msg.int32();
		fcap = msg.int32();
		stamina = msg.int32();
		stamcap = msg.int32();
		boolean night = (dt < 0.25) || (dt > 0.75);
		ast = new Astronomy(dt, mp, yt, night);
	}
	
	public void paginae(Message msg) {
		while(!msg.eom()) {
			int act = msg.uint8();
			if(act == '+') {
				String nm = msg.string();
				int ver = msg.uint16();
				paginae.add(Resource.load(nm, ver)); 
			} else if(act == '-') {
			}
		}
	}
}
