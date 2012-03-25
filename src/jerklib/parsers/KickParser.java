package jerklib.parsers;

import jerklib.Channel;
import jerklib.EventToken;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.impl.KickEventImpl;

/**
 * @author mohadib
 *
 */
public class KickParser implements CommandParser
{
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		Session session = event.getSession();
		Channel channel = session.getChannel(token.arg(0));
		
		String msg = "";
		if (token.args().size() == 3)
		{
			msg = token.arg(2);
		}
		
		return new KickEventImpl
		(
			token.data(), 
			session, 
			token.nick(), // byWho
			token.userName(), // username
			token.hostName(), // host name
			token.arg(1), // victim
			msg, // message
			channel
		);
	}
}
