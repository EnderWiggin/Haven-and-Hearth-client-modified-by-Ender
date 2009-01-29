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
    public Map<String, CAttr> cattr = new HashMap<String, CAttr>();
	
    public Glob(Session sess) {
	this.sess = sess;
	map = new MCache(sess);
	party = new Party(this);
    }
    
    public static class CAttr extends Observable {
	String nm;
	int base, comp;
	
	public CAttr(String nm, int base, int comp) {
	    this.nm = nm.intern();
	    this.base = base;
	    this.comp = comp;
	}
	
	public void update(int base, int comp) {
	    if((base == this.base) && (comp == this.comp))
		return;
	    this.base = base;
	    this.comp = comp;
	    setChanged();
	    notifyObservers(null);
	}
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
	synchronized(cattr) {
	    while(!msg.eom()) {
		String nm = msg.string();
		int base = msg.int32();
		int comp = msg.int32();
		CAttr a = cattr.get(nm);
		if(a == null) {
		    a = new CAttr(nm, base, comp);
		    cattr.put(nm, a);
		} else {
		    a.update(base, comp);
		}
	    }
	}
    }
}
