package jerklib.events.impl;


import jerklib.Channel;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.JoinCompleteEvent;


/**
 * @see JoinCompleteEvent
 * @author mohadib
 */
public class JoinCompleteEventImpl implements JoinCompleteEvent
{

    private final String rawEventData;
    private final Type type = IRCEvent.Type.JOIN_COMPLETE;
    private Session session;
    private final Channel channel;

    public JoinCompleteEventImpl(String rawEventData, Session session, Channel channel)
    {
        this.rawEventData = rawEventData;
        this.session = session;
        this.channel = channel;
    }

    /* (non-Javadoc)
     * @see jerklib.events.JoinCompleteEvent#getChannel()
     */
    public final Channel getChannel()
    {
        return channel;
    }

    /* (non-Javadoc)
     * @see jerklib.events.IRCEvent#getType()
     */
    public final Type getType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see jerklib.events.IRCEvent#getRawEventData()
     */
    public final String getRawEventData()
    {
        return rawEventData;
    }

    /* (non-Javadoc)
     * @see jerklib.events.IRCEvent#getSession()
     */
    public final Session getSession()
    {
        return session;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return rawEventData;
    }

}
