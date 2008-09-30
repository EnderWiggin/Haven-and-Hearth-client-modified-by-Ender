package haven.test;

import java.util.*;
import haven.*;

public class MultiClient extends BaseTest {
    public Collection<TestClient> clients = new HashSet<TestClient>();
    public final int num;
    
    public MultiClient(int num) {
	this.num = num;
    }
    
    public void run() {
	for(int i = 0; i < num; i++) {
	    TestClient c = new TestClient("test" + (i + 1));
	    new CharSelector(c, null, null) {
		public void succeed() {
		    System.out.println("Selected character");
		}
	    };
	    synchronized(clients) {
		clients.add(c);
	    }
	    c.start();
	}
	while(true) {
	    try {
		Thread.sleep(1000);
	    } catch(InterruptedException e) {
		stopall();
	    }
	    int alive = 0;
	    for(TestClient c : clients) {
		if(c.alive())
		    alive++;
	    }
	    if(alive == 0) {
		printf("All clients are dead, exiting");
		break;
	    }
	    printf("Alive: %d/%d", alive, num);
	}
    }
    
    public void stopall() {
	synchronized(clients) {
	    for(TestClient c : clients)
		c.stop();
	}
    }
    
    public static void usage() {
	System.err.println("usage: MultiClient NUM");
    }

    public static void main(String[] args) {
	if(args.length < 1) {
	    usage();
	    System.exit(1);
	}
	new MultiClient(Integer.parseInt(args[0])).start();
    }
}
