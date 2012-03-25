package jerklib.events.impl.dcc;

import jerklib.Channel;
import jerklib.Session;
import jerklib.events.dcc.DccEvent;

class DccEventImpl implements DccEvent
{
	private String ctcpString , hostName , message , nick , userName , rawEventData;
	private Channel channel;
	private Session session;

	protected DccEventImpl
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

	
	public DccType getDccType()
	{
		return DccType.UNKNOWN;
	}

	public String getRawEventData()
	{
		return this.rawEventData;
	}

	public Session getSession()
	{
		return this.session;
	}

	public Type getType()
	{
		return Type.DCC_EVENT;
	}

	public String getCtcpString()
	{
		return this.ctcpString;
	}

	public Channel getChannel()
	{
		return this.channel;
	}

	public String getHostName()
	{
		return this.hostName;
	}

	public String getMessage()
	{
		return this.message;
	}

	public String getNick()
	{
		return this.nick;
	}

	public String getUserName()
	{
		return this.userName;
	}

}
