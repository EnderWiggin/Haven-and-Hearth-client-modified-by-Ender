package jerklib.examples;

import jerklib.Channel;
import jerklib.ConnectionManager;
import jerklib.Profile;
import jerklib.Session;
import jerklib.events.ConnectionCompleteEvent;
import jerklib.events.JoinCompleteEvent;
import jerklib.events.MessageEvent;
import jerklib.listeners.DefaultIRCEventListener;

public class DefaultListenerExample extends DefaultIRCEventListener implements Runnable
{
    public DefaultListenerExample()
    {
    }

    Session session;
    //TODO: change this as to not spam our channel
    static final String CHANNEL_TO_JOIN = "#jerklib";

    public void run()
    {
        ConnectionManager manager = new ConnectionManager(new Profile("ble", "ble", "ble_", "ble__"));

        session = manager.requestConnection("irc.freenode.net");

        session.addIRCEventListener(this);
    }

    @Override
    protected void handleJoinCompleteEvent(JoinCompleteEvent event)
    {
        event.getChannel().say("Hello from BaseListenerExample");
    }

    @Override
    protected void handleConnectComplete(ConnectionCompleteEvent event)
    {
        event.getSession().join(CHANNEL_TO_JOIN);
    }

    @Override
    protected void handleChannelMessage(MessageEvent event)
    {
        log.info(event.getChannel().getName() + ":" + event.getNick() + ":" + event.getMessage());
        if ("now die".equalsIgnoreCase(event.getMessage()))
        {
            event.getChannel().say("Okay, fine, I'll die");
            try
            {
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                // *nothing*
            }
            System.exit(0);
        }
    }

    public static void main(String[] args)
    {
        DefaultListenerExample ble = new DefaultListenerExample();
        Thread t = new Thread(ble);
        t.start();
        try
        {
            Thread.sleep(30000L); // give it the axe in 30!
        }
        catch (InterruptedException e)
        {
            // *nothing*
        }
        ble.sayGoodbye();
        try
        {
            Thread.sleep(5000); // let the message be written!
        }
        catch (InterruptedException e)
        {
            // *nothing*
        }
        System.exit(0);
    }

    private void sayGoodbye()
    {
        for (Channel chan : session.getChannels())
        {
            chan.say("I'm melting! (built-in sword of Damocles... or bucket of water, whatever)");
        }
    }
}
