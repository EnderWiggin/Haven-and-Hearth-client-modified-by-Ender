package jerklib.parsers;

import jerklib.EventToken;
import jerklib.events.IRCEvent;

public interface CommandParser
{
	public IRCEvent createEvent(EventToken token , IRCEvent event);
}
