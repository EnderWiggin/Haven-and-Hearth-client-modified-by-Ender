package jerklib.events.impl;

import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.NickChangeEvent;

/**
 * @author mohadib
 * @see NickChangeEvent
 *
 */
public class NickChangeEventImpl implements NickChangeEvent
{

	private final Type type = IRCEvent.Type.NICK_CHANGE;
	private final String rawEventData, oldNick, newNick, hostName, userName;
	private final Session session;

	public NickChangeEventImpl
	(
		String rawEventData, 
		Session session, 
		String oldNick, 
		String newNick, 
		String hostName, 
		String userName
	)
	{
		this.rawEventData = rawEventData;
		this.session = session;
		this.oldNick = oldNick;
		this.newNick = newNick;
		this.hostName = hostName;
		this.userName = userName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.NickChangeEvent#getOldNick()
	 */
	public final String getOldNick()
	{
		return oldNick;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.NickChangeEvent#getNewNick()
	 */
	public final String getNewNick()
	{
		return newNick;
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
	 * @see jerklib.events.NickChangeEvent#getHostName()
	 */
	public String getHostName()
	{
		return hostName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.NickChangeEvent#getUserName()
	 */
	public String getUserName()
	{
		return userName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return rawEventData;
	}
}
