package haven;

import java.net.*;
import java.util.*;

public class Bootstrap implements UI.Receiver {
    UI ui;
    Session sess;
    String address;
    Queue<Message> msgs = new LinkedList<Message>();
    byte[] initcookie = null;
	
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
    
    public void setinitcookie(byte[] cookie) {
	initcookie = cookie;
    }
	
    public void setaddr(String addr) {
	address = addr;
    }
	
    public Session run(HavenPanel hp) throws InterruptedException {
	ui = hp.newui(null);
	ui.setreceiver(this);
	ui.bind(new LoginScreen(ui.root), 1);
	String username;
	boolean savepw = false;
	Utils.setpref("password", "");
	byte[] token = null;
	if(Utils.getpref("savedtoken", "").length() == 64)
	    token = Utils.hex2byte(Utils.getpref("savedtoken", null));
	username = Utils.getpref("username", "");
	String authserver = System.getProperty("haven.authserv", address);
	retry: do {
	    byte[] cookie;
	    if(initcookie != null) {
		cookie = initcookie;
		initcookie = null;
	    } else if(token != null) {
		savepw = true;
		ui.uimsg(1, "token", username);
		while(true) {
		    Message msg;
		    synchronized(msgs) {
			while((msg = msgs.poll()) == null)
			    msgs.wait();
		    }
		    if(msg.id == 1) {
			if(msg.name == "login") {
			    break;
			} else if(msg.name == "forget") {
			    token = null;
			    Utils.setpref("savedtoken", "");
			    continue retry;
			}
		    }
		}
		ui.uimsg(1, "prg", "Authenticating...");
		AuthClient auth = null;
		try {
		    auth = new AuthClient(authserver, username);
		    if(!auth.trytoken(token)) {
			auth.close();
			token = null;
			Utils.setpref("savedtoken", "");
			ui.uimsg(1, "error", "Invalid save");
			continue retry;
		    }
		    cookie = auth.cookie;
		} catch(java.io.IOException e) {
		    ui.uimsg(1, "error", e.getMessage());
		    continue retry;
		} finally {
		    try {
			if(auth != null)
			    auth.close();
		    } catch(java.io.IOException e) {}
		}
	    } else {
		String password;
		ui.uimsg(1, "passwd", username, savepw);
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
			    savepw = (Boolean)msg.args[2];
			    break;
			}
		    }
		}
		ui.uimsg(1, "prg", "Authenticating...");
		AuthClient auth = null;
		try {
		    auth = new AuthClient(authserver, username);
		    if(!auth.trypasswd(password)) {
			auth.close();
			password = "";
			ui.uimsg(1, "error", "Username or password incorrect");
			continue retry;
		    }
		    cookie = auth.cookie;
		    if(savepw) {
			if(auth.gettoken())
			    Utils.setpref("savedtoken", Utils.byte2hex(auth.token));
		    }
		} catch(java.io.IOException e) {
		    ui.uimsg(1, "error", e.getMessage());
		    continue retry;
		} finally {
		    try {
			if(auth != null)
			    auth.close();
		    } catch(java.io.IOException e) {}
		}
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
		    ui.destroy(1);
		    break retry;
		} else if(sess.connfailed != 0) {
		    String error;
		    switch(sess.connfailed) {
		    case 1:
			error = "Invalid authentication token";
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
		    case 5:
			error = "Authentication token expired";
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
	haven.error.ErrorHandler.setprop("usr", sess.username);
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
