package jerklib.events;

import jerklib.Channel;
import java.util.List;

/**
 * This is the event fired when someone quits
 *
 * @author mohadib
 */
public interface QuitEvent extends IRCEvent
{

    /**
     * returns the nick of who quit
     *
     * @return the nick who quit
     */
    public String getNick();

    /**
     * Get the username from the hostmask of the quitted
     *
     * @return the username
     */
    public String getUserName();

    /**
     * get the host name of the user.
     *
     * @return the hostname of the quitted.
     */
    public String getHostName();


    /**
     * getQuitMessage get the quit message
     *
     * @return the quit message
     */
    public String getQuitMessage();

    /**
     * returns a list of Channel objects
     * the nick who quit was in
     *
     * @return List of channels nick was in
     * @see Channel
     */
    public List<Channel> getChannelList();
}
