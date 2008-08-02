package haven;

import java.net.*;
import java.util.*;

public class Bootstrap implements UI.Receiver {
	UI ui;
	Session sess;
	String address;
	Queue<Message> msgs = new LinkedList<Message>();
	
	public static class Message {
		int id;
		String name;
		Object[] args;
		
		public Message(int id, String name, Object... args) {
			this.id = id;
			this.name = name;
			this.args = args;
		}
	}
	
	public Bootstrap() {
		address = "127.0.0.1";
	}
	
	public void setaddr(String addr) {
		address = addr;
	}
	
	public Session run(HavenPanel hp) throws InterruptedException {
		ui = hp.newui(null);
		ui.setreceiver(this);
		ui.bind(new LoginScreen(ui.root), 1);
		String username, password;
		boolean savepw = false;
		username = Utils.getpref("username", "");
		password = Utils.getpref("password", "");
		if(!password.equals(""))
			savepw = true;
		retry: do {
			ui.uimsg(1, "state", 0);
			ui.uimsg(1, "ld", username, password, savepw);
			while(true) {
				Message msg;
				synchronized(msgs) {
					while((msg = msgs.poll()) == null)
						msgs.wait();
				}
				if(msg.id == 1) {
					if(msg.name == "login") {
						username = (String)msg.args[0];
						password = (String)msg.args[1];
						break;
					} else if(msg.name == "savepw") {
						savepw = (Boolean)msg.args[0];
					}
				}
			}
			ui.uimsg(1, "state", 1);
			ui.uimsg(1, "prg", "Authenticating...");
			byte[] cookie;
			try {
			    AuthClient auth = new AuthClient(address, username);
			    if(!auth.trypasswd(password)) {
				auth.close();
				password = "";
				ui.uimsg(1, "error", "Username or password incorrect");
				continue retry;
			    }
			    cookie = auth.cookie;
			    auth.close();
			} catch(java.io.IOException e) {
			    ui.uimsg(1, "error", e.getMessage());
			    continue retry;
			}
			ui.uimsg(1, "prg", "Connecting...");
			try {
				sess = new Session(InetAddress.getByName(address), username, cookie);
			} catch(UnknownHostException e) {
				ui.uimsg(1, "error", "Could not locate server");
				continue retry;
			}
			Thread.sleep(100);
			while(true) {
				if(sess.state == "") {
					Utils.setpref("username", username);
					if(savepw)
						Utils.setpref("password", password);
					else
						Utils.setpref("password", "");
					ui.destroy(1);
					break retry;
				} else if(sess.connfailed != 0) {
					String error;
					switch(sess.connfailed) {
					case 1:
						error = "Username or password incorrect";
						password = "";
						break;
					case 2:
						error = "Already logged in";
						break;
					case 3:
						error = "Could not connect to server";
						break;
					case 4:
						error = "This client is too old";
						break;
					default:
						error = "Connection failed";
						break;
					}
					ui.uimsg(1, "error", error);
					sess = null;
					continue retry;
				}
				synchronized(sess) {
					sess.wait();
				}
			}
		} while(true);
		return(sess);
		//(new RemoteUI(sess, ui)).start();
	}
	
	public void rcvmsg(int widget, String msg, Object... args) {
		synchronized(msgs) {
			msgs.add(new Message(widget, msg, args));
			msgs.notifyAll();
		}
	}
}
