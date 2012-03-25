package jerklib.parsers;

import java.util.Arrays;
import java.util.List;

import jerklib.EventToken;
import jerklib.events.IRCEvent;
import jerklib.events.impl.WhoisEventImpl;

public class WhoisParser implements CommandParser
{
	private WhoisEventImpl we;
	
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		switch (token.numeric())
		{
			case 311:
			{
				
				// "<nick> <user> <host> * :<real name>"
				we = new WhoisEventImpl
				(		
					token.arg(0),
					token.arg(4),
					token.arg(1),
					token.arg(2),
					token.data(), 
					event.getSession()
				); 
				break;
			}
			case 319:
			{
				// "<nick> :{[@|+]<channel><space>}"
				// :kubrick.freenode.net 319 scripy mohadib :@#jerklib
				// kubrick.freenode.net 319 scripy mohadib :@#jerklib ##swing
				if (we != null )
				{
					List<String> chanNames = Arrays.asList(token.arg(2).split("\\s+"));
					we.setChannelNamesList(chanNames);
					we.appendRawEventData(token.data());
				}
				break;
			}
			case 312:
			{
				// "<nick> <server> :<server info>"
				// :kubrick.freenode.net 312 scripy mohadib irc.freenode.net :http://freenode.net/
				if (we != null)
				{
					we.setWhoisServer(token.arg(2));
					we.setWhoisServerInfo(token.arg(3));
					we.appendRawEventData(token.data());
				}
				break;
			}
			case 320:
			{
				// not in RFC1459
				// :kubrick.freenode.net 320 scripy mohadib :is identified to services
				if (we != null)
				{
					we.appendRawEventData(token.data());
				}
				break;
			}
			case 317:
			{
				//:anthony.freenode.net 317 scripy scripy 2 1202063240 :seconds idle,signon time
				// from rfc "<nick> <integer> :seconds idle"
				if (we != null)
				{
					we.setSignOnTime(Integer.parseInt(token.arg(3)));
					we.setSecondsIdle(Integer.parseInt(token.arg(2)));
					we.appendRawEventData(token.data());
				}
				break;
			}
			case 318:
			{
				// end of whois - fireevent
				if (we != null)
				{
					we.appendRawEventData(token.data());
					return we;
				}
				break;
			}
		}
		return event;
	}
}
