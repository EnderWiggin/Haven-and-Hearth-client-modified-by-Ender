package haven;

import java.util.*;
import java.io.InputStream;
import javax.sound.sampled.*;

public class Audio {
    private static Thread player;
    public static final AudioFormat fmt = new AudioFormat(44100, 16, 2, true, false);
    private static Collection<CS> ncl = new LinkedList<CS>();
    
    public interface CS {
	public boolean get(double[] sample);
    }
	
    private static class Player extends Thread {
	private Collection<CS> clips = new LinkedList<CS>();
	private int srate, nch = 2;
	
	Player() {
	    super(Utils.tg(), "Haven audio player");
	    setDaemon(true);
	    srate = (int)fmt.getSampleRate();
	}
	
	private void fillbuf(byte[] buf, int off, int len) {
	    double[] val = new double[nch];
	    double[] sm = new double[nch];
	    while(len > 0) {
		for(int i = 0; i < nch; i++)
		    val[i] = 0;
		for(Iterator<CS> i = clips.iterator(); i.hasNext();) {
		    CS cs = i.next();
		    if(!cs.get(sm)) {
			i.remove();
			continue;
		    }
		    for(int ch = 0; ch < nch; ch++)
			val[ch] += sm[ch];
		}
		for(int i = 0; i < nch; i++) {
		    int iv = (int)(val[i] * 32767.0);
		    if(iv < 0) {
			if(iv < -32768)
			    iv = -32768;
			iv += 65536;
		    } else {
			if(iv > 32767)
			    iv = 32767;
		    }
		    buf[off++] = (byte)(iv & 0xff);
		    buf[off++] = (byte)((iv & 0xff00) >> 8);
		    len -= 2;
		}
	    }
	}

	public void run() {
	    SourceDataLine line = null;
	    try {
		try {
		    line = (SourceDataLine)AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, fmt));
		    line.open(fmt);
		    line.start();
		} catch(Exception e) {
		    e.printStackTrace();
		    return;
		}
		byte[] buf = new byte[1024];
		while(true) {
		    synchronized(ncl) {
			for(CS cs : ncl)
			    clips.add(cs);
			ncl.clear();
		    }
		    if(Thread.interrupted())
			throw(new InterruptedException());
		    fillbuf(buf, 0, 1024);
		    for(int off = 0; off < buf.length; off += line.write(buf, off, buf.length - off));
		}
	    } catch(InterruptedException e) {
	    } finally {
		synchronized(Audio.class) {
		    player = null;
		}
		if(line != null)
		    line.close();
	    }
	}
    }

    private static synchronized void ckpl() {
	if(player == null) {
	    player = new Player();
	    player.start();
	}
    }
    
    public static void play(final InputStream clip, final double vol, final double sp) {
	synchronized(ncl) {
	    ncl.add(new CS() {
		    int ack = 0;
		    double[] ov = new double[2];
		    
		    public boolean get(double[] sm) {
			try {
			    ack += 44100.0 * sp;
			    while(ack >= 44100) {
				for(int i = 0; i < 2; i++) {
				    int b1 = clip.read();
				    int b2 = clip.read();
				    if((b1 < 0) || (b2 < 0))
					return(false);
				    int v = b1 + (b2 << 8);
				    if(v >= 32768)
					v -= 65536;
				    ov[i] = ((double)v / 32768.0) * vol;
				}
				ack -= 44100;
			    }
			} catch(java.io.IOException e) {
			    return(false);
			}
			for(int i = 0; i < 2; i++)
			    sm[i] = ov[i];
			return(true);
		    }
		});
	}
	ckpl();
    }

    public static void play(byte[] clip, double vol, double sp) {
	play(new java.io.ByteArrayInputStream(clip), vol, sp);
    }
    
    public static void play(byte[] clip) {
	play(clip, 1.0, 1.0);
    }
    
    public static byte[] readclip(InputStream in) throws java.io.IOException {
	AudioInputStream cs;
	try {
	    cs = AudioSystem.getAudioInputStream(fmt, AudioSystem.getAudioInputStream(in));
	} catch(UnsupportedAudioFileException e) {
	    throw(new java.io.IOException(e));
	}
	java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
	byte[] bbuf = new byte[65536];
	while(true) {
	    int rv = cs.read(bbuf);
	    if(rv < 0)
		break;
	    buf.write(bbuf, 0, rv);
	}
	return(buf.toByteArray());
    }
}
