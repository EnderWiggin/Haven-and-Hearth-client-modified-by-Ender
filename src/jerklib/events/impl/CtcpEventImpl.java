package jerklib.events.impl;

import jerklib.Channel;
import jerklib.Session;
import jerklib.events.CtcpEvent;

/**
 * @author mohadib
 * @see CtcpEvent
 *
 */
public class CtcpEventImpl implements CtcpEvent
{

	private String ctcpString, hostName, message, nick, userName, rawEventData;
	private Channel channel;
	private Session session;

	public CtcpEventImpl
	(
		String ctcpString, 
		String hostName, 
		String message, 
		String nick, 
		String userName, 
		String rawEventData, 
		Channel channel, 
		Session session
	)
	{
		super();
		this.ctcpString = ctcpString;
		this.hostName = hostName;
		this.message = message;
		this.nick = nick;
		this.userName = userName;
		this.rawEventData = rawEventData;
		this.channel = channel;
		this.session = session;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.CtcpEvent#getCtcpString()
	 */
	public String getCtcpString()
	{
		return ctcpString;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.MessageEvent#getChannel()
	 */
	public Channel getChannel()
	{
		return channel;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.MessageEvent#getHostName()
	 */
	public String getHostName()
	{
		return hostName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.MessageEvent#getMessage()
	 */
	public String getMessage()
	{
		return message;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.MessageEvent#getNick()
	 */
	public String getNick()
	{
		return nick;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.MessageEvent#getUserName()
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
		return Type.CTCP_EVENT;
	}

}
