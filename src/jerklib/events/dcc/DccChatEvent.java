package jerklib.events.dcc;

import java.net.InetAddress;

/**
 * DCC CHAT event.
 * 
 * @author Andres N. Kievsky
 */
public interface DccChatEvent extends DccEvent
{
	// Usually "chat" or "wboard".
	String getProtocol();

	InetAddress getIp();

	int getPort();
}
