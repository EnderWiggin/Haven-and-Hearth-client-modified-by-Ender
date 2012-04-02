package jerklib.events;

/**
 * NickChangeIRCEvent is created when someone in a channel changes their nick
 *
 * @author mohadib
 */
public interface NickChangeEvent extends IRCEvent
{

    /**
     * Returns the previous nick of the user before the change
     *
     * @return Old nick for user.
     */
    public String getOldNick();

    /**
     * getNewNick() returns the new nick of the user
     *
     * @return New nick for user
     */
    public String getNewNick();

    /**
     * Get the username of the user who changed their nick
     *
     * @return username
     */
    public String getUserName();

    /**
     * Get the hostname of the user who changed their nick
     *
     * @return hostname
     */
    public String getHostName();
}
