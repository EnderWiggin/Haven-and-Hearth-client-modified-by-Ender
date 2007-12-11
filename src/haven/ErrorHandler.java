package haven;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Queue;
import java.util.LinkedList;

public class ErrorHandler extends ThreadGroup {
    private static final URL errordest;
    private final ThreadGroup initial;
    private Reporter reporter;
	
    static {
	try {
	    errordest = new URL("http://localhost/~fredrik/haven/error");
	} catch(MalformedURLException e) {
	    throw(new Error(e));
	}
    }

    private class Reporter extends Thread {
	private Queue<Throwable> errors = new LinkedList<Throwable>();
	private final ErrorStatus status;
	
	public Reporter(ErrorStatus status) {
	    super(initial, "Error reporter");
	    setDaemon(true);
	    this.status = status;
	}
	
	public void run() {
	    while(true) {
		synchronized(errors) {
		    try {
			errors.wait();
		    } catch(InterruptedException e) {
			return;
		    }
		    Throwable t;
		    while((t = errors.poll()) != null) {
			try {
			    doreport(t);
			} catch(IOException e) {
			    status.senderror(e);
			}
		    }
		}
	    }
	}
	
	private void doreport(Throwable t) throws IOException {
	    status.goterror(t);
	    URLConnection c = errordest.openConnection();
	    status.connecting();
	    c.setDoOutput(true);
	    c.addRequestProperty("Content-Type", "application/x-java-error");
	    c.connect();
	    ObjectOutputStream o = new ObjectOutputStream(c.getOutputStream());
	    o.writeObject(t);
	    o.close();
	    status.sending();
	    InputStream i = c.getInputStream();
	    byte[] buf = new byte[1024];
	    while(i.read(buf) >= 0);
	    i.close();
	    status.done();
	}
    
	public void report(Throwable t) {
	    synchronized(errors) {
		errors.add(t);
		errors.notifyAll();
	    }
	}
    }

    private abstract class ErrorGui extends Frame implements ErrorStatus {
	private Label status;
	private Panel vp, dp;
	private boolean verified, done;
	
	public ErrorGui() {
	    super("Haven error!");
	    setMinimumSize(new Dimension(300, 100));
	    setResizable(false);
	    setLayout(new BorderLayout());
	    add(status = new Label(""), BorderLayout.CENTER);
	    
	    vp = new Panel();
	    vp.setLayout(new FlowLayout());
	    Button b;
	    vp.add(b = new Button("Yes"));
	    b.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			synchronized(ErrorGui.this) {
			    verified = true;
			    done = true;
			    ErrorGui.this.notifyAll();
			}
		    }
		});
	    vp.add(b = new Button("No"));
	    b.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			synchronized(ErrorGui.this) {
			    verified = false;
			    done = true;
			    ErrorGui.this.notifyAll();
			}
		    }
		});
	    dp = new Panel();
	    dp.setLayout(new FlowLayout());
	    dp.add(b = new Button("OK"));
	    b.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			synchronized(ErrorGui.this) {
			    done = true;
			    ErrorGui.this.notifyAll();
			}
		    }
		});
	}

	public void goterror(Throwable t) {
	    done = false;
	    add(vp, BorderLayout.SOUTH);
	    status.setText("An error has occurred! Do you wish to report it?");
	    pack();
	    setVisible(true);
	    synchronized(this) {
		try {
		    while(!done)
			wait();
		} catch(InterruptedException e) {
		    throw(new Error(e));
		}
	    }
	    if(!verified)
		errorsent();
	}
	
	public void connecting() {
	    status.setText("Connecting to server");
	    pack();
	}
	
	public void sending() {
	    status.setText("Sending error");
	    pack();
	}
	
	public void done() {
	    done = false;
	    remove(vp);
	    add(dp, BorderLayout.SOUTH);
	    status.setText("Done");
	    pack();
	    synchronized(this) {
		try {
		    while(!done)
			wait();
		} catch(InterruptedException e) {
		    throw(new Error(e));
		}
	    }
	    errorsent();
	}
	
	public void senderror(Exception e) {
	    e.printStackTrace();
	    done = false;
	    remove(vp);
	    add(dp, BorderLayout.SOUTH);
	    status.setText("An error occurred while sending!");
	    pack();
	    synchronized(this) {
		try {
		    while(!done)
			wait();
		} catch(InterruptedException e2) {
		    throw(new Error(e2));
		}
	    }
	    errorsent();
	}
	
	public abstract void errorsent();
    }
    
    private interface ErrorStatus {
	public void goterror(Throwable t);
	public void connecting();
	public void sending();
	public void done();
	public void senderror(Exception e);
    }

    public ErrorHandler(Runnable main) {
	super("Haven client");
	initial = Thread.currentThread().getThreadGroup();
	reporter = new Reporter(new ErrorGui() {
		public void errorsent() {
		    System.exit(1);
		}
	    });
	reporter.start();
	Thread init = new Thread(this, main, "Main error handled thread");
	init.start();
    }
    
    public void uncaughtException(Thread t, Throwable e) {
	reporter.report(e);
    }
}
