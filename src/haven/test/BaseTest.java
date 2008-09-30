package haven.test;

import java.util.*;
import haven.*;

public abstract class BaseTest implements Runnable {
    public ThreadGroup tg;
    public Thread me;
    
    public BaseTest() {
	tg = new ThreadGroup("Test process");
	Resource.loadergroup = tg;
	Runtime.getRuntime().addShutdownHook(new Thread() {
		public void run() {
		    printf("Terminating test upon JVM shutdown...");
		    BaseTest.this.stop();
		    try {
			me.join();
			printf("Shut down cleanly");
		    } catch(InterruptedException e) {
			printf("Termination handler interrupted");
		    }
		}
	    });
    }
    
    public static void printf(String fmt, Object... args) {
	System.out.println(String.format(fmt, args));
    }
    
    public void start() {
	me = new Thread(tg, this, "Test controller");
	me.start();
    }
    
    public void stop() {
	me.interrupt();
    }
}
