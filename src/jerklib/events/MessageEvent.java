package jerklib.events;

import jerklib.Channel;

/**
 * Base interface for all PRIVMSGs
 *
 *@author mohadib
 */
public interface MessageEvent extends IRCEvent
{

    /**
     * returns IRCChannel object the PrivMsg occured in
     *
     * @return the Channel object
     */
    public Channel getChannel();


    /**
     * returns the nick of the person who created the PrivMsgIRCEvent
     *
     * @return the nick
     */
    public String getNick();

    /**
     * This will return the username field of the user's hostmask
     * nick!username@host
     *
     * @return the login field
     */
    public String getUserName();


    /**
     * getHostName() returns a string that represents the host
     * of the creator of this event
     *
     * @return the host name
     */
    public String getHostName();


    /**
     * getMessage() returns the message part of the event
     *
     * @return the message
     */
    public String getMessage();


}
