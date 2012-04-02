package jerklib;

import jerklib.Session.State;
import jerklib.events.IRCEvent;
import jerklib.listeners.WriteRequestListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * A class for reading and writing to an IRC connection.
 * This class will also handle PING/PONG.
 * 
 * @author mohadib
 *
 */
class Connection
{
	private Logger log = Logger.getLogger(this.getClass().getName());

	/* ConnectionManager for this Connection */
	private final ConnectionManager manager;

	/* SocketChannel this connection will use for reading/writing */
	private final SocketChannel socChannel;

	/* A Buffer for write request */
	final List<WriteRequest> writeRequests = Collections.synchronizedList(new ArrayList<WriteRequest>());

	/* ByteBuffer for readinging into */
	private final ByteBuffer readBuffer = ByteBuffer.allocate(2048);

	/* indicates if an event fragment is waiting */
	private boolean gotFragment;

	/* buffer for event fragments */
	private final StringBuffer stringBuff = new StringBuffer();

	/* actual hostname connected to */
	private String actualHostName;
	
	/* Session Connection belongs to */
	private final Session session;

	/**
	 * @param manager
	 * @param socChannel - socket channel to read from
	 * @param session - Session this Connection belongs to
	 */
	Connection(ConnectionManager manager, SocketChannel socChannel, Session session)
	{
		this.manager = manager;
		this.socChannel = socChannel;
		this.session = session;
	}

	/**
	 * Get profile use for this Connection
	 * 
	 * @return the Profile
	 */
	Profile getProfile()
	{
		return session.getRequestedConnection().getProfile();
	}

	/**
	 * Sets the actual host name of this Connection.
	 * @param name
	 */
	void setHostName(String name)
	{
		actualHostName = name;
	}

	/**
	 * Gets actual hostname for Connection.
	 * 
	 * @return hostname
	 */
	String getHostName()
	{
		return actualHostName;
	}

	/**
	 * Adds a listener to be notified of all data written via this Connection
	 * 
	 * @param request
	 */
	void addWriteRequest(WriteRequest request)
	{
		writeRequests.add(request);
	}

	/**
	 * Called to finish the Connection Process
	 * 
	 * @return true if fincon is successfull
	 * @throws IOException
	 */
	boolean finishConnect() throws IOException
	{
		return socChannel.finishConnect();
	}
	
	/**
	 * Reads from connection and creates default IRCEvents that 
	 * are added to the ConnectionManager for relaying
	 * 
	 * @return bytes read
	 */
	int read()
	{

		if (!socChannel.isConnected())
		{
			log.severe("Read call while sochan.isConnected() == false");
			return -1;
		}

		readBuffer.clear();

		int numRead = 0;

		try
		{
			numRead = socChannel.read(readBuffer);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			session.disconnected();
		}

		if (numRead == -1)
		{
			session.disconnected();
		}

		if (session.getState() == State.DISCONNECTED || numRead <= 0) { return 0; }

		readBuffer.flip();

		String tmpStr = new String(readBuffer.array(), 0, numRead);

		// read did not contain a \r\n
		if (tmpStr.indexOf("\r\n") == -1)
		{
			// append whole thing to buffer and set fragment flag
			stringBuff.append(tmpStr);
			gotFragment = true;

			return numRead;
		}

		// this read had a \r\n in it

		if (gotFragment)
		{
			// prepend fragment to front of current message
			tmpStr = stringBuff.toString() + tmpStr;
			stringBuff.delete(0, stringBuff.length());
			gotFragment = false;
		}

		String[] strSplit = tmpStr.split("\r\n");

		for (int i = 0; i < (strSplit.length - 1); i++)
		{
			manager.addToEventQueue(createDefaultIRCEvent(strSplit[i]));
		}

		String last = strSplit[strSplit.length - 1];

		if (!tmpStr.endsWith("\r\n"))
		{
			// since string did not end with \r\n we need to
			// append the last element in strSplit to a stringbuffer
			// for next read and set flag to indicate we have a fragment waiting
			stringBuff.append(last);
			gotFragment = true;
		}
		else
		{
			manager.addToEventQueue(createDefaultIRCEvent(last));
		}

		return numRead;
	}

	/**
	 * Writes all requests in queue to server
	 * 
	 * @return number bytes written
	 */
	int doWrites()
	{
		int amount = 0;

		List<WriteRequest> tmpReqs = new ArrayList<WriteRequest>();
		synchronized (writeRequests)
		{
			tmpReqs.addAll(writeRequests);
			writeRequests.clear();
		}

		for (WriteRequest request : tmpReqs)
		{
			String data;

			if (request.getType() == WriteRequest.Type.CHANNEL_MSG)
			{
				data = "PRIVMSG " + request.getChannel().getName() + " :" + request.getMessage() + "\r\n";
			}
			else if (request.getType() == WriteRequest.Type.PRIVATE_MSG)
			{
				data = "PRIVMSG " + request.getNick() + " :" + request.getMessage() + "\r\n";
			}
			else
			{
				data = request.getMessage();
				if (!data.endsWith("\r\n"))
				{
					data += "\r\n";
				}
			}

			byte[] dataArray = data.getBytes();
			ByteBuffer buff = ByteBuffer.allocate(dataArray.length);
			buff.put(dataArray);
			buff.flip();

			try
			{
				amount += socChannel.write(buff);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				session.disconnected();
			}

			if (session.getState() == State.DISCONNECTED) { return amount; }

			fireWriteEvent(request);
		}

		return amount;
	}

	/**
	 * Send a ping
	 */
	void ping()
	{
		writeRequests.add(new WriteRequest("PING " + actualHostName + "\r\n", session));
		session.pingSent();
	}

	/**
	 * Send a pong
	 * 
	 * @param event , the Ping event
	 */
	void pong(IRCEvent event)
	{
		session.gotResponse();
		String data = event.getRawEventData().substring(event.getRawEventData().lastIndexOf(":") + 1);
		writeRequests.add(new WriteRequest("PONG " + data + "\r\n", session));
	}

	/**
	 * Alert connection a pong was received
	 */
	void gotPong()
	{
		session.gotResponse();
	}

	/**
	 * Close connection
	 * 
	 * @param quitMessage
	 */
	void quit(String quitMessage)
	{
		try
		{
			if (quitMessage == null) quitMessage = "";
			WriteRequest request = new WriteRequest("QUIT :" + quitMessage + "\r\n", session);
			writeRequests.add(request);
			// clear out write queue
			doWrites();
			socChannel.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Fires a write request to all write listeners
	 * 
	 * @param request
	 */
	void fireWriteEvent(WriteRequest request)
	{
		for (WriteRequestListener listener : manager.getWriteListeners())
		{
			listener.receiveEvent(request);
		}
	}
	
	/**
	 * Create a default irc event
	 * 
	 * @param rawData
	 * @return
	 */
	private IRCEvent createDefaultIRCEvent(final String rawData)
	{
		return new IRCEvent()
		{

			public Session getSession()
			{
				return session;
			}

			public String getRawEventData()
			{
				return rawData;
			}

			public Type getType()
			{
				return IRCEvent.Type.DEFAULT;
			}
		};
	}

}
