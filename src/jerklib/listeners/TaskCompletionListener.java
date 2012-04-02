package jerklib.listeners;

/**
 * An interface that can be used for returning a value from a Task
 * 
 * @author mohadib
 *
 */
public interface TaskCompletionListener
{
    /**
     * Called when a task wishes to notify 
     * 
     * @param result
     */
    public void taskComplete(Object result);
}
