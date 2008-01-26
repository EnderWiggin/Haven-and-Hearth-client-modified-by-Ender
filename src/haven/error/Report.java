package haven.error;

public class Report implements java.io.Serializable {
    private boolean reported = false;
    public final Throwable t;
    public final long time;
    
    public Report(Throwable t) {
	this.t = t;
	time = System.currentTimeMillis();
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
