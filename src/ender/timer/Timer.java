package ender.timer;


public class Timer {
    private static final int SERVER_RATIO = 3;
    
    private long start;
    private long time;
    private String name;
    private int type;
    private long seconds;
    public Callback callback;
    
    public Timer(long server, long local, long time, String name, Callback callback, int type){
	this.start = server + SERVER_RATIO*((System.currentTimeMillis()/1000)-local);
	this.time = time;
	this.name = name;
	this.type = type;
	this.callback = callback;
    }
    
    public Timer(long server, long local, long time, String name, Callback callback){
	this(server, local, time, name, callback, 0);
    }
    
    public Timer(long server, long local, long time, String name){
	this(server, local, time, name, null);
    }
    
    public synchronized boolean update(long server, long local, long now){
	seconds = time - now + local - (server - start)/SERVER_RATIO;
	if(seconds <= 0){
	    if(callback != null){
		callback.finish(this);
	    }
	    return true;
	}
	if(callback != null){
		callback.update(this);
	    }
	return false;
    }
    
    public synchronized long getStart() {
        return start;
    }

    public synchronized void setStart(long start) {
        this.start = start;
    }

    public synchronized int getType() {
        return type;
    }

    public synchronized void setType(int type) {
        this.type = type;
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
	int h = (int) (seconds/3600);
	int m = (int) ((seconds%3600)/60);
	int s = (int) (seconds%60);
	return String.format("%d:%02d:%02d", h,m,s);
    }
    
}
