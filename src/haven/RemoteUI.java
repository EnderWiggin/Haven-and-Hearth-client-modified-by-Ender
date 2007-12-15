package haven;

public class RemoteUI extends Thread implements UI.Receiver {
	Session sess;
	UI ui;
	
	public RemoteUI(Session sess, UI ui) {
		super("Remote Haven UI handler");
		this.sess = sess;
		this.ui = ui;
		ui.setreceiver(this);
	}
	
	public void rcvmsg(int id, String name, Object... args) {
		Message msg = new Message(Message.RMSG_WDGMSG);
		msg.adduint16(id);
		msg.addstring(name);
		msg.addlist(args);
		sess.queuemsg(msg);
	}
	
	public void run() {
		while(sess.connected) {
			Message msg;
			while((msg = sess.unqueuer()) != null) {
				if(msg.type == Message.RMSG_NEWWDG) {
					int id = msg.uint16();
					String type = msg.string();
					Coord c = msg.coord();
					int parent = msg.uint16();
					Object[] args = msg.list();
					ui.newwidget(id, type, c, parent, args);
				} else if(msg.type == Message.RMSG_WDGMSG) {
					int id = msg.uint16();
					String name = msg.string();
					ui.uimsg(id, name, msg.list());
				} else if(msg.type == Message.RMSG_DSTWDG) {
					int id = msg.uint16();
					ui.destroy(id);
				}
			}
			try {
				synchronized(sess) {
					sess.wait();
				}
			} catch(InterruptedException e) {
				break;
			}
		}
	}
}
