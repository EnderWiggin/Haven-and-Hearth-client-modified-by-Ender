package haven;

import java.net.*;

public class Bootstrap extends Thread implements UI.Receiver {
	UI ui;
	Session sess;
	String address, defaddr;
	String username, password;
	boolean servlist;
	int cfocus = 0;
	
	public Bootstrap(UI ui, boolean servlist) {
		super(Utils.tg(), "Haven bootstrap thread");
		this.ui = ui;
		ui.setreceiver(this);
		defaddr = "127.0.0.1";
		this.servlist = servlist;
	}
	
	public void setaddr(String addr) {
		defaddr = addr;
	}
	
	public void run() {
		ui.newwidget(5, "cnt", new Coord(0, 0), 0, new Coord(800, 600));
		ui.uimsg(5, "tabfocus", 1);
		ui.newwidget(4, "img", new Coord(0, 0), 5, "gfx/loginscr.gif");
		//ui.newwidget(1, "text", new Coord(100, 100), 5, new Coord(100, 20), defaddr);
		address = defaddr;
		if(servlist) {
			ui.newwidget(1, "lb", new Coord(50, 50), 5, new Object[] {new Coord(200, 300),
				"127.0.0.1", "localhost",
				"192.168.0.116", "dolda",
				"192.168.0.144", "server",
				"sh.seatribe.se", "Seatribe"
				});
			ui.uimsg(1, "act", 1);
		}
		ui.newwidget(2, "text", new Coord(345, 330), 5, new Coord(150, 20), Utils.getpref("username", ""));
		ui.newwidget(3, "text", new Coord(345, 390), 5, new Coord(150, 20), Utils.getpref("password", ""));
		ui.uimsg(3, "pw", 1);
		ui.newwidget(4, "ibtn", new Coord(373, 430), 5, "gfx/hud/buttons/loginu.gif", "gfx/hud/buttons/logind.gif");
		ui.uimsg(5, "act", 1);
		ui.uimsg(5, "focus", 1);
		retry: do {
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
				return;
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
					return;
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
					username = password = null;
					ui.uimsg(2, "get");
					ui.uimsg(3, "get");
				}
			} if((widget == 4) && (msg == "activate")) {
				ui.uimsg(2, "get");
				ui.uimsg(3, "get");
			} else if((widget == 5) && (msg == "focus")) {
				cfocus = (Integer)args[0];
			} else if(widget == 7) {
				ui.destroy(6);
			}
			if(msg == "chose") {
				if(widget == 1)
					address = (String)args[0];
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
