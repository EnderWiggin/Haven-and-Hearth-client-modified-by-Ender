package jerklib.events.impl;

import jerklib.Channel;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.QuitEvent;

import java.util.List;

/**
 * @author mohadib
 * @see QuitEvent
 *
 */
public class QuitEventImpl implements QuitEvent
{

	private final Type type = IRCEvent.Type.QUIT;
	private final String rawEventData, who, msg, userName, hostName;
	private final Session session;
	private final List<Channel> chanList;

	public QuitEventImpl(String rawEventData, Session session, String who, String userName, String hostName, String msg, List<Channel> chanList)
	{
		this.rawEventData = rawEventData;
		this.who = who;
		this.userName = userName;
		this.hostName = hostName;
		this.session = session;
		this.msg = msg;
		this.chanList = chanList;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.QuitEvent#getHostName()
	 */
	public String getHostName()
	{
		return hostName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.QuitEvent#getUserName()
	 */
	public String getUserName()
	{
		return userName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.QuitEvent#getNick()
	 */
	public final String getNick()
	{
		return who;
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
	 * @see jerklib.events.QuitEvent#getQuitMessage()
	 */
	public final String getQuitMessage()
	{
		return msg;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.QuitEvent#getChannelList()
	 */
	public final List<Channel> getChannelList()
	{
		return chanList;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return rawEventData;
	}

}
