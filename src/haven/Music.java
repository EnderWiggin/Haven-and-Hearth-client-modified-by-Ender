package haven;

import java.io.*;
import javax.sound.midi.*;

public class Music {
    private static Player player;
    
    private static class Player extends Thread {
	private Resource res;
	private Thread waitfor;
	private Sequencer seq;
	private Synthesizer synth;
	
	private Player(Resource res, Thread waitfor) {
	    super(Utils.tg(), "Music player");
	    setDaemon(true);
	    this.res = res;
	    this.waitfor = waitfor;
	}
	
	public void run() {
	    try {
		if(waitfor != null)
		    waitfor.join();
		res.loadwaitint();
		try {
		    seq = MidiSystem.getSequencer(false);
		    synth = MidiSystem.getSynthesizer();
		    seq.open();
		    seq.setSequence(res.layer(Resource.Music.class).seq);
		    synth.open();
		    seq.getTransmitter().setReceiver(synth.getReceiver());
		} catch(MidiUnavailableException e) {
		    return;
		} catch(InvalidMidiDataException e) {
		    return;
		}
		seq.addMetaEventListener(new MetaEventListener() {
			public void meta(MetaMessage msg) {
			    if(msg.getType() == 47)
				Player.this.interrupt();
			}
		    });
		seq.start();
		while(true)
		    Thread.sleep(10000);
	    } catch(InterruptedException e) {
	    } finally {
		if(seq != null)
		    seq.close();
		if(synth != null)
		    synth.close();
		synchronized(Music.class) {
		    if(player == this)
			player = null;
		}
	    }
	}
    }
    
    public static void play(Resource res) {
	synchronized(Music.class) {
	    if(player != null)
		player.interrupt();
	    if(res != null) {
		player = new Player(res, player);
		player.start();
	    }
	}
    }
}
