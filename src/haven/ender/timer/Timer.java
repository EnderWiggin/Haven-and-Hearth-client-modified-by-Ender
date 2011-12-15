package ender.timer;

import haven.Coord;
import haven.Label;
import haven.UI;
import haven.Window;


public class Timer {
    private static final int SERVER_RATIO = 3;
    
    public static long server;
    public static long local;
    
    private long start;
    private long time;
    private String name;
    private long seconds;
    public Callback updcallback;
    
    public Timer(long start, long time, String name){
	this.start = start;
	this.time = time;
	this.name = name;
	TimerController.getInstance().add(this);
    }
    
    public Timer(long time, String name){
	this(0, time, name);
    }
    
    public boolean isWorking(){
	return start != 0;
    }
    
    public void stop(){
	start = 0;
	if(updcallback != null){
	    updcallback.run(this);
	}
	TimerController.getInstance().save();
    }
    
    public void start(){
	start = server + SERVER_RATIO*((System.currentTimeMillis()/1000)-local);
	TimerController.getInstance().save();
    }
    
    public void start(long start){
	this.start = start;
    }
    
    public synchronized boolean update(){
	long now = System.currentTimeMillis()/1000;
	seconds = time - now + local - (server - start)/SERVER_RATIO;
	if(seconds <= 0){
	    Window wnd = new Window(new Coord(250,100), Coord.z, UI.instance.root, "Timer");
	    String str;
	    if(seconds < -60){
		str = String.format("%s elapsed since timer named \"%s\"  finished it's work", toString(), name);
	    } else {
		str = String.format("Timer named \"%s\" just finished it's work", name);
	    }
	    new Label(Coord.z, wnd, str);
	    wnd.justclose = true;
	    wnd.pack();
	    return true;
	}
	if(updcallback != null){
	    updcallback.run(this);
	}
	return false;
    }
    
    public synchronized long getStart() {
        return start;
    }

    public synchronized void setStart(long start) {
        this.start = start;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized void setTime(long time) {
        this.time = time;
    }

    public synchronized long getTime()
    {
	return time;
    }

    @Override
    public String toString() {
	long t = Math.abs(isWorking()?seconds:time);
	int h = (int) (t/3600);
	int m = (int) ((t%3600)/60);
	int s = (int) (t%60);
	return String.format("%d:%02d:%02d", h,m,s);
    }
    
    public void destroy(){
	TimerController.getInstance().remove(this);
	updcallback = null;
    }
    
}
