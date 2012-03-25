package jerklib.parsers;

import jerklib.EventToken;
import jerklib.events.IRCEvent;
import jerklib.events.impl.NickInUseEventImpl;

public class NickInUseParser implements CommandParser
{
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		return new NickInUseEventImpl
		(
				token.arg(1),
				token.data(), 
				event.getSession()
		); 
	}
}
