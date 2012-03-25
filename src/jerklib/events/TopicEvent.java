package jerklib.events;

import jerklib.Channel;

import java.util.Date;


/**
 * 
 * Event fired when topic is received
 * @author mohadib
 * @see Channel
 */
public interface TopicEvent extends IRCEvent
{
    /**
     * Gets the topic
     *
     * @return the topic
     */
    public String getTopic();

    /**
     * Gets who set the topic
     *
     * @return topic setter
     */
    public String getSetBy();

    /**
     * Gets when topic was set
     *
     * @return when
     */
    public Date getSetWhen();

    /**
     * Gets Channel
     *
     * @return Channel
     * @see Channel
     */
    public Channel getChannel();

}
