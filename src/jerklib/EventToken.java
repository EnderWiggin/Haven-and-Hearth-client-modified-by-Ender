package jerklib;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * A Class to parse a line of IRC text
 * 
 *  <pre>
 * &lt;message&gt;  ::= [':' &lt;prefix&gt; &lt;SPACE&gt; ] &lt;command&gt; &lt;params&gt; &lt;crlf&gt;
 * &lt;prefix&gt;   ::= &lt;servername&gt; | &lt;nick&gt; [ '!' &lt;user&gt; ] [ '@' &lt;host&gt; ]
 * &lt;command&gt;  ::= &lt;letter&gt; { &lt;letter&gt; } | &lt;number&gt; &lt;number&gt; &lt;number&gt;
 * &lt;SPACE&gt;    ::= ' ' { ' ' }
 * &lt;params&gt;   ::= &lt;SPACE&gt; [ ':' &lt;trailing&gt; | &lt;middle&gt; &lt;params&gt; ]
 *
 * &lt;middle&gt;   ::= &lt;Any *non-empty* sequence of octets not including SPACE
 *              or NUL or CR or LF, the first of which may not be ':'&gt;
 * &lt;trailing&gt; ::= &lt;Any, possibly *empty*, sequence of octets not including
 *                NUL or CR or LF&gt;
 * </pre>
 * 
 * @author mohadib
 *
 */
public class EventToken
{
	private final String data; 
	private String prefix = "", command = "";
	private List<String>arguments = new ArrayList<String>();
	private int offset = 0;
	
	/**
	 * Create a new EventToken
	 * 
	 * @param data to parse
	 */
	public EventToken(String data)
	{
		this.data = data;
		parse();
	}
	
	/**
	 * Parse message
	 */
	private void parse()
	{
		//see if message has prefix
		if(data.startsWith(":"))
		{
			extractPrefix(data);
			incTillChar();
		}
		
		//get command
		command = data.substring(offset , data.indexOf(" ", offset));
		offset += command.length();
		incTillChar();
		
		extractArguments();
	}
	
	/**
	 * Extract arguments from message
	 */
	private void extractArguments()
	{
		String argument = "";
		for(int i = offset; i < data.length() ; i++)
		{
			if(!Character.isWhitespace(data.charAt(i)))
			{
				argument += data.charAt(i);
				
				//if argument.equals(":") then arg is everything till EOL
				if(argument.length() == 1 && argument.equals(":"))
				{
					argument = data.substring(i + 1);
					arguments.add(argument);
					return;
				}
				offset++;
			}
			else
			{
				if(argument.length() > 0)
				{
					arguments.add(argument);
					argument = "";
				}
				offset++;
			}
		}
		
		if(argument.length() != 0)
		{
			arguments.add(argument);
		}
	}
	

	/**
	 * Increment offset until a non-whitespace char is found
	 */
	private void incTillChar()
	{
		for(int i = offset ; i < data.length(); i++)
		{
			if(!Character.isWhitespace(data.charAt(i)))
			{
				return;
			}
			offset++;
		}
	}
	
	/**
	 * Extract prefix part of messgae , inc offset
	 * 
	 * @param data
	 */
	private void extractPrefix(String data)
	{
		//set prefix - : is at 0
		prefix = data.substring(1 , data.indexOf(" "));
		
		//increment offset , +1 is for : removed
		offset += prefix.length() + 1;
	}
	
	/**
	 * Gets hostname from message 
	 * @return hostname or empty string if hostname could not be parsed
	 */
	public String hostName()
	{
		int index = prefix.indexOf('@');
		if(index != -1 && index + 1 < prefix.length())
		{
			return prefix.substring(index + 1);
		}
		return "";
	}

	/**
	 * Get username from message
	 * 
	 * @return username or empty string is username could not be parsed.
	 */
	public String userName()
	{
		int sindex = prefix.indexOf('!');
		int eindex = prefix.indexOf("@");
		if(eindex == -1)eindex = prefix.length() - 1;
		if(sindex != -1 && sindex + 1 < prefix.length())
		{
			return prefix.substring(sindex + 1 , eindex);
		}
		return "";
	}
	
	/**
	 * Get nick from message
	 * 
	 * @return nick or empty string is could not be parsed
	 */
	public String nick()
	{
		if(prefix.indexOf("!") != -1)
		{
			return prefix.substring(0,prefix.indexOf('!'));
		}
		return "";
	}

	/**
	 * Gets message prefix if any
	 * @return returns prefix or empty string if no prefix
	 */
	public String prefix()
	{
		return prefix;
	}
	
	/**
	 * Gets the command 
	 * @return the command
	 */
	public String command()
	{
		return command;
	}
	
	/**
	 * Gets list of arguments
	 * 
	 * @return list of arguments
	 */
	public List<String>args()
	{
		return arguments;
	}
	
	/**
	 * Gets an argument
	 * 
	 * @param index
	 * @return the argument or null if no argument at that index
	 */
	public String arg(int index)
	{
		if(index < arguments.size())
		{
			return arguments.get(index);
		}
		return null;
	}
	
	/**
	 * Returns raw event data
	 * 
	 * @return raw event data
	 */
	public String data()
	{
		return data;
	}
	
	/**
	 * Get the numeric code of an event.
	 * 
	 * @return numeric or -1 if command is not numeric
	 */
	public int numeric()
	{
		int i = -1;
		try
		{
			i = Integer.parseInt(command);
		}
		catch (NumberFormatException e){}
		return i;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return data;
	}
}
