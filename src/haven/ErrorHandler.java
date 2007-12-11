package haven;

import java.io.*;
import java.net.*;

public class ErrorHandler extends ThreadGroup {
    private static final URL errordest;
	
    static {
	try {
	    errordest = new URL("http://localhost/~fredrik/haven/error");
	} catch(MalformedURLException e) {
	    throw(new Error(e));
	}
    }

    public ErrorHandler(Runnable main) {
	super("Haven client");
	Thread init = new Thread(this, main);
	init.start();
    }
    
    public void uncaughtException(Thread t, Throwable e) {
	try {
	    System.err.println("An error occurred: " + t);
	    URLConnection c = errordest.openConnection();
	    System.err.println("Connecting to error server");
	    c.setDoOutput(true);
	    c.addRequestProperty("Content-Type", "application/x-java-error");
	    c.connect();
	    System.err.println("Connected to error server");
	    ObjectOutputStream o = new ObjectOutputStream(c.getOutputStream());
	    o.writeObject(e);
	    o.close();
	    System.err.println("Error sent!");
	    InputStream i = c.getInputStream();
	    byte[] buf = new byte[1024];
	    while(i.read(buf) >= 0);
	    i.close();
	    System.err.println("Got response");
	} catch(IOException me) {
	    me.printStackTrace();
	    System.exit(3);
	} finally {
	    System.exit(2);
	}
    }
}
