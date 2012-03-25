package jerklib.parsers;

import java.util.HashMap;
import java.util.Map;

import jerklib.Channel;
import jerklib.EventToken;
import jerklib.events.IRCEvent;
import jerklib.events.TopicEvent;
import jerklib.events.impl.TopicEventImpl;

/*
:sterling.freenode.net 332 scrip #test :Welcome to #test - This channel is
:sterling.freenode.net 333 scrip #test LuX 1159267246
*/
public class TopicParser implements CommandParser
{
	private Map<Channel, TopicEvent> topicMap = new HashMap<Channel, TopicEvent>();
	
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		if(token.numeric() == 332)
		{
			TopicEvent tEvent = new TopicEventImpl
			(
					token.data(), 
					event.getSession(), 
					event.getSession().getChannel(token.arg(1)),
					token.arg(2)
			);
			if (topicMap.containsValue(tEvent.getChannel()))
			{
				((TopicEventImpl) topicMap.get(tEvent.getChannel())).appendToTopic(tEvent.getTopic());
			}
			else
			{
				topicMap.put(tEvent.getChannel(), tEvent);
			}
		}
		else
		{
			Channel chan = event.getSession().getChannel(token.arg(1));
			if (topicMap.containsKey(chan))
			{
				TopicEventImpl tEvent = (TopicEventImpl) topicMap.get(chan);
				topicMap.remove(chan);
				tEvent.setSetBy(token.arg(2));
				tEvent.setSetWhen(token.arg(3));
				chan.setTopicEvent(tEvent);
				return tEvent;
			}
		}
		return event;
	}
}
