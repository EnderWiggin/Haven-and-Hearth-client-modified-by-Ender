package jerklib.events.impl;

import jerklib.Session;
import jerklib.events.ConnectionCompleteEvent;
import jerklib.events.IRCEvent;

/**
 * @author mohadib
 * @see ConnectionCompleteEvent
 *
 */
public class ConnectionCompleteEventImpl implements ConnectionCompleteEvent
{

	private final String rawEventData, hostName, oldHostName;
	private final Session session;

	public ConnectionCompleteEventImpl(String rawEventData, String hostName, Session session, String oldHostName)
	{

		this.rawEventData = rawEventData;
		this.hostName = hostName;
		this.session = session;
		this.oldHostName = oldHostName;

	}

	/* (non-Javadoc)
	 * @see jerklib.events.ConnectionCompleteEvent#getOldHostName()
	 */
	public String getOldHostName()
	{
		return oldHostName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.IRCEvent#getType()
	 */
	public Type getType()
	{
		return IRCEvent.Type.CONNECT_COMPLETE;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.IRCEvent#getRawEventData()
	 */
	public String getRawEventData()
	{
		return rawEventData;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.ConnectionCompleteEvent#getActualHostName()
	 */
	public String getActualHostName()
	{
		return hostName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.IRCEvent#getSession()
	 */
	public Session getSession()
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
