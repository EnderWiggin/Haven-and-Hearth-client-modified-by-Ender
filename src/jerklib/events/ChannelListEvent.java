package jerklib.events;

import jerklib.Session;


/**
 * The event fired when a line from a channel listing is parsed
 *
 * @author mohaidb
 * @see Session#chanList()
 * @see Session#chanList(String)
 */
public interface ChannelListEvent extends IRCEvent
{

    /**
     * Gets the channel name
     *
     * @return the channel name
     */
    public String getChannelName();


    /**
     * Egts the number of users in the channel
     *
     * @return number of users
     */
    public int getNumberOfUser();


    /**
     * Gets the topic of the channel
     *
     * @return the channel topic
     */
    public String getTopic();

}
