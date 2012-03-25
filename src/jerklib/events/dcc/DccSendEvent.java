package jerklib.events.dcc;

import java.net.InetAddress;

/**
 * DCC SEND event.
 * 
 * @author Andres N. Kievsky
 */
public interface DccSendEvent extends DccEvent
{
	String getFilename();

	InetAddress getIp();

	int getPort();

	long getFileSize();

	boolean isFileSizeKnown();
}
