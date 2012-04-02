package jerklib.events;

import jerklib.Channel;

/**
 * Event fired when someone is kicked from a channel
 * @author mohadib
 * @see Channel#kick(String, String)
 */
public interface KickEvent extends IRCEvent
{
    /**
     * Gets the nick of the user who
     * did the kicking
     *
     * @return nick
     */
    public String byWho();

    /**
     * Get the username from the hostmask of the kicker (the person doing the kicking)
     *
     * @return the username
     */
    public String getUserName();

    /**
     * get the host name of the kicker (the person doing the kicking)
     *
     * @return the hostname
     */
    public String getHostName();

    /**
     * Gets the kick message
     *
     * @return message
     */
    public String getMessage();

    /**
     * Gets the nick of who was kicked
     *
     * @return who was kicked
     */
    public String getWho();


    /**
     * Gets the channel object someone was kicked from
     *
     * @return The Channel
     */
    public Channel getChannel();
}
