/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven.error;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public abstract class ErrorGui extends JDialog implements ErrorStatus {
    private JLabel status;
    private JPanel details;
    private JButton closebtn, detbtn;
    private JTextArea exbox;
    private Thread reporter;
    private boolean done;
    
    public ErrorGui(java.awt.Frame parent) {
	super(parent, "Haven error!", true);
	setMinimumSize(new Dimension(300, 100));
	setResizable(false);
	add(new JPanel() {{
	    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	    add(new JLabel("An error has occurred!"));
	    add(status = new JLabel("Please wait..."));
	    add(new JPanel() {{
		setLayout(new FlowLayout());
		setAlignmentX(0);
		add(closebtn = new JButton("Close") {{
		    addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ev) {
				ErrorGui.this.dispose();
				synchronized(ErrorGui.this) {
				    done = true;
				    ErrorGui.this.notifyAll();
				}
			    }
			});
		}});
		add(detbtn = new JButton("Details >>>") {{
		    addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ev) {
				if(details.isVisible()) {
				    details.setVisible(false);
				    detbtn.setText("Details >>>");
				} else {
				    details.setVisible(true);
				    detbtn.setText("<<< Details");
				}
				ErrorGui.this.pack();
			    }
			});
		}});
	    }});
	    add(details = new JPanel() {{
		setLayout(new BorderLayout());
		setAlignmentX(0);
		setVisible(false);
		add(exbox = new JTextArea() {{
		    setEditable(false);
		}});
	    }});
	}});
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent ev) {
		    ErrorGui.this.dispose();
		    synchronized(ErrorGui.this) {
			done = true;
			ErrorGui.this.notifyAll();
		    }
		    reporter.interrupt();
		}
	    });
	pack();
    }
    
    public boolean goterror(Throwable t) {
	reporter = Thread.currentThread();
	java.io.StringWriter w = new java.io.StringWriter();
	t.printStackTrace(new java.io.PrintWriter(w));
	final String tr = w.toString();
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    closebtn.setEnabled(false);
		    status.setText("Please wait...");
		    exbox.setText(tr);
		    pack();
		    setVisible(true);
		}
	    });
	return(true);
    }
	
    public void connecting() {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    status.setText("Connecting to server...");
		    pack();
		}
	    });
    }
	
    public void sending() {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    status.setText("Sending error...");
		    pack();
		}
	    });
    }
	
    public void done() {
	done = false;
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    closebtn.setEnabled(true);
		    status.setText("The error has been reported.");
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
		    closebtn.setEnabled(true);
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
