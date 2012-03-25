package jerklib.events.dcc;

/**
 * DCC RESUME event.
 * 
 * @author Andres N. Kievsky
 */
public interface DccResumeEvent extends DccEvent
{
	String getFilename();

	int getPort();

	long getPosition();

}
