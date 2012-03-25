package jerklib.events.impl;

import jerklib.Session;
import jerklib.events.AwayEvent;
import jerklib.events.IRCEvent;

public class AwayEventImpl implements AwayEvent
{

    private final boolean isAway, isYou;
    private final String rawEventData, awayMessage, nick;
    private final Session session;
    private Type type = IRCEvent.Type.AWAY_EVENT;
    private EventType eventType;

    public AwayEventImpl(String awayMessage, EventType eventType, boolean away, boolean you, String nick, String rawEventData, Session session)
    {
        this.awayMessage = awayMessage;
        this.eventType = eventType;
        isAway = away;
        isYou = you;
        this.nick = nick;
        this.rawEventData = rawEventData;
        this.session = session;
    }

    public AwayEventImpl(Session session, EventType eventType, boolean away, boolean you, String nick, String rawEventData)
    {
        this.awayMessage = "";
        this.session = session;
        this.eventType = eventType;
        isAway = away;
        isYou = you;
        this.nick = nick;
        this.rawEventData = rawEventData;
    }

    /* (non-Javadoc)
     * @see jerklib.events.AwayEvent#getAwayMessage()
     */
    public String getAwayMessage()
    {
        return awayMessage;
    }

    /* (non-Javadoc)
     * @see jerklib.events.AwayEvent#isAway()
     */
    public boolean isAway()
    {
        return isAway;
    }

    /* (non-Javadoc)
     * @see jerklib.events.AwayEvent#isYou()
     */
    public boolean isYou()
    {
        return isYou;
    }

    /* (non-Javadoc)
     * @see jerklib.events.AwayEvent#getNick()
     */
    public String getNick()
    {
        return nick;
    }

    /* (non-Javadoc)
     * @see jerklib.events.IRCEvent#getRawEventData()
     */
    public String getRawEventData()
    {
        return rawEventData;
    }

    /* (non-Javadoc)
     * @see jerklib.events.IRCEvent#getSession()
     */
    public Session getSession()
    {
        return session;
    }

    /* (non-Javadoc)
     * @see jerklib.events.IRCEvent#getType()
     */
    public Type getType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see jerklib.events.AwayEvent#getEventType()
     */
    public EventType getEventType()
    {
        return eventType;
    }

}