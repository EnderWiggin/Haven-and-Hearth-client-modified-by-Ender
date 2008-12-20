package haven;

import java.util.*;

public class Glob {
    public long time;
    public Astronomy ast;
    public OCache oc = new OCache(this);
    public MCache map;
    public Session sess;
    public Party party;
    public Collection<Resource> paginae = new TreeSet<Resource>();
    public Map<String, Integer> cattr = new HashMap<String, Integer>();
	
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
    
    public void cattr(Message msg) {
	while(!msg.eom()) {
	    String nm = msg.string();
	    int val = msg.int32();
	    cattr.put(nm.intern(), val);
	}
    }
}
