package dolda.xiphutil;

import java.io.IOException;
import com.jcraft.jogg.*;

public class PacketStream {
    private StreamState strm = null;
    private Page page = null;
    private final PageStream in;
    private boolean eos = false;
	
    public PacketStream(PageStream in) {
	this.in = in;
    }
	
    public Packet packet() throws IOException {
	if(eos)
	    return(null);
	if(strm == null) {
	    strm = new StreamState();
	    page = in.page();
	    strm.init(page.serialno());
	}
	Packet pkt = new Packet();
	while(true) {
	    int ret = strm.packetout(pkt);
	    if(ret < 0)
		throw(new OggException()); /* ? */
	    if(ret == 1)
		return(pkt);
	    if(page == null) {
		if((page = in.page()) == null) {
		    eos = true;
		    return(null);
		}
	    }
	    if(strm.pagein(page) != 0)
		throw(new OggException());
	    page = null;
	}
    }
	
    public void close() throws IOException {
	in.close();
    }
}
