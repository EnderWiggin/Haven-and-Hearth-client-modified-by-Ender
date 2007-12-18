package haven.error;

import java.awt.*;
import java.awt.event.*;

public abstract class ErrorGui extends Frame implements ErrorStatus {
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
