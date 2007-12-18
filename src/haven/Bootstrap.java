package haven;

import java.net.*;

public class Bootstrap extends Thread implements UI.Receiver {
	UI ui;
	Session sess;
	String address, defaddr;
	String username, password;
	int cfocus = 0;
	
	public Bootstrap(UI ui) {
		super("Haven bootstrap thread");
		this.ui = ui;
		ui.setreceiver(this);
		defaddr = "127.0.0.1";
	}
	
	public void setaddr(String addr) {
		defaddr = addr;
	}
	
	public void run() {
		ui.newwidget(5, "cnt", new Coord(0, 0), 0, new Coord(800, 600));
		ui.newwidget(4, "img", new Coord(0, 0), 5, "gfx/testimgs/snow.png");
		ui.newwidget(1, "text", new Coord(100, 100), 5, new Coord(100, 20), Utils.getpref("server", defaddr));
		ui.newwidget(2, "text", new Coord(100, 130), 5, new Coord(100, 20), Utils.getpref("username", ""));
		ui.newwidget(3, "text", new Coord(100, 160), 5, new Coord(100, 20), Utils.getpref("password", ""));
		ui.newwidget(6, "wnd", new Coord(400, 300), 0, new Coord(100, 50));
		ui.newwidget(7, "btn", new Coord (10, 10), 6, new Coord(50, 20), "Barda!");
		ui.uimsg(5, "tabfocus", 1);
		ui.uimsg(5, "act", 1);
		retry: do {
			address = null;
			username = null;
			password = null;
			synchronized(this) {
				while((address == null) || (username == null) || (password == null)) {
					try {
						this.wait();
					} catch(InterruptedException e) {
						return;
					}
				}
			}
			try {
				sess = new Session(InetAddress.getByName(address), username, password);
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
					Utils.setpref("server", address);
					Utils.setpref("username", username);
					Utils.setpref("password", password);
					ui.destroy(5);
					break retry;
				} else if(sess.connfailed != 0) {
					System.out.println("Failed: " + sess.connfailed);
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
		synchronized(this) {
			if((widget == 5) && (msg == "activate")) {
				if(cfocus == 2) {
					ui.uimsg(3, "settext", "");
					ui.uimsg(5, "focus", 3);
				} else {
					address = username = password = null;
					ui.uimsg(1, "get");
					ui.uimsg(2, "get");
					ui.uimsg(3, "get");
				}
			} else if((widget == 5) && (msg == "focus")) {
				cfocus = (Integer)args[0];
			} else if(widget == 7) {
				ui.destroy(6);
			}
			if(msg == "text") {
				if(widget == 1) {
					address = (String)args[0];
				} else if(widget == 2) {
					username = (String)args[0];
				} else if(widget == 3) {
					password = (String)args[0];
				}
			}
			if((address != null) && (username != null) && (password != null)) {
				if(username.equals("")) {
					ui.uimsg(5, "focus", 2);
					username = null;
				} else if(password.equals("")) {
					ui.uimsg(5, "focus", 3);
					password = null;
				}
				notifyAll();
			}
		}
	}
}
