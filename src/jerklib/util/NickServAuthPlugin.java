package jerklib.util;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.IRCEvent.Type;
import jerklib.events.modes.ModeAdjustment;
import jerklib.events.modes.ModeEvent;
import jerklib.events.modes.ModeAdjustment.Action;
import jerklib.tasks.TaskImpl;

/**
 * 
 * A Task to identify with NickServ and then join a list of channels names.
 * Once the Task has succsessfully identifed with NickServ
 * TaskCompletion Listeners will be notified with a true Boolean object.
 * <p>
 * If 40 seconds passes and the mode event to indicate ident success has not
 * been received, TaskCompletion Listeners will be notified with a false Boolean object.
 * <p>
 * This plugin assumes Nickserv responds to the following syntax
 * <p>
 * "identify password"
 * <p>
 * To cancel this Task call cancel()
 * <p>
 * <b>To Use This Task</b>: You must use onEvent(Task , Type ... type) to add the Task
 * You must pass Type.CONNECT_COMPLETE and Type.MODE_EVENT. Example of adding the Task:
 * <p>
 * session.onEvent(auth, Type.CONNECT_COMPLETE , Type.MODE_EVENT);
 * <p>
 * Example Code:
 * <p>
<pre> 		
 		final NickServAuthPlugin auth = new NickServAuthPlugin
		(
			"letmein", //password
			'e', //mode char that indicates success
			session, //session
			Arrays.asList("#jerklib" , "##swing") // list of channels to join on success
		);
		
		auth.addTaskListener(new TaskCompletionListener()
		{
			public void taskComplete(Object result)
			{
				if(result.equals(new Boolean(false)))
				{
					conman.quit();
				}
				else
				{
					System.out.println("Authed!");
				}
			}
		});
		
		session.onEvent(auth, Type.CONNECT_COMPLETE , Type.MODE_EVENT);
</pre>
 * 
 * 
 * @see Session#onEvent(jerklib.tasks.Task, jerklib.events.IRCEvent.Type...)
 * @see Type
 * @author mohadib
 *
 */
public class NickServAuthPlugin extends TaskImpl
{
	private final Session session;
	private final String pass;
	private final char identMode;
	private final List<String> channels;
	private boolean authed;
	
	/**
	 * @param pass - nickserv password
	 * @param identMode - mode that indicates ident success
	 * @param session - Session this Task is attatched to
	 * @param channels - A list of channel names to join on ident success
	 */
	public NickServAuthPlugin
	(
		String pass , 
		char identMode,
		Session session,
		List<String>channels
	)
	{
		super("NickServAuth");
		this.pass = pass;
		this.identMode = identMode;
		this.session = session;
		this.channels = channels;
	}
	
	/* (non-Javadoc)
	 * @see jerklib.listeners.IRCEventListener#receiveEvent(jerklib.events.IRCEvent)
	 */
	public void receiveEvent(IRCEvent e)
	{
		if(e.getType() == Type.CONNECT_COMPLETE)connectionComplete(e);
		else if(e.getType() == Type.MODE_EVENT)mode(e);
	}
	
	private void mode(IRCEvent e)
	{
		ModeEvent me = (ModeEvent)e;
		if(me.getModeType() == ModeEvent.ModeType.USER)
		{
			for(ModeAdjustment ma : me.getModeAdjustments())
			{
				if(ma.getMode() == identMode && ma.getAction() == Action.PLUS)
				{
					authed = true;
					joinChannels();
					taskComplete(new Boolean(true));
				}
			}
		}
	}
	
	private void connectionComplete(IRCEvent e)
	{
		authed = false;
		e.getSession().sayPrivate( "nickserv" , "identify " + pass);
		final Timer t = new Timer();
		t.schedule(new TimerTask()
		{
			public void run()
			{
				if(!authed)
				{
					taskComplete(new Boolean(false));
				}
				this.cancel();
				t.cancel();
			}
		}, 40000 );
	}
	
	private void joinChannels()
	{
		for(String name : channels)
		{
			session.join(name);
		}
	}
	
}
