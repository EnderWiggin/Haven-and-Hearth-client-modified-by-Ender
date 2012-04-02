package jerklib.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jerklib.EventToken;
import jerklib.events.IRCEvent;
import jerklib.events.impl.InviteEventImpl;

public class InviteParser implements CommandParser
{
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		String data = token.data();
		Pattern p = Pattern.compile("^:(\\S+?)!(\\S+?)@(\\S+)\\s+INVITE.+?:(.*)$");
		Matcher m = p.matcher(data);
		m.matches();
		return new InviteEventImpl
		(
			m.group(4).toLowerCase(), 
			m.group(1), 
			m.group(2), 
			m.group(3), 
			data, 
			event.getSession()
		); 
	}
}
