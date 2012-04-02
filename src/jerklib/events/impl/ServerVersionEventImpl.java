package jerklib.events.impl;

import jerklib.Session;
import jerklib.events.ServerVersionEvent;

/**
 * @author mohadib
 * @see ServerVersionEvent
 */
public class ServerVersionEventImpl implements ServerVersionEvent
{
	private final String comment, hostName, version, debugLevel, rawEventData;
	private final Session session;
	private final Type type = Type.SERVER_VERSION_EVENT;

	public ServerVersionEventImpl
	(
		String comment, 
		String hostName, 
		String version, 
		String debugLevel, 
		String rawEventData, 
		Session session
	)
	{
		super();
		this.comment = comment;
		this.hostName = hostName;
		this.version = version;
		this.debugLevel = debugLevel;
		this.rawEventData = rawEventData;
		this.session = session;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.ServerVersionEvent#getComment()
	 */
	public String getComment()
	{
		return comment;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.ServerVersionEvent#getHostName()
	 */
	public String getHostName()
	{
		return hostName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.ServerVersionEvent#getVersion()
	 */
	public String getVersion()
	{
		return version;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.ServerVersionEvent#getdebugLevel()
	 */
	public String getdebugLevel()
	{
		return debugLevel;
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
