package haven.error;

import java.io.*;
import java.net.*;
import java.util.Queue;
import java.util.LinkedList;

public class ErrorHandler extends ThreadGroup {
    private static final URL errordest;
    private final ThreadGroup initial;
    private Reporter reporter;
	
    static {
	try {
	    errordest = new URL("http://www.havenandhearth.com/java/error");
	} catch(MalformedURLException e) {
	    throw(new Error(e));
	}
    }

    private class Reporter extends Thread {
	private Queue<Throwable> errors = new LinkedList<Throwable>();
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
		    Throwable t;
		    while((t = errors.poll()) != null) {
			try {
			    doreport(t);
			} catch(Exception e) {
			    status.senderror(e);
			}
		    }
		}
	    }
	}
	
	private void doreport(Throwable t) throws IOException {
	    status.goterror(t);
	    URLConnection c = errordest.openConnection();
	    status.connecting();
	    c.setDoOutput(true);
	    c.addRequestProperty("Content-Type", "application/x-java-error");
	    c.connect();
	    ObjectOutputStream o = new ObjectOutputStream(c.getOutputStream());
	    o.writeObject(t);
	    o.close();
	    status.sending();
	    InputStream i = c.getInputStream();
	    byte[] buf = new byte[1024];
	    while(i.read(buf) >= 0);
	    i.close();
	    status.done();
	}
    
	public void report(Throwable t) {
	    synchronized(errors) {
		errors.add(t);
		errors.notifyAll();
	    }
	}
    }

    public ErrorHandler(ErrorStatus ui) {
	super("Haven client");
	initial = Thread.currentThread().getThreadGroup();
	reporter = new Reporter(ui);
	reporter.start();
    }
    
    public ErrorHandler() {
	this(new ErrorStatus.Simple());
    }
    
    public void uncaughtException(Thread t, Throwable e) {
	reporter.report(e);
    }
}
