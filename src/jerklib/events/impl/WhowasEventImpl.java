package jerklib.events.impl;

import jerklib.Session;
import jerklib.events.WhowasEvent;

/**
 * @author mohadib
 * @see WhowasEvent
 *
 */
public class WhowasEventImpl implements WhowasEvent
{
	private final Type type = Type.WHOWAS_EVENT;
	private final String hostName, userName, nick, realName, rawEventData;
	private final Session session;

	public WhowasEventImpl
	(
		String hostName, 
		String userName, 
		String nick, 
		String realName, 
		String rawEventData, 
		Session session
	)
	{
		this.hostName = hostName;
		this.userName = userName;
		this.nick = nick;
		this.realName = realName;
		this.rawEventData = rawEventData;
		this.session = session;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhowasEvent#getHostName()
	 */
	public String getHostName()
	{
		return hostName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhowasEvent#getNick()
	 */
	public String getNick()
	{
		return nick;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhowasEvent#getRealName()
	 */
	public String getRealName()
	{
		return realName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhowasEvent#getUserName()
	 */
	public String getUserName()
	{
		return userName;
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
