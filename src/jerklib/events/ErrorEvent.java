package jerklib.events;

/**
 * Base interface for error events
 * 
 * @author mohadib
 *
 */
public interface ErrorEvent extends IRCEvent
{
    public enum ErrorType
    {
        NUMERIC_ERROR,
        UNRESOLVED_HOSTNAME
    }

    /**
     * Get the error type
     * @return ErrorType
     */
    public ErrorType getErrorType();
}
