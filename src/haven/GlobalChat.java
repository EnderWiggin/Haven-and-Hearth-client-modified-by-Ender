package haven;

import jerklib.ConnectionManager;
import jerklib.Profile;
import jerklib.Session;
import jerklib.events.*;
import jerklib.events.IRCEvent.Type;
import jerklib.listeners.IRCEventListener;

/**
 *  A simple example that demonsrates how to use JerkLib
 *  @author mohadib 
 */
public class GlobalChat implements IRCEventListener
{
    private ConnectionManager manager;
    public IRChatHW chhw = null;
    boolean bodyisready = false;
    JoinCompleteEvent ircjce;
    String charname;

    public GlobalChat(IRChatHW chhw, String nick)
    {
	/*
	 * ConnectionManager takes a Profile to use for new connections.
	 */
	nick = nick.replaceAll(" ", "_");
	charname = nick;
	manager = new ConnectionManager(new Profile(nick));
	chhw.gcrcv("Connecting to global chat...");

	/*
	 * One instance of ConnectionManager can connect to many IRC networks.
	 * ConnectionManager#requestConnection(String) will return a Session object.
	 * The Session is the main way users will interact with this library and IRC
	 * networks
	 */
	Session session = manager.requestConnection("irc.synirc.net");

	/*
	 * JerkLib fires IRCEvents to notify users of the lib of incoming events
	 * from a connected IRC server.
	 */
	session.addIRCEventListener(this);
	this.chhw = chhw;

    }

    /*
     * This method is for implementing an IRCEventListener. This method will be
     * called anytime Jerklib parses an event from the Session its attached to.
     * All events are sent as IRCEvents. You can check its actual type and cast it
     * to a more specific type.
     */
    public void receiveEvent(IRCEvent e)
    {


	if (e.getType() == Type.CONNECT_COMPLETE)
	{
	    e.getSession().join("#haven2");
	}
	else if (e.getType() == Type.CHANNEL_MESSAGE)
	{
	    MessageEvent me = (MessageEvent) e;
	    chhw.gcrcv(me.getNick() + ": " + me.getMessage());
	}
	else if (e.getType() == Type.JOIN_COMPLETE)
	{
	    ircjce = (JoinCompleteEvent) e;
	    bodyisready = true;
	    chhw.gcrcv("Global chat is ready.");
	}
	else
	{
	    // System.out.println(e.getType() + " " + e.getRawEventData());
	}
    }
    
    public void close(String quitMessage){
	if(ircjce == null || ircjce.getSession() == null) return;
	ircjce.getSession().close(quitMessage);
	chhw = null;
	ircjce = null;
    }
    
    public void gcsnd (String text) {
	if (bodyisready) {
	    chhw.gcrcv(charname + ": " + text);
	    ircjce.getChannel().say(text);
	}
    }
}