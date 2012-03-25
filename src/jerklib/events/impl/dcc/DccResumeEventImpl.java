package jerklib.events.impl.dcc;

import jerklib.Channel;
import jerklib.Session;
import jerklib.events.dcc.DccEvent;
import jerklib.events.dcc.DccResumeEvent;

/**
 * 
 * @author Andres N. Kievsky
 */
public class DccResumeEventImpl extends DccEventImpl implements DccResumeEvent
{

	private String filename;
	private int port;
	private long position;

	public DccResumeEventImpl(String filename, int port, long position, String ctcpString, String hostName, String message, String nick, String userName, String rawEventData, Channel channel,
			Session session)
	{
		super(ctcpString, hostName, message, nick, userName, rawEventData, channel, session);
		this.filename = filename;
		this.port = port;
		this.position = position;
	}

	public DccType getDccType()
	{
		return DccEvent.DccType.RESUME;
	}

	public String getFilename()
	{
		return this.filename;
	}

	public int getPort()
	{
		return this.port;
	}

	public long getPosition()
	{
		return this.position;
	}

}
