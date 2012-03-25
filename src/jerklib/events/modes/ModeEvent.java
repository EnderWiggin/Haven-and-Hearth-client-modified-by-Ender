package jerklib.events.modes;

import jerklib.Channel;
import jerklib.events.IRCEvent;

import java.util.List;

/**
 * Event fired when mode changes for us(UserMode) or Channel(ChannelMode)
 *  
 * @author mohadib
 */
public interface ModeEvent extends IRCEvent
{
		enum ModeType
		{
			USER,
			CHANNEL
		}
		
		
		/**
		 * Indicates if this is a user mode or channel mode event
		 * @return the ModeType 
		 */
		public ModeType getModeType();

    /**
     * Gets the list of mode adjustments generated 
     * @return List of mode adjustments
     */
    public List<ModeAdjustment> getModeAdjustments();

    /**
     * Gets who set the mode
     *
     * @return who set the mode
     */
    public String setBy();


    /**
     * If mode event adjusted a Channel mode
     * then the Channel effected will be returned
     *
     * @return Channel
     * @see Channel
     */
    public Channel getChannel();
}
