package jerklib.events;

/**
 * <p>
 * This is an event that is fired under three conditions:
 * <ul>
 * <li>Sending a message to a user who is marked as away.</li>
 * <li>User of lib marks self as away.</li>
 * <li>User of lib returns from away.</li>
 * </ul>
 * You can determine under which circumstance the event was fired by looking at
 * the {@link EventType}.</p>
 *
 * @author <a href="mailto:rob@mybawx.org">Robert O'Connor<a/>
 */
public interface AwayEvent extends IRCEvent
{


    /**
     * An enum to determine the type of event that was fired.
     * <br>WENT_AWAY is when user of lib goes away.<br>
     * RETURNED_FROM_AWAY is when user of lib returns from away state.<br>
     * USER_IS_AWAY is when some other user goes away<br>
     */
    public static enum EventType
    {
        WENT_AWAY,
        RETURNED_FROM_AWAY,
        USER_IS_AWAY
    }

    /**
     * Return the event type that was fired
     *
     * @return the type of event that was fired.
     * @see EventType
     */
    public EventType getEventType();

    /**
     * Whether or not subject of event is away.
     *
     * @return if we're away or not.
     */
    public boolean isAway();


    /**
     * Returns the away message or an empty String if it was user of lib who caused the event to fire.
     *
     * @return the away message
     */
    public String getAwayMessage();

    /**
     * Whether or not it was user of lib that caused this event
     *
     * @return if it was us or not.
     */
    public boolean isYou();

    /**
     * Get the nick who fired the event.
     *
     * @return the nick of the user who caused the event to fire.
     */
    public String getNick();


}
