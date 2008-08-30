package haven;

import java.io.*;

public interface ResCache {
    public OutputStream store(String name) throws IOException;
    public InputStream fetch(String name) throws IOException;
    
    public static class TestCache implements ResCache {
	public OutputStream store(final String name) {
	    return(new ByteArrayOutputStream() {
		    public void close() {
			byte[] res = toByteArray();
			System.out.println(name + ": " + res.length);
		    }
		});
	}
	
	public InputStream fetch(String name) throws IOException {
	    throw(new FileNotFoundException());
	}
    }
}
