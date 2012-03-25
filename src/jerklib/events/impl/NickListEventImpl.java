package jerklib.events.impl;

import jerklib.Channel;
import jerklib.Session;
import jerklib.events.NickListEvent;

import java.util.List;

/**
 * @author NickListEventImpl
 *
 */
public class NickListEventImpl implements NickListEvent
{
	private final Type type = Type.NICK_LIST_EVENT;
	private final List<String> nicks;
	private final Channel channel;
	private final String rawEventData;
	private final Session session;

	public NickListEventImpl(String rawEventData, Session session, Channel channel, List<String> nicks)
	{
		this.rawEventData = rawEventData;
		this.session = session;
		this.channel = channel;
		this.nicks = nicks;

	}

	/* (non-Javadoc)
	 * @see jerklib.events.NickListEvent#getChannel()
	 */
	public Channel getChannel()
	{
		return channel;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.NickListEvent#getNicks()
	 */
	public List<String> getNicks()
	{
		return nicks;
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
