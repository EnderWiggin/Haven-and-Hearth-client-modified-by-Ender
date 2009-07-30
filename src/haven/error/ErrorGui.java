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
	
    public ErrorGui(java.awt.Frame parent) {
	super(parent, "Haven error!", true);
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

    public boolean goterror(Throwable t) {
	done = false;
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    add(vp, BorderLayout.SOUTH);
		    status.setText("An error has occurred! Do you wish to report it?");
		    pack();
		    setVisible(true);
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
	remove(vp);
	pack();
	if(!verified)
	    errorsent();
	return(verified);
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
