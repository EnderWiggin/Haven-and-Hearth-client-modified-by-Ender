package jerklib.events.impl;

import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.InviteEvent;

/**
 *@see InviteEvent
 */
public class InviteEventImpl implements InviteEvent
{
    private final String nick, channel, rawEventData, userName, hostName;
    private Type type = IRCEvent.Type.INVITE_EVENT;
    private Session session;

    public InviteEventImpl(String channel, String nick, String userName, String hostName, String rawEventData, Session session)
    {
        this.channel = channel;
        this.nick = nick;
        this.userName = userName;
        this.hostName = hostName;
        this.rawEventData = rawEventData;
        this.session = session;
    }

    /**
     * getType() is used to find out the exact type of event the IRCEvent object
     * is. The IRCEvent object can be cast into a more specific event object to
     * get access to convience methods for the specific event types.
     *
     * @return <code>Type</code> enum for event.
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Gets the channel to which we were invited to
     *
     * @return the channel we were invited to.
     */
    public String getChannelName()
    {
        return channel;
    }

    /**
     * Gets the nick of the person who invited us
     *
     * @return the nick of the person who invited us
     */
    public String getNick()
    {
        return nick;
    }

    /* (non-Javadoc)
     * @see jerklib.events.InviteEvent#getHostName()
     */
    public String getHostName()
    {
        return hostName;
    }

    /* (non-Javadoc)
     * @see jerklib.events.InviteEvent#getUserName()
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * getRawEventData() returns the raw IRC data that makes up this event
     *
     * @return <code>String</code> Raw IRC event text.
     */
    public String getRawEventData()
    {
        return rawEventData;
    }

    public Session getSession()
    {
        return session;
    }
}
