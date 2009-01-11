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
	    ((ErrorHandler)tg).lsetprop(key, val);
    }
    
    public void lsetprop(String key, Object val) {
	props.put(key, val);
    }

    private class Reporter extends Thread {
	private Queue<Report> errors = new LinkedList<Report>();
	private ErrorStatus status;
	
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
	    if(!status.goterror(r.t))
		return;
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
    
	public void report(Thread th, Throwable t) {
	    Report r = new Report(t);
	    r.props.putAll(props);
	    r.props.put("thnm", th.getName());
	    r.props.put("thcl", th.getClass().getName());
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
	InputStream in = ErrorHandler.class.getResourceAsStream("/buildinfo");
	try {
	    try {
		if(in != null) {
		    Properties info = new Properties();
		    info.load(in);
		    for(Map.Entry<Object, Object> e : info.entrySet())
			props.put("jar." + (String)e.getKey(), e.getValue());
		}
	    } finally {
		in.close();
	    }
	} catch(IOException e) {
	    throw(new Error(e));
	}
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
    
    public void sethandler(ErrorStatus handler) {
	reporter.status = handler;
    }
    
    public void uncaughtException(Thread t, Throwable e) {
	reporter.report(t, e);
    }
}
