package jerklib.events.impl;

import jerklib.Channel;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.MessageEvent;

/**
 * @author mohadib
 * @see MessageEvent
 * 
 */
public class MessageEventImpl implements MessageEvent
{

	private final String nick, userName, hostName, message, rawEventData;
	private final Channel channel;
	private final Type type;
	private final Session session;

	public MessageEventImpl
	(
		Channel channel, 
		String hostName, 
		String message, 
		String nick, 
		String rawEventData, 
		Session session, 
		IRCEvent.Type type, 
		String userName
	)
	{
		this.channel = channel;
		this.hostName = hostName;
		this.message = message;
		this.nick = nick;
		this.rawEventData = rawEventData;
		this.session = session;
		this.type = type;
		this.userName = userName;
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
	 * @see jerklib.events.IRCEvent#getRawEventData()
	 */
	public String getRawEventData()
	{
		return rawEventData;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.IRCEvent#getType()
	 */
	public Type getType()
	{
		return type;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.MessageEvent#getUserName()
	 */
	public String getUserName()
	{
		return userName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.IRCEvent#getSession()
	 */
	public Session getSession()
	{
		return session;
	}
}
