package haven;

import java.io.*;
import dolda.xiphutil.VorbisStream;

/*
 * Not that I'm ungrateful for the work the JCraft has done with
 * writing an Ogg/Vorbis decoder in Java, they really should try and
 * learn some rational API design.
 */
public class VorbisDecoder extends InputStream {
    private VorbisStream in;
    private byte[] buf;
    private int bufp;
    public final int chn, rate;
    
    public VorbisDecoder(InputStream in) throws IOException {
	this.in = new VorbisStream(in);
	chn = this.in.chn;
	rate = this.in.rate;
    }
    
    private boolean decode() throws IOException {
	float[][] inb = in.decode();
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
	if((buf == null) && !decode())
	    return(-1);
	if(buf.length - bufp < len)
	    len = buf.length - bufp;
	System.arraycopy(buf, bufp, dst, off, len);
	if((bufp += len) == buf.length)
	    buf = null;
	return(len);
    }
    
    public void close() throws IOException {
	in.close();
    }
    
    public static void main(String[] args) throws Exception {
	InputStream dec = new VorbisDecoder(new FileInputStream(args[0]));
	byte[] buf = new byte[4096];
	int ret;
	while((ret = dec.read(buf)) >= 0)
	    System.out.write(buf, 0, ret);
    }
}
