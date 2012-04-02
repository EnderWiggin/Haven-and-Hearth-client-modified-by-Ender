package jerklib.events.impl.dcc;

import java.net.InetAddress;

import jerklib.Channel;
import jerklib.Session;
import jerklib.events.dcc.DccChatEvent;
import jerklib.events.dcc.DccEvent;

/**
 * 
 * @author Andres N. Kievsky
 */
public class DccChatEventImpl extends DccEventImpl implements DccChatEvent
{

	private int port;
	private InetAddress ip;
	private String protocol;

	public DccChatEventImpl(String protocol, InetAddress ip, int port, String ctcpString, String hostName, String message, String nick, String userName, String rawEventData, Channel channel,
			Session session)
	{
		super(ctcpString, hostName, message, nick, userName, rawEventData, channel, session);
		this.protocol = protocol;
		this.ip = ip;
		this.port = port;
	}

	public DccType getDccType()
	{
		return DccEvent.DccType.CHAT;
	}

	public String getProtocol()
	{
		return this.protocol;
	}

	public InetAddress getIp()
	{
		return this.ip;
	}

	public int getPort()
	{
		return this.port;
	}

}
