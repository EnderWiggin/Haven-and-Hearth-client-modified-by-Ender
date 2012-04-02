package haven;


public class IRChatHW extends ChatHW {

    private static Object instance;
    private GlobalChat gc = null;
    
    public IRChatHW(String title) {
	super(null, title, true);
	gc = new GlobalChat(this, ui.sess.charname);
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if((sender == in) && (msg.equals("activate"))){
	    gc.gcsnd(args[0].toString());
	    in.settext("");
	} else if(sender == cbtn){
	    ui.destroy(this);
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    public void gcrcv(String text) {
	if(Config.timestamp)
	    text = Utils.timestamp() + text;
	out.append(text);
    }

    @Override
    public void destroy() {
	gc.close("quit message");
	gc = null;
	instance = null;
	super.destroy();
    }

    public static void open() {
	if (instance == null) {
	    instance = new IRChatHW("Global");
	}
    }

}
