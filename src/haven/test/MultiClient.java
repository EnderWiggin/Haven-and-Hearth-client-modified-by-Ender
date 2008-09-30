package haven.test;

import java.util.*;
import haven.*;

public class MultiClient extends BaseTest {
    public Collection<TestClient> clients = new HashSet<TestClient>();
    public int num = 10;

    public void run() {
	for(int i = 0; i < num; i++) {
	    TestClient c = new TestClient("test" + i);
	    synchronized(clients) {
		clients.add(c);
	    }
	    c.start();
	}
	while(true) {
	    try {
		Thread.sleep(1000);
	    } catch(InterruptedException e) {
		stop();
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
    
    public void stop() {
	synchronized(clients) {
	    for(TestClient c : clients)
		c.stop();
	}
    }
    
    public static void main(String[] args) {
	new MultiClient().start();
    }
}
