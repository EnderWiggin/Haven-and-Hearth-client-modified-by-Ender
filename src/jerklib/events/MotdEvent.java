package jerklib.events;


/**
 * MOTDEvent is the event dispatched for every MOTD line from the server
 *
 * @author mohadib
 */
public interface MotdEvent extends IRCEvent
{

    /**
     * Gets a line of the MOTD
     *
     * @return One line of the MOTD
     */
    public String getMotdLine();


    /**
     * returns name of host this event originated from
     *
     * @return hostname
     */
    public String getHostName();

}
