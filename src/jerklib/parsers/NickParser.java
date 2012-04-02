package jerklib.parsers;

import jerklib.EventToken;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.impl.NickChangeEventImpl;

public class NickParser implements CommandParser
{
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		Session session = event.getSession();
		return new NickChangeEventImpl
		(
				token.data(), 
				session, 
				token.nick(), // old
				token.arg(0), // new nick
				token.hostName(), // hostname
				token.userName() // username
		); 
	}
}
