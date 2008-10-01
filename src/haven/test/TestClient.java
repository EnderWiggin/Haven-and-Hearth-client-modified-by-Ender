package haven.test;

import haven.*;
import java.util.*;
import java.net.InetAddress;

public class TestClient implements Runnable {
    public Session sess;
    public InetAddress addr;
    public String user;
    public byte[] cookie;
    public ThreadGroup tg;
    public Thread me;
    public UI ui;
    public boolean loop = false;
    public Collection<Robot> robots = new HashSet<Robot>();
    private static Object errsync = new Object();
    
    public TestClient(String user) {
	try {
	    addr = InetAddress.getByName("localhost");
	} catch(java.net.UnknownHostException e) {
	    throw(new RuntimeException("localhost not known"));
	}
	this.user = user;
	this.cookie = new byte[64];
	tg = new ThreadGroup(Utils.tg(), "Test client") {
		public void uncaughtException(Thread t, Throwable e) {
		    synchronized(errsync) {
			System.err.println("Exception in test client: " + TestClient.this.user);
			e.printStackTrace(System.err);
		    }
		    TestClient.this.stop();
		}
	    };
    }
    
    public void connect() throws InterruptedException {
	sess = new Session(addr, user, cookie);
	synchronized(sess) {
	    while(sess.state != "") {
		if(sess.connfailed != 0)
		    throw(new RuntimeException("Connection failure for " + user + " (" + sess.connfailed + ")"));
		sess.wait();
	    }
	}
    }
    
    public void addbot(Robot bot) {
	synchronized(robots) {
	    robots.add(bot);
	}
    }
    
    public void rembot(Robot bot) {
	synchronized(robots) {
	    robots.remove(bot);
	}
    }
    
    public class TestUI extends UI {
	public TestUI(Coord sz, Session sess) {
	    super(sz, sess);
	}
	
	public void newwidget(int id, String type, Coord c, int parent, Object... args) throws InterruptedException {
	    super.newwidget(id, type, c, parent, args);
	    Widget w = widgets.get(id);
	    synchronized(robots) {
		for(Robot r : robots)
		    r.newwdg(id, w, args);
	    }
	}
	
	public void destroy(Widget w) {
	    int id;
	    if(!rwidgets.containsKey(w))
		id = -1;
	    else
		id = rwidgets.get(w);
	    synchronized(robots) {
		for(Robot r : robots)
		    r.dstwdg(id, w);
	    }
	    super.destroy(w);
	}
	
	public void uimsg(int id, String msg, Object... args) {
	    Widget w = widgets.get(id);
	    synchronized(robots) {
		for(Robot r : robots)
		    r.uimsg(id, w, msg, args);
	    }
	    super.uimsg(id, msg, args);
	}
    }

    public void run() {
	try {
	    try {
		do {
		    connect();
		    RemoteUI rui = new RemoteUI(sess);
		    ui = new TestUI(new Coord(800, 600), sess);
		    rui.run(ui);
		} while(loop);
	    } catch(InterruptedException e) {
	    }
	} finally {
	    stop();
	}
    }
    
    public void start() {
	me = new Thread(tg, this, "Main thread");
	me.start();
    }
    
    public void stop() {
	tg.interrupt();
    }
    
    public boolean alive() {
	return((me != null) && me.isAlive());
    }
    
    public void join() {
	while(alive()) {
	    try {
		me.join();
	    } catch(InterruptedException e) {
		tg.interrupt();
	    }
	}
    }
    
    public String toString() {
	return("Client " + user);
    }
}
