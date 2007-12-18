package haven.error;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dialog;
import javax.swing.*;
import java.awt.event.*;

public abstract class ErrorGui extends JDialog implements ErrorStatus {
    private JLabel status;
    private JPanel vp, dp;
    private boolean verified, done;
	
    public ErrorGui(java.awt.Window parent) {
	super(parent, "Haven error!", Dialog.ModalityType.APPLICATION_MODAL);
	setMinimumSize(new Dimension(300, 100));
	setResizable(false);
	setLayout(new BorderLayout());
	add(status = new JLabel(""), BorderLayout.CENTER);
	    
	vp = new JPanel();
	vp.setLayout(new FlowLayout());
	JButton b;
	vp.add(b = new JButton("Yes"));
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    synchronized(ErrorGui.this) {
			verified = true;
			done = true;
			ErrorGui.this.notifyAll();
		    }
		}
	    });
	vp.add(b = new JButton("No"));
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    synchronized(ErrorGui.this) {
			verified = false;
			done = true;
			ErrorGui.this.notifyAll();
		    }
		}
	    });
	dp = new JPanel();
	dp.setLayout(new FlowLayout());
	dp.add(b = new JButton("OK"));
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
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    add(vp, BorderLayout.SOUTH);
		    status.setText("An error has occurred! Do you wish to report it?");
		    pack();
		    show();
		}
	    });
	synchronized(this) {
	    try {
		while(!done) {
		    wait();
		}
	    } catch(InterruptedException e) {
		throw(new Error(e));
	    }
	}
	if(!verified)
	    errorsent();
    }
	
    public void connecting() {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    status.setText("Connecting to server");
		    pack();
		}
	    });
    }
	
    public void sending() {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    status.setText("Sending error");
		    pack();
		}
	    });
    }
	
    public void done() {
	done = false;
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    remove(vp);
		    add(dp, BorderLayout.SOUTH);
		    status.setText("Done");
		    pack();
		}
	    });
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
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    remove(vp);
		    add(dp, BorderLayout.SOUTH);
		    status.setText("An error occurred while sending!");
		    pack();
		}
	    });
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
