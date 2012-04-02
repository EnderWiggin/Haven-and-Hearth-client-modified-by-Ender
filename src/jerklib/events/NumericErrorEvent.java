package jerklib.events;

/**
 * 
 * Event fired for most all numeric error replies
 * 
 * @author Mohadib
 */
public interface NumericErrorEvent extends ErrorEvent
{
    /**
     * gets error message
     *
     * @return error message
     */
    public String getErrorMsg();

    /**
     * Gets numeric error code
     *
     * @return numeric
     */
    public int getNumeric();

}
