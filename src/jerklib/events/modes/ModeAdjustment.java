package jerklib.events.modes;


/**
 * A Class to represent a mode adjustment to a user or a channel.
 * 
 * @author mohadib
 *
 */
public class ModeAdjustment
{
	private final Action action;
	private final char mode;
	private final String argument;
	
	/**
	 * Enum of mode action types. Modes can only be applied or removed. 
	 */
	public static enum Action
	{
		PLUS,
		MINUS
	}
	
	public ModeAdjustment(Action action , char mode , String argument)
	{
		this.action = action;
		this.mode = mode;
		this.argument = argument;
	}
	
	/**
	 * Indicates if the mode is being applied or removed
	 * @return PLUS if applying MINUS if removing
	 */
	public Action getAction()
	{
		return action; 
	}
	
	/**
	 * Get the mode for this adjustment
	 * 
	 * @return the mode
	 */
	public char getMode()
	{
		return mode;
	}
	
	/**
	 * This will return the argument for 
	 * this mode if any.
	 * 
	 * @return the argument for the mode or an empty string is no argument
	 */
	public String getArgument()
	{
		return argument;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return (action == Action.PLUS?"+":"-") + mode + " " + argument;
	}
}
