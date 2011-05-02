package ender.timer;

import java.util.ArrayList;
import java.util.List;

public class TimerController extends Thread {

    private List<Timer> timers;
    private long server=0, local=0;
    
    public TimerController(){
	super("Timer Thread");
	timers = new ArrayList<Timer>();
	start();
    }
    
 // Thread main process
    @Override
    public void run() {
	while(true) {
	    synchronized (timers) {
		long now = System.currentTimeMillis()/1000;
		int i = 0;
		while(i < timers.size()){
		    Timer timer = timers.get(i);
		    if(timer.update(server, local, now)){
			timers.remove(i);
		    } else{
			i++;
		    }
		}
	    }	    
	    try {
		sleep(1000);
	    } catch (InterruptedException e) {}
	}
    }
    
    public void add(Timer timer){
	synchronized (timers) {
	    timers.add(timer);
	}
    }
    
    public void update(long server, long local){
	this.server = server;
	this.local = local;
    }
    
}
