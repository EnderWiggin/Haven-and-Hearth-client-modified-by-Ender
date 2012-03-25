package jerklib.events.dcc;

import jerklib.events.CtcpEvent;
import jerklib.events.IRCEvent;

/**
 * Base interface for all DCC Events.
 * 
 * @author Andres N. Kievsky
 */
public interface DccEvent extends IRCEvent, CtcpEvent
{

	/**
	 * Type enum is used to determine type. It is returned from getDccType()
	 */

	public enum DccType
	{
		/**
		 * Request to start a DCC SEND. The sender starts with: DCC SEND filename ip
		 * port (and optionally file size)
		 */
		SEND,

		/**
		 * Ask the sender to skip part of the file. DCC RESUME filename port
		 * position
		 */
		RESUME,

		/**
		 * If the sender supports DCC RESUME, it replies with: DCC ACCEPT filename
		 * port position
		 */
		ACCEPT,

		/**
		 * Request to start a DCC CHAT. Usual format: DCC CHAT protocol ip port
		 */
		CHAT,

		/**
		 * Used then the DCC command is unknown.
		 */
		UNKNOWN,
	}

	/**
	 * Used to find out the exact type of event the DccEvent object is. The
	 * DccEvent object can be cast into a more specific event object to get access
	 * to convenience methods for the specific event types.
	 * 
	 * @return Type of DCC Event
	 */
	public DccType getDccType();

}
