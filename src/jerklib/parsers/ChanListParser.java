package jerklib.parsers;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jerklib.EventToken;
import jerklib.events.IRCEvent;
import jerklib.events.impl.ChannelListEventImpl;

public class ChanListParser implements CommandParser
{
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		String data = token.data();
		Pattern p = Pattern.compile("^:\\S+\\s322\\s\\S+\\s(\\S+)\\s(\\d+)\\s:(.*)$");
		Matcher m = p.matcher(data);
		if (m.matches()) 
		{ 
			return new ChannelListEventImpl
			(
				data, 
				m.group(1), 
				m.group(3), 
				Integer.parseInt(m.group(2)), 
				event.getSession()
			); 
		}
		return event;
	}
}
