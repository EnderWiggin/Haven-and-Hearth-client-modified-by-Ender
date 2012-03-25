package jerklib.events;

import jerklib.Channel;


/**
 * JoinIRCEvent is the event that will be dispatched when someone joins a channel
 *
 * @author mohadib
 */
public interface JoinEvent extends IRCEvent
{

    /**
     * returns the nick of who joined the channel
     *
     * @return Nick of who joined channel
     */
    public String getNick();


    /**
     * return the username in the user's hostmask
     *
     * @return username of the user
     */
    public String getUserName();

    /**
     * returns the hostname of the person who joined the channel
     *
     * @return hostname of the person who joined
     */
    public String getHostName();

    /**
     * returns the name of the channel joined to cause this event
     *
     * @return Name of channel
     */
    public String getChannelName();

    /**
     * returns the Channel object joined
     *
     * @return The Channel object
     * @see Channel
     */
    public Channel getChannel();

}
