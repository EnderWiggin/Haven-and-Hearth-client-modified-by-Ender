package jerklib.events.impl;

import jerklib.Channel;
import jerklib.Session;
import jerklib.events.modes.ModeAdjustment;
import jerklib.events.modes.ModeEvent;

import java.util.List;

/**
 * @author mohadib
 * @see ModeEvent
 *
 */
public class ModeEventImpl implements ModeEvent
{

	private final Type type = Type.MODE_EVENT;
	private final ModeType modeType;
	private final Session session;
	private final String rawEventData, setBy;
	private final Channel channel;
	private final List<ModeAdjustment>modeAdjustments;

	public ModeEventImpl
	(
		ModeType type,
		String rawEventData, 
		Session session, 
		List<ModeAdjustment>modeAdjustments, 
		String setBy, 
		Channel channel
	)
	{
		modeType = type;
		this.rawEventData = rawEventData;
		this.session = session;
		this.modeAdjustments = modeAdjustments;
		this.setBy = setBy;
		this.channel = channel;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.modes.ModeEvent#getChannel()
	 */
	public Channel getChannel()
	{
		return channel;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.modes.ModeEvent#getModeAdjustments()
	 */
	public List<ModeAdjustment> getModeAdjustments()
	{
		return modeAdjustments;
	}

	/* (non-Javadoc)
	 * @see jerklib.events.modes.ModeEvent#setBy()
	 */
	public String setBy()
	{
		return setBy;
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
	 * @see jerklib.events.modes.ModeEvent#getModeType()
	 */
	public ModeType getModeType()
	{
		return modeType;
	}
	
	/* (non-Javadoc)
	 * @see jerklib.events.IRCEvent#getType()
	 */
	public Type getType()
	{
		return type;
	}

}
