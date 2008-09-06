package dolda.xiphutil;

import java.io.*;
import com.jcraft.jogg.*;

public class PageStream {
    private SyncState sync = new SyncState();
    private final InputStream in;
    private boolean eos = false;
	
    public PageStream(InputStream in) {
	this.in = in;
	sync.init();
    }
	
    public Page page() throws IOException {
	if(eos)
	    return(null);
	Page page = new Page();
	while(true) {
	    int ret = sync.pageout(page);
	    if(ret < 0)
		throw(new OggException()); /* ? */
	    if(ret == 1) {
		if(page.eos() != 0)
		    eos = true;
		return(page);
	    }
	    int off = sync.buffer(4096);
	    int len = in.read(sync.data, off, 4096);
	    if(len < 0)
		return(null);
	    sync.wrote(len);
	}
    }
	
    public void close() throws IOException {
	in.close();
    }
}
