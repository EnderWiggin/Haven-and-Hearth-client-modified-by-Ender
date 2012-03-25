package jerklib.events.dcc;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import jerklib.events.MessageEvent;
import jerklib.events.impl.dcc.DccAcceptEventImpl;
import jerklib.events.impl.dcc.DccChatEventImpl;
import jerklib.events.impl.dcc.DccResumeEventImpl;
import jerklib.events.impl.dcc.DccSendEventImpl;
import jerklib.events.impl.dcc.DccUnknownEventImpl;
import jerklib.util.InetAddressUtils;

/**
 * Factory methods for DCC Events.
 * 
 * @author Andres N. Kievsky
 */

public class DccEventFactory
{

	
	public static boolean isNumeric(String data)
	{
		try
		{
			Integer.parseInt(data);
			return true;
		}
		catch (NumberFormatException e){}
		
		try
		{
			Long.parseLong(data);
			return true;
		}
		catch (NumberFormatException e){}
		
		return false;
	}
	
	public static boolean isInteger(String data)
	{
		try
		{
			Integer.parseInt(data);
			return true;
		}
		catch (NumberFormatException e){}
		return false;
	}
	
	
	public static Integer asInteger(String data)
	{
		return new Integer(data);
	}
	
	public static boolean isLong(String data)
	{
		try
		{
			Long.parseLong(data);
			return true;
		}
		catch (NumberFormatException e){}
		return false;
	}
	
	public static Long asLong(String data)
	{
		return new Long(data);
	}
	
	
	public static DccEvent dcc(MessageEvent event, String ctcpString)
	{
		
		//EventToken dccTokens = new EventToken(ctcpString);
		
		//List<Token> dccTokenList = dccTokens.getWordTokens();
		//hack till mr_ank can fix
		List<String> dccTokenList = new ArrayList<String>();
		
		// TODO ANK: Reject invalid ports, invalid filenames, IPs like 0.0.0.0.

		if(dccTokenList.size() >= 2)
		{
			String dccType = dccTokenList.get(1);
			
			// DCC SEND filename ip port <fsize>
			if("SEND".equals(dccType)
					&& (dccTokenList.size() == 5 || dccTokenList.size() == 6)
					&& isNumeric(dccTokenList.get(3))
					&& isInteger(dccTokenList.get(4)))
			{
				String filename = dccTokenList.get(2);
				InetAddress ip = InetAddressUtils.parseNumericIp(asLong(dccTokenList.get(3)));
				int port = asInteger(dccTokenList.get(4));

				// File Size is optative.
				long fileSize = -1;
				if (dccTokenList.size() == 6 && isLong(dccTokenList.get(5)))
				{
					fileSize = asLong(dccTokenList.get(5));
				}

				if (ip != null) {
					return new DccSendEventImpl(filename, ip, port, fileSize, ctcpString, event.getHostName(), event.getMessage(), event.getNick(), event.getUserName(), event.getRawEventData(), event.getChannel(), event.getSession());
				}
			}
			
			// DCC RESUME filename port position
			else if("RESUME".equals(dccType)
					&& dccTokenList.size() == 5
					&& isInteger(dccTokenList.get(3))
					&& isLong(dccTokenList.get(4)))
			{
				String filename = dccTokenList.get(2);
				int port = asInteger(dccTokenList.get(3));
				long position = asLong(dccTokenList.get(4));
				return new DccResumeEventImpl(filename, port, position, ctcpString, event.getHostName(), event.getMessage(), event.getNick(), event.getUserName(), event.getRawEventData(), event.getChannel(), event.getSession());
			}
			
			// DCC ACCEPT filename port position
			else if("ACCEPT".equals(dccType)
					&& dccTokenList.size() == 5		
					&& isInteger(dccTokenList.get(3))
					&& isLong(dccTokenList.get(4)))
			{
				String filename = dccTokenList.get(2);
				int port = asInteger(dccTokenList.get(3));
				long position = asLong(dccTokenList.get(4));
				return new DccAcceptEventImpl(filename, port, position, ctcpString, event.getHostName(), event.getMessage(), event.getNick(), event.getUserName(), event.getRawEventData(), event.getChannel(), event.getSession());
			}
			
			// DCC CHAT protocol ip port
			else if("CHAT".equals(dccType)
					&& dccTokenList.size() == 5
					&& isNumeric(dccTokenList.get(3))
					&& isInteger(dccTokenList.get(4)))
			{
				String protocol = dccTokenList.get(2);
				InetAddress ip = InetAddressUtils.parseNumericIp(asLong(dccTokenList.get(3)));
				int port = asInteger(dccTokenList.get(4));
				
				if (ip != null)
				{
					return new DccChatEventImpl(protocol, ip, port, ctcpString, event.getHostName(), event.getMessage(), event.getNick(), event.getUserName(), event.getRawEventData(), event.getChannel(), event.getSession());
				}
			}
			
		}
		
	// Default case: unknown DCC type.
		return new DccUnknownEventImpl(ctcpString, event.getHostName(), event.getMessage(), event.getNick(), event.getUserName(), event.getRawEventData(), event.getChannel(), event.getSession());
	}

}
