package jerklib.events;

import jerklib.Channel;

/**
 * NoticeIRCEvent - the event for notices from the server
 *
 * @author mohadib
 */
public interface NoticeEvent extends IRCEvent
{

    /**
     * returns notice message
     *
     * @return notice message
     */
    public String getNoticeMessage();

    /**
     * Gets who sent the notice event
     *
     * @return who
     */
    public String byWho();

    /**
     * If this notice is sent to a user this will return who
     *
     * @return who
     */
    public String toWho();

    /**
     * If this is a Channel notice this will return the Channel
     *
     * @return Channel
     * @see Channel
     */
    public Channel getChannel();

}
