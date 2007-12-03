package haven;

import java.net.*;

public class Bootstrap extends Thread implements UI.Receiver {
	UI ui;
	Session sess;
	String address;
	
	public Bootstrap(UI ui) {
		this.ui = ui;
		ui.setreceiver(this);
	}
	
	public void run() {
		ui.newwidget(1, "text", new Coord(100, 100), 0, new Coord(100, 20), "192.168.0.116");
		ui.newwidget(2, "img", new Coord(0, 0), 0, "gfx/testimgs/snow.bmp");
		retry: do {
			address = null;
			synchronized(this) {
				while(address == null) {
					try {
						this.wait();
					} catch(InterruptedException e) {
						return;
					}
				}
			}
			try {
				sess = new Session(InetAddress.getByName(address));
			} catch(UnknownHostException e) {
				/* XXX */
				throw(new RuntimeException(e));
			}
			try {
				Thread.sleep(100);
			} catch(InterruptedException e) {
			}
			while(true) {
				if(sess.connected) {
					System.out.println("Connected!");
					ui.destroy(1);
					ui.destroy(2);
					break retry;
				} else if(sess.connfailed) {
					sess = null;
					continue retry;
				}
				try {
					synchronized(sess) {
						sess.wait();
					}
				} catch(InterruptedException e) {
					break;
				}
			}
		} while(true);
		(new RemoteUI(sess, ui)).start();
	}
	
	public void rcvmsg(int widget, String msg, Object... args) {
		if(widget == 1) {
			synchronized(this) {
				address = (String)args[0];
				notifyAll();
			}
		} else {
			System.out.println(widget + ": " + msg);
		}
	}
}
