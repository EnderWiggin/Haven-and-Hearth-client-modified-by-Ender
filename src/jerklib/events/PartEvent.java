package jerklib.events;

import jerklib.Channel;

/**
 * PartIRCEvent is made when someone parts a channel
 *
 * @author mohadib
 */
public interface PartEvent extends IRCEvent
{


    /**
     * returns the nick of who parted
     *
     * @return nick of parted
     */
    public String getWho();

    /**
     * get the username of the nick who parted the channel.
     *
     * @return the username of the parted,
     */
    public String getUserName();

    /**
     * get the hostname of the parted
     *
     * @return the hostname of the parted
     */
    public String getHostName();


    /**
     * returns the name of the channel parted
     *
     * @return name of channel parted
     */
    public String getChannelName();


    /**
     * returns IRCChannel object for channel parted
     *
     * @return Channel object parted
     * @see Channel
     */
    public Channel getChannel();


    /**
     * returns part message if there is one
     *
     * @return part message
     */
    public String getPartMessage();
}
