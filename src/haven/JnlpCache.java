package haven;

import java.lang.reflect.*;
import java.io.*;
import java.net.URL;
import javax.jnlp.*;

public class JnlpCache implements ResCache {
    private PersistenceService back;
    private URL base;
    
    private JnlpCache(PersistenceService back, URL base) {
	this.back = back;
	this.base = base;
	System.out.println(this.back);
	System.out.println(this.base);
    }
    
    public static JnlpCache create() {
	try {
	    Class<? extends ServiceManager> cl = Class.forName("javax.jnlp.ServiceManager").asSubclass(ServiceManager.class);
	    Method m = cl.getMethod("lookup", String.class);
	    BasicService basic = (BasicService)m.invoke(null, "javax.jnlp.BasicService");
	    PersistenceService prs = (PersistenceService)m.invoke(null, "javax.jnlp.PersistenceService");
	    return(new JnlpCache(prs, basic.getCodeBase()));
	} catch(Exception e) {
	    return(null);
	}
    }
    
    private void put(URL loc, byte[] data) {
	System.out.println("Trying to cache " + data.length + " bytes in " + loc);
	FileContents file;
	try {
	    try {
		file = back.get(loc);
	    } catch(FileNotFoundException e) {
		back.create(loc, data.length);
		file = back.get(loc);
	    }
	    if(file.getMaxLength() < data.length) {
		if(file.setMaxLength(data.length) < data.length) {
		    back.delete(loc);
		    return;
		}
	    }
	    OutputStream s = file.getOutputStream(true);
	    try {
		s.write(data);
		System.out.println(loc + " cached successfully");
	    } finally {
		s.close();
	    }
	} catch(IOException e) {
	    return;
	}
    }

    public OutputStream store(final String name) throws IOException {
	OutputStream ret = new ByteArrayOutputStream() {
		public void close() {
		    byte[] res = toByteArray();
		    try {
			put(new URL(base, name), res);
		    } catch(java.net.MalformedURLException e) {
			throw(new RuntimeException(e));
		    }
		}
	    };
	return(ret);
    }
    
    public InputStream fetch(String name) throws IOException {
	URL loc = new URL(base, name);
	System.out.println("Fetching " + name + " from JNLP cache at " + loc);
	FileContents file = back.get(loc);
	InputStream in = file.getInputStream();
	System.out.println("JNLP Success");
	return(in);
    }
}
