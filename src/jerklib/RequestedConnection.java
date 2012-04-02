package jerklib;


/**
 * Class to encapsulate data about a requested connection,
 * 
 * @author mohadib
 */
public class RequestedConnection
{

    private final String hostName;
    private final int port;
    private Profile profile;
    private final long requestedTime = System.currentTimeMillis();

    /**
     * Create new RequestedConnection object
     * 
     * @param hostName - hostname to connect to
     * @param port - port to use
     * @param profile - profile to use
     */
    public RequestedConnection(String hostName, int port, Profile profile)
    {
        this.hostName = hostName;
        this.port = port;
        this.profile = profile;
    }


    /**
     * Get hostname
     * 
     * @return hostname
     */
    public String getHostName()
    {
        return hostName;
    }

    /**
     * Get port
     * 
     * @return port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Get profile
     * @return profile
     */
    public Profile getProfile()
    {
        return profile;
    }

    /**
     * Get the time this RequestedConnection was created.
     * 
     * @return time
     */
    public long getTimeRequested()
    {
        return requestedTime;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return hostName.hashCode() + port + profile.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
        if (o instanceof RequestedConnection && o.hashCode() == hashCode())
        {
            RequestedConnection rCon = (RequestedConnection) o;
            return rCon.getHostName().equals(hostName) &&
                    rCon.getPort() == port &&
                    rCon.getProfile().equals(profile);
        }
        return false;
    }

    /**
     * Update the profile used with this requested connection
     * @param profile
     */
    void setProfile(Profile profile)
    {
        this.profile = profile;
    }

}
