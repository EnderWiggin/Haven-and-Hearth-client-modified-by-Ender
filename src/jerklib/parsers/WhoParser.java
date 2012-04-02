package jerklib.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jerklib.EventToken;
import jerklib.events.IRCEvent;
import jerklib.events.impl.WhoEventImpl;

public class WhoParser implements CommandParser
{
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		String data = token.data();
		Pattern p = Pattern.compile("^:.+?\\s+352\\s+.+?\\s+(.+?)\\s+(.+?)\\s+(.+?)\\s+(.+?)\\s+(.+?)\\s+(.+?):(\\d+)\\s+(.+)$");
		Matcher m = p.matcher(data);
		if (m.matches())
		{
			
			boolean away = m.group(6).charAt(0) == 'G';
			return new WhoEventImpl(m.group(1), // channel
					Integer.parseInt(m.group(7)), // hop count
					m.group(3), // hostname
					away, // status indicator
					m.group(5), // nick
					data, // raw event data
					m.group(8), // real name
					m.group(4), // server name
					event.getSession(), // session
					m.group(2) // username
			);
		}
		return event;
	}
}
