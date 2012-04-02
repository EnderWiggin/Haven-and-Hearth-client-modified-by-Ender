package jerklib.events.dcc;

/**
 * DCC ACCEPT event.
 * 
 * @author Andres N. Kievsky
 */
public interface DccAcceptEvent extends DccEvent
{
	String getFilename();

	int getPort();

	long getPosition();

}
