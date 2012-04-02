package jerklib.parsers;

import jerklib.Channel;
import jerklib.EventToken;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.JoinEvent;
import jerklib.events.impl.JoinCompleteEventImpl;
import jerklib.events.impl.JoinEventImpl;

public class JoinParser implements CommandParser
{

	// :r0bby!n=wakawaka@guifications/user/r0bby JOIN :#jerklib
	// :mohadib_!~mohadib@68.35.11.181 JOIN &test
	
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		Session session = event.getSession();
		
		JoinEvent je = new JoinEventImpl
		(
			token.data(), 
			session, 
			token.nick(), // nick
			token.userName(), // user name
			token.hostName(), // host
			token.arg(0), // channel name
			session.getChannel(token.arg(0)) // channel
		);
		
		if(je.getNick().equalsIgnoreCase(event.getSession().getNick()))
		{
			return new JoinCompleteEventImpl
			(
				je.getRawEventData(), 
				je.getSession(),
				new Channel(je.getChannelName() , event.getSession())
			);
		}
		return je;
	}
}
