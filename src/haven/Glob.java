package haven;

import java.util.*;

public class Glob {
	long time;
	Astronomy ast;
	OCache oc = new OCache(this);
	MCache map;
	Session sess;
	Party party;
	int glut, fcap, stamina, stamcap;
	Collection<Resource> paginae = new TreeSet<Resource>();
	
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
