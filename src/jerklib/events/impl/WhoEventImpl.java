package jerklib.events.impl;

import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.WhoEvent;

/**
 * Created: Jan 31, 2008 6:31:31 PM
 *
 * @author <a href="mailto:robby.oconnor@gmail.com">Robert O'Connor</a>
 * @see WhoEvent
 */
public class WhoEventImpl implements WhoEvent
{
    private final String nick, userName, realName, hostName, channel, rawEventData;
    private final String serverName;
    private final boolean isAway;
    private final int hopCount;
    private final Type type = IRCEvent.Type.WHO_EVENT;
    private final Session session;

    public WhoEventImpl(
            String channel, int hopCount, String hostName,
            boolean away, String nick, String rawEventData,
            String realName, String serverName, Session session, String userName
    )
    {
        this.channel = channel;
        this.hopCount = hopCount;
        this.hostName = hostName;
        isAway = away;
        this.nick = nick;
        this.rawEventData = rawEventData;
        this.realName = realName;
        this.serverName = serverName;
        this.session = session;
        this.userName = userName;
    }

    /**
     * Get the nick of the user
     *
     * @return the nick of the user.
     */
    public String getNick()
    {
        return nick;
    }

    /**
     * Get the username of the user
     *
     * @return the username
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Get the hostname of the user
     *
     * @return the hostname
     */
    public String getHostName()
    {
        return hostName;
    }

    /**
     * Get the real name of the user.
     *
     * @return the real name
     */
    public String getRealName()
    {
        return realName;
    }

    /**
     * Retrieve the channel (for when you WHO a channel)
     *
     * @return the channel or an empty String
     */
    public String getChannel()
    {
        return channel.equals("*") ? "" : channel;
    }

    /**
     * Get whether or not the user is away.
     *
     * @return whether or not the user is away.
     */
    public boolean isAway()
    {
        return isAway;
    }

    /**
     * Used to find out the exact type of event the IRCEvent object
     * is. The IRCEvent object can be cast into a more specific event object to
     * get access to convience methods for the specific event types.
     *
     * @return Type of event
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Returns the raw IRC data that makes up this event
     *
     * @return Raw IRC event text.
     */
    public String getRawEventData()
    {
        return rawEventData;
    }

    /* (non-Javadoc)
     * @see jerklib.events.WhoEvent#getHopCount()
     */
    public int getHopCount()
    {
        return hopCount;
    }

    /**
     * Gets session for connection
     *
     * @return Session
     */
    public Session getSession()
    {
        return session;
    }

    /* (non-Javadoc)
     * @see jerklib.events.WhoEvent#getServerName()
     */
    public String getServerName()
    {
        return serverName;
    }
}
