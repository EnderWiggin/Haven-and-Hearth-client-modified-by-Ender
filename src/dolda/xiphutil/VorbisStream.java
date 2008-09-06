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
    
    public String toString() {
	return(String.format("Vorbis Stream (encoded by `%s', %d comments, %d channels, sampled at %d Hz)", vnd, uc.size(), chn, rate));
    }
    
    public static void main(String[] args) throws Exception {
	VorbisStream vs = new VorbisStream(new FileInputStream(args[0]));
	float[][] buf;
	while((buf = vs.decode()) != null) {
	    byte[] dst = new byte[2 * vs.chn * buf[0].length];
	    int p = 0;
	    for(int i = 0; i < buf[0].length; i++) {
		for(int c = 0; c < vs.chn; c++) {
		    int s = (int)(buf[c][i] * 32767);
		    dst[p++] = (byte)s;
		    dst[p++] = (byte)(s >> 8);
		}
	    }
	    System.out.write(dst);
	}
    }
    
    public void close() throws IOException {
	in.close();
    }
}
