package jerklib.parsers;

import jerklib.EventToken;
import jerklib.events.IRCEvent;
import jerklib.events.impl.MotdEventImpl;

public class MotdParser implements CommandParser
{
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		return new MotdEventImpl
		(
			token.data(), 
			event.getSession(), 
			token.arg(1), 
			token.prefix()
		);
	}
}
