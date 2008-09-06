package dolda.xiphutil;

import java.util.*;
import java.io.*;
import com.jcraft.jogg.Packet;
import com.jcraft.jorbis.*;

public class VorbisStream {
    private PacketStream in;
    private Info info = new Info();
    private Comment cmt = new Comment();
    private DspState dsp = new DspState();
    private Block blk = new Block(dsp);
    private float[][][] pcmp;
    private int[] idxp;
    public final Map<String, String> uc;
    public final String vnd;
    public final int chn, rate;
    
    public VorbisStream(PacketStream in) throws IOException {
	this.in = in;
	info.init();
	cmt.init();
	for(int i = 0; i < 3; i++) {
	    Packet pkt = in.packet();
	    if(pkt == null)
		throw(new VorbisException());
	    if(info.synthesis_headerin(cmt, pkt) < 0)
		throw(new VorbisException());
	}
	vnd = new String(cmt.vendor, 0, cmt.vendor.length - 1, "UTF-8");
	HashMap<String, String> uc = new HashMap<String, String>();
	for(int i = 0; i < cmt.user_comments.length - 1; i++) {
	    byte[] cb = cmt.user_comments[i];
	    String cs = new String(cb, 0, cb.length - 1, "UTF-8");
	    int ep;
	    if((ep = cs.indexOf('=')) < 1)
		throw(new VorbisException());
	    uc.put(cs.substring(0, ep).toLowerCase().intern(), cs.substring(ep + 1));
	}
	this.uc = Collections.unmodifiableMap(uc);
	chn = info.channels;
	rate = info.rate;
	dsp.synthesis_init(info);
	blk.init(dsp);
	pcmp = new float[1][][];
	idxp = new int[chn];
    }
    
    public VorbisStream(InputStream in) throws IOException {
	this(new PacketStream(new PageStream(in)));
    }
    
    public float[][] decode() throws IOException {
	while(true) {
	    int len = dsp.synthesis_pcmout(pcmp, idxp);
	    if(len > 0) {
		float[][] ret = new float[chn][];
		for(int i = 0; i < chn; i++)
		    ret[i] = Arrays.copyOfRange(pcmp[0][i], idxp[i], idxp[i] + len);
		dsp.synthesis_read(len);
		return(ret);
	    }
	    Packet pkt = in.packet();
	    if(pkt == null)
		return(null);
	    if((blk.synthesis(pkt) != 0) || (dsp.synthesis_blockin(blk) != 0))
		throw(new VorbisException());
	}
    }
    
    public InputStream pcmstream() {
	return(new InputStream() {
		private byte[] buf;
		private int bufp;
		
		private boolean convert() throws IOException {
		    float[][] inb = decode();
		    if(inb == null) {
			buf = new byte[0];
			return(false);
		    }
		    buf = new byte[2 * chn * inb[0].length];
		    int p = 0;
		    for(int i = 0; i < inb[0].length; i++) {
			for(int c = 0; c < chn; c++) {
			    int s = (int)(inb[c][i] * 32767);
			    buf[p++] = (byte)s;
			    buf[p++] = (byte)(s >> 8);
			}
		    }
		    bufp = 0;
		    return(true);
		}

		public int read() throws IOException {
		    byte[] rb = new byte[1];
		    int ret;
		    do {
			ret = read(rb);
			if(ret < 0)
			    return(-1);
		    } while(ret == 0);
		    return(rb[0]);
		}
    
		public int read(byte[] dst, int off, int len) throws IOException {
		    if((buf == null) && !convert())
			return(-1);
		    if(buf.length - bufp < len)
			len = buf.length - bufp;
		    System.arraycopy(buf, bufp, dst, off, len);
		    if((bufp += len) == buf.length)
			buf = null;
		    return(len);
		}
    
		public void close() throws IOException {
		    VorbisStream.this.close();
		}
	    });
    }
    
    public String toString() {
	return(String.format("Vorbis Stream (encoded by `%s', %d comments, %d channels, sampled at %d Hz)", vnd, uc.size(), chn, rate));
    }
    
    public static void main(String[] args) throws Exception {
	VorbisStream vs = new VorbisStream(new FileInputStream(args[0]));
	InputStream pcm = vs.pcmstream();
	byte[] buf = new byte[4096];
	int ret;
	while((ret = pcm.read(buf)) >= 0)
	    System.out.write(buf);
    }
    
    public void close() throws IOException {
	in.close();
    }
}
