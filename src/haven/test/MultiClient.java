package haven.test;

import java.util.*;
import haven.*;

public class MultiClient extends BaseTest {
    public Collection<TestClient> clients = new HashSet<TestClient>();
    public int num, delay;
    public int started;
    
    public MultiClient(int num, int delay) {
	this.num = num;
	this.delay = delay;
	this.started = 0;
    }
    
    public void run() {
	long lastck = System.currentTimeMillis();
	long laststarted = 0;
	try {
	    while(true) {
		long now = System.currentTimeMillis();
		long timeout = 1000;
		if((started < num) && (now - laststarted >= delay)) {
		    TestClient c = new TestClient("test" + (started + 1));
		    new CharSelector(c, null, null) {
			public void succeed() {
			    System.out.println("Selected character");
			}
		    };
		    synchronized(clients) {
			clients.add(c);
		    }
		    c.start();
		    started++;
		    laststarted = now;
		}
		if((started < num) && ((delay - (now - laststarted)) < timeout))
		    timeout = delay - (now - laststarted);
		if(timeout < 0)
		    timeout = 0;
		try {
		    Thread.sleep(timeout);
		} catch(InterruptedException e) {
		    num = 0;
		    stopall();
		}
		if(now - lastck > 1000) {
		    int alive = 0;
		    for(TestClient c : clients) {
			if(c.alive())
			    alive++;
		    }
		    if((alive == 0) && (started >= num)) {
			printf("All clients are dead, exiting");
			break;
		    }
		    printf("Alive: %d/%d/%d", alive, started, num);
		    lastck = now;
		}
	    }
	} finally {
	    stopall();
	}
    }
    
    public void stopall() {
	synchronized(clients) {
	    for(TestClient c : clients)
		c.stop();
	}
    }
    
    public static void usage() {
	System.err.println("usage: MultiClient NUM [DELAY]");
    }

    public static void main(String[] args) {
	if(args.length < 1) {
	    usage();
	    System.exit(1);
	}
	int num = Integer.parseInt(args[0]);
	int delay = 0;
	if(args.length > 1)
	    delay = Integer.parseInt(args[1]);
	new MultiClient(num, delay).start();
    }
}
