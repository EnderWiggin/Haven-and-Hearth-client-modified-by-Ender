package jerklib.events.impl;

import jerklib.Channel;
import jerklib.Session;
import jerklib.events.TopicEvent;

import java.util.Date;

/**
 * @author mohadib
 * @see TopicEvent
 *
 */
public class TopicEventImpl implements TopicEvent
{

	private String setBy, data, hostname;
	private Date setWhen;
	private Session session;
	private Channel channel;
	private StringBuffer buff = new StringBuffer();

	public TopicEventImpl(String rawEventData, Session session, Channel channel, String topic)
	{
		this.data = rawEventData;
		this.session = session;
		this.channel = channel;
		buff.append(topic);
	}

	/* (non-Javadoc)
	 * @see jerklib.events.TopicEvent#getTopic()
	 */
	public String getTopic()
	{
		return buff.toString();
	}

	/**
	 * @return hostname
	 */
	public String getHostName()
	{
		return hostname;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.IRCEvent#getRawEventData()
	 */
	public String getRawEventData()
	{
		return data;
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
		return Type.TOPIC;
	}

	/**
	 * @param setWhen
	 */
	public void setSetWhen(String setWhen)
	{
		this.setWhen = new Date(1000L * Long.parseLong(setWhen));
	}

	/**
	 * @param setBy
	 */
	public void setSetBy(String setBy)
	{
		this.setBy = setBy;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.TopicEvent#getSetBy()
	 */
	public String getSetBy()
	{
		return setBy;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.TopicEvent#getSetWhen()
	 */
	public Date getSetWhen()
	{
		return setWhen;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.TopicEvent#getChannel()
	 */
	public Channel getChannel()
	{
		return channel;
	}

	/**
	 * @param topic
	 */
	public void appendToTopic(String topic)
	{
		buff.append(topic);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return channel.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		if (o == this) { return true; }
		if (o instanceof TopicEventImpl && o.hashCode() == hashCode()) { return ((TopicEvent) o).getChannel().equals(getChannel()); }
		return false;
	}

}
