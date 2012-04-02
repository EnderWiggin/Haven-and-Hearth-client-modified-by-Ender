package jerklib.listeners;

import jerklib.events.*;
import jerklib.events.IRCEvent.Type;
import jerklib.events.dcc.DccEvent;
import jerklib.events.modes.ModeEvent;

import java.util.logging.Logger;

public abstract class DefaultIRCEventListener implements IRCEventListener
{
    protected Logger log = Logger.getLogger(this.getClass().getName());

    public void receiveEvent(IRCEvent e)
    {
        Type t = e.getType();
        boolean handled = handleChannelEvents(t, e);
        handled |= handleServerEvents(t, e);
        handled |= handleOnChannelEvents(t, e);
        if (!handled)
        {
            log.info("Unhandled event: " + e.getRawEventData());
        }
    }

    protected boolean handleChannelEvents(Type t, IRCEvent e)
    {
        // things that happen ON a channel
        if (Type.TOPIC.equals(t))
        {
            handleTopicEvent((TopicEvent) e);
            return true;
        }
        if (Type.AWAY_EVENT.equals(t))
        {
            handleAwayEvent((AwayEvent) e);
            return true;
        }
        if (Type.CHANNEL_LIST_EVENT.equals(t))
        {
            handleChannelListEvent((ChannelListEvent) e);
            return true;
        }
        if (Type.CHANNEL_MESSAGE.equals(t))
        {
            handleChannelMessage((MessageEvent) e);
            return true;
        }
        if (Type.NICK_CHANGE.equals(t))
        {
            handleNickChangeEvent((NickChangeEvent) e);
            return true;
        }
        if (Type.NICK_IN_USE.equals(t))
        {
            handleNickInUseEvent((NickInUseEvent) e);
            return true;
        }
        if (Type.PRIVATE_MESSAGE.equals(t))
        {
            handlePrivateMessage((MessageEvent) e);
            return true;
        }
        return false;
    }

    protected boolean handleServerEvents(Type t, IRCEvent e)
    {
        // things that happen on a server
        if (Type.CONNECT_COMPLETE.equals(t))
        {
            handleConnectComplete((ConnectionCompleteEvent) e);
            return true;
        }
        if (Type.CTCP_EVENT.equals(t))
        {
           	handleCtcpEvent((CtcpEvent) e);	
            return true;
        }
        if (Type.DCC_EVENT.equals(t))
        {
        		handleDccEvent((DccEvent) e);
            return true;
        }
        if (Type.ERROR.equals(t))
        {
            handleErrorEvent((ErrorEvent) e);
            return true;
        }
        if (Type.INVITE_EVENT.equals(t))
        {
            handleInviteEvent((InviteEvent) e);
            return true;
        }
        if (Type.JOIN.equals(t))
        {
            handleJoinEvent((JoinEvent) e);
            return true;
        }
        if (Type.JOIN_COMPLETE.equals(t))
        {
            handleJoinCompleteEvent((JoinCompleteEvent) e);
            return true;
        }
        if (Type.MOTD.equals(t))
        {
            handleMotdEvent((MotdEvent) e);
            return true;
        }
        if (Type.NOTICE.equals(t))
        {
            handleNoticeEvent((NoticeEvent) e);
            return true;
        }
        if (Type.SERVER_INFORMATION.equals(t))
        {
            handleServerInformationEvent((ServerInformationEvent) e);
            return true;
        }
        if (Type.SERVER_VERSION_EVENT.equals(t))
        {
            handleServerVersionEvent((ServerVersionEvent) e);
            return true;
        }
        return false;
    }

    protected boolean handleOnChannelEvents(Type t, IRCEvent e)
    {
        // things that happen TO a channel
        if (Type.KICK_EVENT.equals(t))
        {
            handleKickEvent((KickEvent) e);
            return true;
        }
        if (Type.MODE_EVENT.equals(t))
        {
            handleModeEvent((ModeEvent) e);
            return true;
        }
        if (Type.NICK_LIST_EVENT.equals(t))
        {
            handleNickListEvent((NickListEvent) e);
            return true;
        }
        if (Type.PART.equals(t))
        {
            handlePartEvent((PartEvent) e);
            return true;
        }
        if (Type.QUIT.equals(t))
        {
            handleQuitEvent((QuitEvent) e);
            return true;
        }
        if (Type.WHO_EVENT.equals(t))
        {
            handleWhoEvent((WhoEvent) e);
            return true;
        }
        if (Type.WHOIS_EVENT.equals(t))
        {
            handleWhoisEvent((WhoisEvent) e);
            return true;
        }
        if (Type.WHOWAS_EVENT.equals(t))
        {
            handleWhowasEvent((WhowasEvent) e);
            return true;
        }
        return false;
    }

    protected void handleWhowasEvent(WhowasEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleWhoisEvent(WhoisEvent event)
    {
        log.finest(event.getRawEventData());
    }


    protected void handleWhoEvent(WhoEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleTopicEvent(TopicEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleServerVersionEvent(ServerVersionEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handlePrivateMessage(MessageEvent event)
    {
        handleChannelMessage(event);
    }

    protected void handleServerInformationEvent(ServerInformationEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleQuitEvent(QuitEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handlePartEvent(PartEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleNoticeEvent(NoticeEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleNickListEvent(NickListEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleNickInUseEvent(NickInUseEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleNickChangeEvent(NickChangeEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleMotdEvent(MotdEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleModeEvent(ModeEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleKickEvent(KickEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleJoinCompleteEvent(JoinCompleteEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleJoinEvent(JoinEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleInviteEvent(InviteEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleErrorEvent(ErrorEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleCtcpEvent(CtcpEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleDccEvent(DccEvent event)
    {
    		log.finest(event.getRawEventData());
    }
    
    protected void handleConnectComplete(ConnectionCompleteEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleChannelMessage(MessageEvent event)
    {
        log.finest(event.getRawEventData());
    }

    protected void handleChannelListEvent(ChannelListEvent e)
    {
        log.finest(e.getRawEventData());
    }

    protected void handleAwayEvent(AwayEvent e)
    {
        log.finest(e.getRawEventData());
    }
}
