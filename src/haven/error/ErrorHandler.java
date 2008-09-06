package haven.error;

import java.io.*;
import java.net.*;
import java.util.*;

public class ErrorHandler extends ThreadGroup {
    private static final URL errordest;
    private static final String[] sysprops = {
	"java.version",
	"java.vendor",
	"os.name",
	"os.arch",
	"os.version",
    };
    private final ThreadGroup initial;
    private Map<String, Object> props = new HashMap<String, Object>();
    private Reporter reporter;
	
    static {
	try {
	    errordest = new URL("http://www.havenandhearth.com/java/error");
	} catch(MalformedURLException e) {
	    throw(new Error(e));
	}
    }

    public static void setprop(String key, Object val) {
	ThreadGroup tg = Thread.currentThread().getThreadGroup();
	if(tg instanceof ErrorHandler)
	    ((ErrorHandler)tg).props.put(key, val);
    }

    private class Reporter extends Thread {
	private Queue<Report> errors = new LinkedList<Report>();
	private final ErrorStatus status;
	
	public Reporter(ErrorStatus status) {
	    super(initial, "Error reporter");
	    setDaemon(true);
	    this.status = status;
	}
	
	public void run() {
	    while(true) {
		synchronized(errors) {
		    try {
			errors.wait();
		    } catch(InterruptedException e) {
			return;
		    }
		    Report r;
		    while((r = errors.poll()) != null) {
			try {
			    doreport(r);
			} catch(Exception e) {
			    status.senderror(e);
			}
		    }
		}
	    }
	}
	
	private void doreport(Report r) throws IOException {
	    status.goterror(r.t);
	    URLConnection c = errordest.openConnection();
	    status.connecting();
	    c.setDoOutput(true);
	    c.addRequestProperty("Content-Type", "application/x-java-error");
	    c.connect();
	    ObjectOutputStream o = new ObjectOutputStream(c.getOutputStream());
	    o.writeObject(r);
	    o.close();
	    status.sending();
	    InputStream i = c.getInputStream();
	    byte[] buf = new byte[1024];
	    while(i.read(buf) >= 0);
	    i.close();
	    status.done();
	}
    
	public void report(Throwable t) {
	    Report r = new Report(t);
	    r.props.putAll(props);
	    synchronized(errors) {
		errors.add(r);
		errors.notifyAll();
	    }
	    try {
		r.join();
	    } catch(InterruptedException e) { /* XXX? */ }
	}
    }

    private void defprops() {
	for(String p : sysprops)
	    props.put(p, System.getProperty(p));
	Runtime rt = Runtime.getRuntime();
	props.put("cpus", rt.availableProcessors());
    }

    public ErrorHandler(ErrorStatus ui) {
	super("Haven client");
	initial = Thread.currentThread().getThreadGroup();
	reporter = new Reporter(ui);
	reporter.start();
	defprops();
    }
    
    public ErrorHandler() {
	this(new ErrorStatus.Simple());
    }
    
    public void uncaughtException(Thread t, Throwable e) {
	reporter.report(e);
    }
}
