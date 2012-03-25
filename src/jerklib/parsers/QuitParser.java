package jerklib.parsers;

import java.util.List;

import jerklib.Channel;
import jerklib.EventToken;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.QuitEvent;
import jerklib.events.impl.QuitEventImpl;

public class QuitParser implements CommandParser
{
	public QuitEvent createEvent(EventToken token, IRCEvent event)
	{
		Session session = event.getSession();
		String nick = token.nick();
		List<Channel> chanList = event.getSession().removeNickFromAllChannels(nick);
		return new QuitEventImpl
		(
			token.data(), 
			session, 
			nick, // who
			token.userName(), // username
			token.hostName(), // hostName
			token.arg(0), // message
			chanList
		);
	}
}
