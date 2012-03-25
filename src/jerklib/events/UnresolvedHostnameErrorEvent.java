package jerklib.events;

import java.nio.channels.UnresolvedAddressException;

/**
 * Error generated when a DNS lookup fails during connection.
 * 
 * @author mohadib
 *
 */
public interface UnresolvedHostnameErrorEvent extends ErrorEvent
{
    /**
     * Gets the unresolvable hostname
     * @return hostname that could not be resloved
     */
    String getHostName();

    /**
     * Gets the wrapped UnresolvedAddressException
     * @return UnresolvedAddressException
     */
    UnresolvedAddressException getException();
}
