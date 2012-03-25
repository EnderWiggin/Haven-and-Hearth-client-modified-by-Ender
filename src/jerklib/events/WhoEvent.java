package jerklib.events;

/**
 * This is fired when the lib gets a reply from a WHO request.
 *
 * @author <a href="mailto:robby.oconnor@gmail.com">Robert O'Connor</a>
 */
public interface WhoEvent extends IRCEvent
{
    /**
     * Get the nick of the user
     *
     * @return the nick of the user.
     */
    public String getNick();

    /**
     * Get the username of the user
     *
     * @return the username
     */
    public String getUserName();

    /**
     * Get the hostname of the user
     *
     * @return the hostname
     */
    public String getHostName();

    /**
     * Get the server the user is on.
     *
     * @return the server.
     */
    public String getServerName();

    /**
     * Returns the number of hops between you and the user.
     *
     * @return the hop count
     */
    public int getHopCount();

    /**
     * Get the real name of the user.
     *
     * @return the real name
     */
    public String getRealName();

    /**
     * Retrieve the channel (for when you WHO a channel)
     *
     * @return the channel or an empty String
     */
    public String getChannel();

    /**
     * Get whether or not the user is away.
     *
     * @return whether or not the user is away.
     */
    public boolean isAway();


}
