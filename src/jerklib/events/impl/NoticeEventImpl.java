package jerklib.events.impl;

import jerklib.Channel;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.NoticeEvent;

/**
 * @author mohadib
 * @see NoticeEventImpl
 *
 */
public class NoticeEventImpl implements NoticeEvent
{

	private final Type type = IRCEvent.Type.NOTICE;
	private final String rawEventData, message, toWho, byWho;
	private final Session session;
	private final Channel channel;

	public NoticeEventImpl(String rawEventData, Session session, String message, String toWho, String byWho, Channel channel)
	{
		this.rawEventData = rawEventData;
		this.session = session;
		this.message = message;
		this.toWho = toWho;
		this.byWho = byWho;
		this.channel = channel;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.NoticeEvent#getNoticeMessage()
	 */
	public String getNoticeMessage()
	{
		return message;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.IRCEvent#getType()
	 */
	public Type getType()
	{
		return type;
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
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return rawEventData;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.NoticeEvent#byWho()
	 */
	public String byWho()
	{
		return byWho;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.NoticeEvent#getChannel()
	 */
	public Channel getChannel()
	{
		return channel;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.NoticeEvent#toWho()
	 */
	public String toWho()
	{
		return toWho;
	}

}
