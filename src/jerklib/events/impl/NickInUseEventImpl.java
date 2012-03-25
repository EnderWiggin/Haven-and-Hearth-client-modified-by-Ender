package jerklib.events.impl;

import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.NickInUseEvent;

/**
 * @author mohadib
 * @see NickInUseEvent
 *
 */
public class NickInUseEventImpl implements NickInUseEvent
{

	private final String inUseNick, rawEventData;
	private final Session session;
	private final IRCEvent.Type type = IRCEvent.Type.NICK_IN_USE;

	public NickInUseEventImpl(String inUseNick, String rawEventData, Session session)
	{
		this.inUseNick = inUseNick;
		this.rawEventData = rawEventData;
		this.session = session;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.NickInUseEvent#getInUseNick()
	 */
	public String getInUseNick()
	{
		return inUseNick;
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

}
