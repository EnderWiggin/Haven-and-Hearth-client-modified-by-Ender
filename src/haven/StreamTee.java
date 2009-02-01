package haven;

import java.io.*;
import java.util.*;

public class StreamTee extends InputStream {
    private InputStream in;
    private List<OutputStream> forked = new LinkedList<OutputStream>();
    private boolean readeof = false;
    private boolean ncwe = false; /* NCWE = No Close Without EOF */
    
    public StreamTee(InputStream in) {
	this.in = in;
    }
    
    public int available() throws IOException {
	return(in.available());
    }
    
    public void close() throws IOException {
	in.close();
	if(!ncwe || readeof) {
	    synchronized(forked) {
		for(OutputStream s : forked)
		    s.close();
	    }
	}
    }
    
    public void setncwe() {
	ncwe = true;
    }
    
    public void flush() throws IOException {
	synchronized(forked) {
	    for(OutputStream s : forked)
		s.flush();
	}
    }
    
    public void mark(int limit) {}
    
    public boolean markSupported() {
	return(false);
    }
    
    public int read() throws IOException {
	int rv = in.read();
	if(rv >= 0) {
	    synchronized(forked) {
		for(OutputStream s : forked)
		    s.write(rv);
	    }
	} else {
	    readeof = true;
	}
	return(rv);
    }
    
    public int read(byte[] buf, int off, int len) throws IOException {
	int rv = in.read(buf, off, len);
	if(rv > 0) {
	    synchronized(forked) {
		for(OutputStream s : forked)
		    s.write(buf, off, rv);
	    }
	} else {
	    readeof = true;
	}
	return(rv);
    }
    
    public void reset() throws IOException {
	throw(new IOException("Mark not supported on StreamTee"));
    }
    
    public void attach(OutputStream s) {
	synchronized(forked) {
	    forked.add(s);
	}
    }
}
