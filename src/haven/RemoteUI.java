package haven;

public class RemoteUI implements UI.Receiver {
	Session sess;
	UI ui;
	
	public RemoteUI(Session sess) {
		this.sess = sess;
		Widget.initbardas();
	}
	
	public void rcvmsg(int id, String name, Object... args) {
		Message msg = new Message(Message.RMSG_WDGMSG);
		msg.adduint16(id);
		msg.addstring(name);
		msg.addlist(args);
		sess.queuemsg(msg);
	}
	
	public void run(HavenPanel hp) throws InterruptedException {
		ui = hp.newui(sess);
		ui.setreceiver(this);
		while(sess.alive()) {
			Message msg;
			while((msg = sess.getuimsg()) != null) {
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
			synchronized(sess) {
				sess.wait();
			}
		}
	}
}
