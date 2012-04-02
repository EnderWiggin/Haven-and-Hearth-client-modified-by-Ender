package jerklib.events.impl;

import jerklib.Session;
import jerklib.events.WhoisEvent;

import java.util.Date;
import java.util.List;

/**
 * @author mohadib
 * @see WhoisEvent
 *
 */
public class WhoisEventImpl implements WhoisEvent
{
	private final Type type = Type.WHOIS_EVENT;
	private final String host, user, realName, nick;
	private final Session session;
	private String whoisServer, whoisServerInfo, rawEventData;
	private List<String> channelNames;
	private boolean isOp;
	private long secondsIdle;
	private int signOnTime;

	public WhoisEventImpl(String nick, String realName, String user, String host, String rawEventData, Session session)
	{
		this.nick = nick;
		this.realName = realName;
		this.user = user;
		this.host = host;
		this.session = session;
		this.rawEventData = rawEventData;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhoisEvent#getChannelNames()
	 */
	public List<String> getChannelNames()
	{
		return channelNames;
	}

	/**
	 * @param chanNames
	 */
	public void setChannelNamesList(List<String> chanNames)
	{
		channelNames = chanNames;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhoisEvent#getHost()
	 */
	public String getHost()
	{
		return host;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhoisEvent#getUser()
	 */
	public String getUser()
	{
		return user;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhoisEvent#getRealName()
	 */
	public String getRealName()
	{
		return realName;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhoisEvent#getNick()
	 */
	public String getNick()
	{
		return nick;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhoisEvent#isAnOperator()
	 */
	public boolean isAnOperator()
	{
		return isOp;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhoisEvent#isIdle()
	 */
	public boolean isIdle()
	{
		return secondsIdle > 0;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhoisEvent#secondsIdle()
	 */
	public long secondsIdle()
	{
		return secondsIdle;
	}

	/**
	 * @param secondsIdle
	 */
	public void setSecondsIdle(int secondsIdle)
	{
		this.secondsIdle = secondsIdle();
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhoisEvent#signOnTime()
	 */
	public Date signOnTime()
	{
		return new Date(1000L * signOnTime);
	}

	/**
	 * @param signOnTime
	 */
	public void setSignOnTime(int signOnTime)
	{
		this.signOnTime = signOnTime;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhoisEvent#whoisServer()
	 */
	public String whoisServer()
	{
		return whoisServer;
	}

	/**
	 * @param whoisServer
	 */
	public void setWhoisServer(String whoisServer)
	{
		this.whoisServer = whoisServer;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.WhoisEvent#whoisServerInfo()
	 */
	public String whoisServerInfo()
	{
		return whoisServerInfo;
	}

	/**
	 * @param whoisServerInfo
	 */
	public void setWhoisServerInfo(String whoisServerInfo)
	{
		this.whoisServerInfo = whoisServerInfo;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.IRCEvent#getRawEventData()
	 */
	public String getRawEventData()
	{
		return rawEventData;
	}

	/**
	 * @param rawEventData
	 */
	public void appendRawEventData(String rawEventData)
	{
		this.rawEventData += "\r\n" + rawEventData;
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
