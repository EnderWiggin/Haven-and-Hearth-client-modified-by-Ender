package haven.error;

import java.util.*;

public class Report implements java.io.Serializable {
    private boolean reported = false;
    public final Throwable t;
    public final long time;
    public final Map<String, Object> props = new HashMap<String, Object>();
    
    public Report(Throwable t) {
	this.t = t;
	time = System.currentTimeMillis();
	Runtime rt = Runtime.getRuntime();
	props.put("mem.free", rt.freeMemory());
	props.put("mem.total", rt.totalMemory());
	props.put("mem.max", rt.maxMemory());
    }
    
    synchronized void join() throws InterruptedException {
	while(!reported)
	    wait();
    }
    
    synchronized void done() {
	reported = true;
	notifyAll();
    }
}
