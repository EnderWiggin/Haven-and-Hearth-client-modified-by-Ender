package jerklib.parsers;

import jerklib.Channel;
import jerklib.EventToken;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.impl.NoticeEventImpl;

public class NoticeParser implements CommandParser
{
	
	/*
	 *:DIBLET!n=fran@c-68-35-11-181.hsd1.nm.comcast.net NOTICE #jerklib :test
	 *:anthony.freenode.net NOTICE mohadib_ :NickServ set your hostname to foo
	 *:DIBLET!n=fran@c-68-35-11-181.hsd1.nm.comcast.net NOTICE #jerklib :test
	 *:NickServ!NickServ@services. NOTICE mohadib_ :This nickname is owned by someone else
	 * NOTICE AUTH :*** No identd (auth) response
	 */
	
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		Session session = event.getSession();
		
		String toWho = "";
		String byWho = session.getConnectedHostName();
		Channel chan = null;
		
		if(!session.isChannelToken(token.arg(0)))
		{
			toWho = token.arg(0);
			if(toWho.equals("AUTH")) toWho = "";
		}
		else
		{
			chan = session.getChannel(token.arg(0));
		}
		
		if(token.prefix().length() > 0)
		{
			if(token.prefix().contains("!"))
			{
				byWho = token.nick();
			}
			else
			{
				byWho = token.prefix();
			}
		}
		
		return new NoticeEventImpl
		(
			token.data(),
			event.getSession(),
			token.arg(1),
			toWho,
			byWho,
			chan
		);
	}
}
