package jerklib.events;


/**
 * Event fired when an Invite message is recieved from server
 *
 * @author <a href="mailto:rob@mybawx.org">Robert O'Connor</a>
 */
public interface InviteEvent extends IRCEvent
{

    /**
     * Gets the nick of the person who invited us
     *
     * @return the nick of the person who invited us
     */
    public String getNick();

    /**
     * Gets the username from the user's hostmask
     *
     * @return the username of the user who invited us.
     */
    public String getUserName();

    /**
     * Gets the hostname of the user who invited us.
     *
     * @return the hostname of the person who invited us
     */
    public String getHostName();

    /**
     * Gets the channel name to which we were invited to
     *
     * @return the channel we were invited to.
     */
    public String getChannelName();
}
