package jerklib.events;

/**
 * Event fired when whowas data received
 * @author mohadib
 */
public interface WhowasEvent extends IRCEvent
{

    /**
     * get nick who was event is about
     *
     * @return nick who was event is about
     */
    String getNick();


    /**
     * get username
     *
     * @return username
     */
    String getUserName();

    /**
     * get hostname of whoised user
     *
     * @return hostname
     */
    String getHostName();

    /**
     * get users realname
     *
     * @return realname
     */
    String getRealName();
}
