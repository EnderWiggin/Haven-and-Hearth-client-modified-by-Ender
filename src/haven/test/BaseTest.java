package haven.test;

import java.util.*;
import haven.*;

public abstract class BaseTest implements Runnable {
    public ThreadGroup tg;
    
    public BaseTest() {
	tg = new ThreadGroup("Test process");
	Resource.loadergroup = tg;
    }
    
    public static void printf(String fmt, Object... args) {
	System.out.println(String.format(fmt, args));
    }
    
    public void start() {
	Thread t = new Thread(tg, this, "Test controller");
	t.start();
    }
}
