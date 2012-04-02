package jerklib.events.impl;

import jerklib.ServerInformation;
import jerklib.Session;
import jerklib.events.ServerInformationEvent;

/**
 * @author mohadib
 * @see ServerInformationEvent
 *
 */
public class ServerInformationEventImpl implements ServerInformationEvent
{

	private final Session session;
	private final String rawEventData;
	private final ServerInformation serverInfo;

	public ServerInformationEventImpl(Session session, String rawEventData, ServerInformation serverInfo)
	{
		this.session = session;
		this.rawEventData = rawEventData;
		this.serverInfo = serverInfo;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.ServerInformationEvent#getServerInformation()
	 */
	public ServerInformation getServerInformation()
	{
		return serverInfo;
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
		return Type.SERVER_INFORMATION;
	}

}
