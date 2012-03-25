package jerklib;

import jerklib.events.IRCEvent.Type;
import jerklib.events.impl.ConnectionLostEventImpl;
import jerklib.events.modes.ModeAdjustment;
import jerklib.events.modes.ModeAdjustment.Action;
import jerklib.listeners.IRCEventListener;
import jerklib.parsers.CommandParser;
import jerklib.parsers.DefaultInternalEventParser;
import jerklib.parsers.InternalEventParser;
import jerklib.tasks.Task;
import jerklib.tasks.TaskImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * 
 * Session contains methods to manage an IRC connection.
 * Like {@link Session#changeNick(String)} , {@link Session#setRejoinOnKick(boolean)} , {@link Session#getUserModes()} etc.
 *<p>
 * Session is where Tasks and Listeners should be added
 * to be notified of IRCEvents coming from the connected server.
 *<p> 
 * You can override the default parsing and internal event handling
 * of a Session with {@link Session#setInternalEventHandler(IRCEventListener)} and
 * {@link Session#setInternalParser(InternalEventParser)}.
 *<p>
 * New Session instances are obtained by requesting a connection with the
 * ConnectionManager
 * 
 * @see ConnectionManager#requestConnection(String)
 * @see ConnectionManager#requestConnection(String, int)
 * @see ConnectionManager#requestConnection(String, int, Profile)
 * 
 * @author mohadib
 */
public class Session extends RequestGenerator
{

	private final List<IRCEventListener> listenerList = new ArrayList<IRCEventListener>();
	private final Map<Type, List<Task>> taskMap = new HashMap<Type, List<Task>>();
	private final RequestedConnection rCon;
	private Connection con;
	private final ConnectionManager conman;
	private boolean rejoinOnKick = true, profileUpdating, isAway , isLoggedIn , useAltNicks = true;
	private Profile tmpProfile;
	private long lastRetry = -1, lastResponse = System.currentTimeMillis();
	private ServerInformation serverInfo = new ServerInformation();
	private State state = State.DISCONNECTED;
	private InternalEventParser parser;
	private IRCEventListener internalEventHandler;
	private List<ModeAdjustment> userModes = new ArrayList<ModeAdjustment>();
	private final Map<String, Channel> channelMap = new HashMap<String, Channel>();
	
	public static enum State
	{
		CONNECTED, 
		CONNECTING, 
		HALF_CONNECTED, 
		DISCONNECTED, 
		MARKED_FOR_REMOVAL, 
		NEED_TO_PING, 
		PING_SENT, 
		NEED_TO_RECONNECT
	}

	
	/**
	 * @param rCon
	 * @param conman
	 */
	Session(RequestedConnection rCon, ConnectionManager conman)
	{
		this.rCon = rCon;
		this.conman = conman;
		setSession(this);
	}
	
	/**
	 * Gets the InternalEventParser this Session uses for event parsing
	 * 
	 * @see InternalEventParser
	 * @see DefaultInternalEventParser
	 * @see CommandParser
	 * @return InternalEventParser
	 */
	public InternalEventParser getInternalEventParser()
	{
		return parser;
	}

	/**
	 * Sets the InternalEventParser this Session should use for
	 * event parsing
	 * 
	 * 
	 * @see InternalEventParser
	 * @see DefaultInternalEventParser
	 * @see CommandParser
	 * @param parser
	 */
	public void setInternalParser(InternalEventParser parser)
	{
		this.parser = parser;
	}
	
	/**
	 * Sets the internal event handler this Session should use 
	 * 
	 * @see IRCEventListener
	 * @see DefaultInternalEventHandler
	 * @param handler
	 */
	public void setInternalEventHandler(IRCEventListener handler)
	{
		internalEventHandler = handler;
	}

	/**
	 * Returns the internal event handler this Session is using
	 * 
	 * @see IRCEventListener
	 * @see DefaultInternalEventHandler
	 * @return event handler
	 * 
	 */
	public IRCEventListener getInternalEventHandler()
	{
		return internalEventHandler;
	}

	/**
	 * Called when UserMode events are received for this Session.
	 * 
	 * @param modes
	 */
	void updateUserModes(List<ModeAdjustment> modes)
	{
		for (ModeAdjustment ma : modes)
		{
			updateUserMode(ma);
		}
	}

	/**
	 * If Action is MINUS and the same mode exists with a PLUS Action then just
	 * remove the PLUS mode ModeAdjustment from the collection.
	 * 
	 * If Action is MINUS and the same mode with PLUS does not exist then add the
	 * MINUS mode to the ModeAdjustment collection
	 * 
	 * if Action is PLUS and the same mode exists with a MINUS Action then remove
	 * MINUS mode and add PLUS mode
	 * 
	 * If Action is PLUS and the same mode with MINUS does not exist then just add
	 * PLUS mode to collection
	 * 
	 * @param mode
	 */
	private void updateUserMode(ModeAdjustment mode)
	{
		int index = indexOfMode(mode.getMode(), userModes);

		if (mode.getAction() == Action.MINUS)
		{
			if (index != -1)
			{
				ModeAdjustment ma = userModes.remove(index);
				if (ma.getAction() == Action.MINUS) userModes.add(ma);
			}
			else
			{
				userModes.add(mode);
			}
		}
		else
		{
			if (index != -1) userModes.remove(index);
			userModes.add(mode);
		}
	}

	/**
	 * Finds the index of a mode in a list modes
	 * @param mode
	 * @param modes
	 * @return index of mode or -1 if mode if not found
	 */
	private int indexOfMode(char mode, List<ModeAdjustment> modes)
	{
		for (int i = 0; i < modes.size(); i++)
		{
			ModeAdjustment ma = modes.get(i);
			if (ma.getMode() == mode) return i;
		}
		return -1;
	}

	/**
	 * returns a List of UserModes for this Session
	 * 
	 * @return UserModes
	 */
	public List<ModeAdjustment> getUserModes()
	{
		return new ArrayList<ModeAdjustment>(userModes);
	}

	/**
	 * Speak in a Channel
	 * 
	 * @see Channel#say(String)
	 * @param channel 
	 * @param msg
	 */
	public void sayChannel(Channel channel, String msg)
	{
		super.sayChannel(msg, channel);
	}


	/* general methods */

	/**
	 * Is this Session currently connected to an IRC server?
	 * 
	 * @return true if connected else false
	 */
	public boolean isConnected()
	{
		return state == State.CONNECTED;
	}
	

	/**
	 * Should this Session rejoin channels it is Kicked from?
	 * Default is true.
	 * 
	 * @return true if channels should be rejoined else false
	 */
	public boolean isRejoinOnKick()
	{
		return rejoinOnKick;
	}
	

	/**
	 * Sets that this Sessions should or should not rejoin Channels
	 * kiced from
	 * 
	 * @param rejoin
	 */
	public void setRejoinOnKick(boolean rejoin)
	{
		rejoinOnKick = rejoin;
	}
	
	
	/**
	 * Called to alert the Session that login was a success
	 */
	void loginSuccess()
	{
		isLoggedIn = true;
	}
	
	
	/**
	 * Returns true if the Session has an active Connection and
	 * has successfully logged on to the Connection.
	 * @return if logged in 
	 */
	public boolean isLoggedIn()
	{
		return isLoggedIn;
	}
	
	/**
	 * Set Session yo try alternate nicks
	 * on connection if a nick in use event is received , or not.
	 * True by default.
	 * 
	 * @param use
	 */
	public void setShouldUseAltNicks(boolean use)
	{
		useAltNicks = use;
	}
	
	
	/**
	 * Returns if Session should try alternate nicks
	 * on connection if a nick in use event is received.
	 * True by default.
	 * 
	 * @return should use alt nicks
	 */
	public boolean getShouldUseAltNicks()
	{
		return useAltNicks;
	}
	
	
	/**
	 * Disconnect from server and destroy Session
	 * 
	 * @param quitMessage
	 */
	public void close(String quitMessage)
	{
		if (con != null)
		{
			con.quit(quitMessage);
		}
		conman.removeSession(this);
		isLoggedIn = false;
	}

	/**
	 * Nick used for Session
	 * 
	 * @return nick
	 */
	public String getNick()
	{
		return getRequestedConnection().getProfile().getActualNick();
	}

	/* (non-Javadoc)
	 * @see jerklib.RequestGenerator#changeNick(java.lang.String)
	 */
	public void changeNick(String newNick)
	{
		tmpProfile = rCon.getProfile().clone();
		tmpProfile.setActualNick(newNick);
		tmpProfile.setFirstNick(newNick);
		profileUpdating = true;
		super.changeNick(newNick);
	}

	/**
	 * Profile is updating when a new nick is requested but has not been approved from server yet.
	 * @return true if profile is updating
	 */
	public boolean isProfileUpdating()
	{
		return profileUpdating;
	}


	/**
	 * Is this Session marked away?
	 * 
	 * @return true if away else false
	 */
	public boolean isAway()
	{
		return isAway;
	}

	/* (non-Javadoc)
	 * @see jerklib.RequestGenerator#setAway(java.lang.String)
	 */
	public void setAway(String message)
	{
		isAway = true;
		super.setAway(message);
	}

	/**
	 *  Unset away
	 */
	public void unsetAway()
	{
		/* if we're not away let's not bother even delegating */
		if (isAway)
		{
			super.unSetAway();
			isAway = false;
		}
	}

	/* methods to get information about connection and server */

	/**
	 * Get ServerInformation for Session
	 * @see ServerInformation
	 * @return ServerInformation for Session
	 */
	public ServerInformation getServerInformation()
	{
		return serverInfo;
	}

	/**
	 * Get RequestedConnection for Session
	 * @see RequestedConnection
	 * @return RequestedConnection for Session
	 */
	public RequestedConnection getRequestedConnection()
	{
		return rCon;
	}

	/**
	 * Returns host name this Session is connected to.
	 * If the session is disconnectd an empty string will be returned.
	 * 
	 * @return hostname or an empty string if not connected
	 * @see Session#getRequestedConnection()
	 * @see RequestedConnection#getHostName()
	 */
	public String getConnectedHostName()
	{
		return con == null?"":con.getHostName();
	}


	/**
	 * Adds an IRCEventListener to the Session. This listener will be
	 * notified of all IRCEvents coming from the connected sever.
	 * 
	 * @param listener
	 */
	public void addIRCEventListener(IRCEventListener listener)
	{
		listenerList.add(listener);
	}

	/**
	 * Remove IRCEventListner from Session
	 * 
	 * @param listener
	 * @return true if listener was removed else false
	 */
	public boolean removeIRCEventListener(IRCEventListener listener)
	{
		return listenerList.remove(listener);
	}

	/**
	 * Get a collection of all IRCEventListeners attached to Session
	 * 
	 * @return listeners
	 */
	public Collection<IRCEventListener> getIRCEventListeners()
	{
		return Collections.unmodifiableCollection(listenerList);
	}

	/**
	 * Add a task to be ran when any IRCEvent is received
	 * @see Task
	 * @see TaskImpl
	 * @param task
	 */
	public void onEvent(Task task)
	{
		// null means task should be notified of all Events
		onEvent(task, (Type) null);
	}

	/**
	 * Add a task to be ran when any of the given Types 
	 * of IRCEvents are received
	 * 
	 * @see Task
	 * @see TaskImpl
	 * @param task - task to run
	 * @param types - types of events task should run on
	 */
	public void onEvent(Task task, Type... types)
	{
		synchronized (taskMap)
		{
			for (Type type : types)
			{
				if (!taskMap.containsKey(type))
				{
					List<Task> tasks = new ArrayList<Task>();
					tasks.add(task);
					taskMap.put(type, tasks);
				}
				else
				{
					taskMap.get(type).add(task);
				}
			}
		}
	}
	
	/**
	 * Gets All Tasks attacthed to Session
	 * Indexed by the Type the task is receving events for.
	 * Task type of null are default tasks that receive all events.
	 * Some Tasks can possibly be the value for many Types.
	 * 
	 * @return tasks
	 */
	Map<Type, List<Task>> getTasks()
	{
		return new HashMap<Type, List<Task>>(taskMap);
	}

	/**
	 * Removes a Task from the Session.
	 * Some Tasks can possibly be the value for many Types.
	 * 
	 * @param t
	 */
	public void removeTask(Task t)
	{
		synchronized (taskMap)
		{
			for (Iterator<Type> it = taskMap.keySet().iterator(); it.hasNext();)
			{
				List<Task> tasks = taskMap.get(it.next());
				if (tasks != null)
				{
					tasks.remove(t);
				}
			}
		}
	}

	/**
	 * Called to alert Session if the profile was updated
	 * 
	 * @param success
	 */
	void updateProfileSuccessfully(boolean success)
	{
		if (success)
		{
			rCon.setProfile(tmpProfile);
		}
		tmpProfile = null;
		profileUpdating = false;
	}

	/**
	 * Get a List of Channels Session is currently in
	 * 
	 * @see Channel
	 * @return channels
	 */
	public List<Channel> getChannels()
	{
		return Collections.unmodifiableList(new ArrayList<Channel>(channelMap.values()));
	}

	/**
	 * Gets a Channel by name 
	 * 
	 * @param channelName
	 * @return Channel or null if no such Channel is joined.
	 */
	public Channel getChannel(String channelName)
	{
		Channel chan = channelMap.get(channelName);
		return chan == null ? channelMap.get(channelName.toLowerCase()) : chan;
	}

	/**
	 * Add a Channel to the session
	 * @see Channel
	 * @param channel
	 */
	void addChannel(Channel channel)
	{
		channelMap.put(channel.getName(), channel);
	}

	/**
	 * Remove a channel from the Session
	 * @param channel
	 * @return true if channel was removed else false
	 */
	boolean removeChannel(Channel channel)
	{
		return channelMap.remove(channel.getName()) == null;
	}

	/**
	 * Updates a nick in all channels currently joined
	 * 
	 * @param oldNick
	 * @param newNick
	 */
	void nickChanged(String oldNick, String newNick)
	{
		synchronized (channelMap)
		{
			for (Channel chan : channelMap.values())
			{
				if (chan.getNicks().contains(oldNick))
				{
					chan.nickChanged(oldNick, newNick);
				}
			}
		}
	}
	
	/**
	 * Removes a nick from all channels
	 * @param nick
	 * @return list of Channels nick was found in
	 */
	public List<Channel> removeNickFromAllChannels(String nick)
	{
		List<Channel> returnList = new ArrayList<Channel>();
		for (Channel chan : channelMap.values())
		{
			if (chan.removeNick(nick))
			{
				returnList.add(chan);
			}
		}
		return returnList;
	}

	/* methods to track connection attempts */

	/**
	 * return time of last reconnect attempt
	 * @return
	 */
	long getLastRetry()
	{
		return lastRetry;
	}

	/**
	 * sets time of last reconnect event
	 */
	void retried()
	{
		lastRetry = System.currentTimeMillis();
	}

	/**
	 * Sets the connection for this Session
	 * @param con
	 */
	void setConnection(Connection con)
	{
		this.con = con;
	}

	/**
	 * Gets Connection used for this Session. Can return null if
	 * Session is disconnected.
	 * 
	 * @return Connection
	 */
	Connection getConnection()
	{
		return con;
	}


	/**
	 * Got ping response
	 */
	void gotResponse()
	{
		lastResponse = System.currentTimeMillis();
		state = State.CONNECTED;
	}

	/**
	 * Ping has been sent but no response yet
	 */
	void pingSent()
	{
		state = State.PING_SENT;
	}

	/**
	 *Session has been disconnected 
	 */
	void disconnected()
	{
		if (state == State.DISCONNECTED) return;
		state = State.DISCONNECTED;
		if (con != null)
		{
			con.quit("");
			con = null;
		}
		
		isLoggedIn = false;
		conman.addToRelayList(new ConnectionLostEventImpl(this));
	}

	/**
	 * Session is now connected
	 */
	void connected()
	{
		gotResponse();
	}

	/**
	 * Session is connecting
	 */
	void connecting()
	{
		state = State.CONNECTING;
	}

	/**
	 * Session is half connected
	 */
	void halfConnected()
	{
		state = State.HALF_CONNECTED;
	}

	/**
	 * Session has been marked for removal
	 */
	void markForRemoval()
	{
		state = State.MARKED_FOR_REMOVAL;
	}

	/**
	 * Get the State of the Session
	 * @return Session state
	 * @see State
	 */
	State getState()
	{
		long current = System.currentTimeMillis();

		if (state == State.DISCONNECTED) return state;

		if (current - lastResponse > 300000 && state == State.NEED_TO_PING)
		{
			state = State.NEED_TO_RECONNECT;
		}
		else if (current - lastResponse > 200000 && state != State.PING_SENT)
		{
			state = State.NEED_TO_PING;
		}

		return state;
	}

	/**
	 * Test if a String starts with a known channel prefix
	 * @param token
	 * @return true if starts with a channel prefix else false
	 */
	public boolean isChannelToken(String token)
	{
		ServerInformation serverInfo = getServerInformation();
		String[] chanPrefixes = serverInfo.getChannelPrefixes();
		for (String prefix : chanPrefixes)
		{
			if (token.startsWith(prefix)) { return true; }
		}
		return false;
	}
	
	/**
	 * Send login messages to server
	 */
	void login()
	{
		// test :irc.inter.net.il CAP * LS :multi-prefix
		// writeRequests.add(new WriteRequest("CAP LS", this));
		sayRaw("NICK " + getNick());
		sayRaw("USER " + rCon.getProfile().getName() + " 0 0 :" + rCon.getProfile().getName());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return rCon.getHostName().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		if (o instanceof Session && o.hashCode() == hashCode()) 
		{ 
			return ((Session) o).getRequestedConnection().getHostName().matches(getRequestedConnection().getHostName())
				&& ((Session) o).getNick().matches(getNick()); 
		}
		return false;
	}

}
