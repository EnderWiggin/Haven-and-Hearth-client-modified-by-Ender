package jerklib.events;

import jerklib.ServerInformation;

/**
 * Event fired when IRC numeric 005 is received - AKA Server Information
 * 
 * @author mohadib
 *
 */
public interface ServerInformationEvent extends IRCEvent
{
    /**
     * Gets the server information object
     * 
     * @return the info
     */
    ServerInformation getServerInformation();
}
