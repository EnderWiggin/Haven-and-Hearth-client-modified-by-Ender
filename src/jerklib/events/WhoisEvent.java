package jerklib.events;

import java.util.Date;
import java.util.List;


/**
 * Fired when whois event recieved
 * 
 * @author mohadib
 */
public interface WhoisEvent extends IRCEvent
{
    /**
     * Gets the nick the whois event is about
     *
     * @return nick
     */
    public String getNick();

    /**
     * the hostname of the whoised user
     *
     * @return hostname
     */
    public String getHost();

    /**
     * gets username of whoised user
     *
     * @return Username
     */
    public String getUser();

    /**
     * gets real name of whoised user
     *
     * @return real name
     */
    public String getRealName();

    /**
     * A list of channel names the user is joined to
     *
     * @return List of Channel names
     */
    public List<String> getChannelNames();

    /**
     * The hostname of the server who answered the
     * whois query
     *
     * @return hostname
     */
    public String whoisServer();

    /**
     * Gets whois server information
     *
     * @return server information
     */
    public String whoisServerInfo();

    /**
     * not impled
     *
     * @return not impled
     */
    public boolean isAnOperator();

    /**
     * returns true if person is idle , else false
     *
     * @return true if person is idle , else false
     */
    public boolean isIdle();

    /**
     * returns how many seconds person has been idle
     *
     * @return amount in seconds person has been idle
     */
    public long secondsIdle();

    /**
     * returns sign on time
     *
     * @return sign on time
     */
    public Date signOnTime();
}
