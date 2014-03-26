package robomsn.data;


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/*
 * Created on 9 avr. 2004
 *
 */
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import robomsn.msn.IncomingMessageList;
import robomsn.msn.OutgoingMessage;
import robomsn.msn.RoboMSN;
import robomsn.msn.SwitchBoardServerIncoming;
import robomsn.msn.SwitchBoardServerOutgoing;

/**
 */
public class Chat
{

	private static Logger log = Logger.getLogger(Chat.class);
	
    private RoboMSN msn;
    private Socket serversocket;
    private SwitchBoardServerIncoming incoming;
    private SwitchBoardServerOutgoing outgoing;
    private boolean connected;
    private String chatid;
    private String serverurl;
    private String serverport;
    private ContactList participants;
    private IncomingMessageList incomingmessagelist;
    private boolean hasSwitchboardServer;	// used to synchronize
    private boolean hasJoined;				// used to synchronize
    private boolean chatWindowOpen;			// true if i need to reconnect with SBS, but
    private ChatListener _listener;
    private Timer _timeout;
    public static final long CONNECT_TIMEOUT = 10000;

    public void setListener(ChatListener listener)
    {
        _listener = listener;
    }

    public void newChat(RoboMSN msn, Contact contact)
    {
        this.msn = msn;
        participants = new ContactList();
        participants.add(contact);

        incomingmessagelist = new IncomingMessageList();

        hasSwitchboardServer = false;
        setConnected(false);
    }

    public void updateChat()
    {
        hasSwitchboardServer = false;
        setConnected(false);
    }

    /**
     * stops chat
     */
    public void stop()
    {
        setConnected(false);
        msn.setStatus(RoboMSN.Status.AVAILABLE);   // WHY is this here??
        outgoing.send("OUT\r\n");
        closeSocket();
    }

    public void closeSocket()
    {
        try
        {
            serversocket.close();
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }

    /** starts timeout; if nobody has joined the chat within
     * CONNECT_TIMEOUT then listeners will be notified of chatTimeout event
     */
    public void connectNewChat()
    {
        _timeout = new Timer();
        _timeout.schedule(new TimeoutTask(this), CONNECT_TIMEOUT);

        //	>>> XFR 15 SB\r\n

        //Out.write("Chat - connectNewChat - setting new msn status");
        msn.getStatus().setChatUser( participants.get(0).getUsername() );
        
        int id = RoboMSN.getTrID();

        // sending to the server that i want one!
        //Out.write("Chat - connectNewChat - requesting session board");
        msn.requestSessionBoard(id, this);

        // hasSwitchBoardServer = false while i dont have a switchboardserver!

        while (!hasSwitchboardServer)
        {
            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e1)
            {
                log.error(e1);
            }
        }

        try
        {
            serversocket = new Socket(serverurl, Integer.parseInt(serverport));
            setConnected(true);

        }
        catch (NumberFormatException e)
        {
            log.error(e);
        }
        catch (UnknownHostException e)
        {
            log.error(e);
        }
        catch (IOException e)
        {
            log.error(e);
        }

        // Socket is open, gotta open channels !
        if (isConnected())
        {
            incoming = new SwitchBoardServerIncoming(msn, serversocket, this);
            outgoing = new SwitchBoardServerOutgoing(serversocket);
        }

        // send user info
        if (isConnected())
        {
            this.sendUserInfo();
            incoming.getALine();
        }

        // invite buddy !
        if (isConnected())
        {
            this.inviteBuddy(participants.get(0).getUsername());

            // and launch automatic listening
            incoming.startAutomaticListening();
        }

        // setting status to the user
        if (isConnected())
        {
            msn.getStatus().setChatUser(participants.get(0).getUsername());
        }
        else
        {
            msn.setStatus(RoboMSN.Status.AVAILABLE);
        }

        if (!this.chatWindowOpen)
        {
            log.debug("<-------------- You are chatting with " + this.getUserName() + "-------------->");
            log.debug("");

        }

        notifyListenersConnected();
    }

    public void connectExistingChat(String adress, String port, String sessionID, String IDString)
    {
        // received :<<< RNG 11752013 207.46.108.38:1863 CKI 849102291.520491113 example@passport.com Example%20Name\r\n
        // to be sent : >>> ANS 1 name_123@hotmail.com 849102291.520491113 11752013\r\n

        this.hasSwitchboardServer = true;
        try
        {
            //Out.write("Chat : connecting existing chat " + adress + " ...");

            serversocket = new Socket(adress, Integer.parseInt(port));
            setConnected(true);

        //Out.write(" ... connected !");
        }
        catch (NumberFormatException e)
        {
            log.error(e);
        }
        catch (UnknownHostException e)
        {
            log.error(e);
        }
        catch (IOException e)
        {
            log.error(e);
        }

        if (isConnected())
        {
            incoming = new SwitchBoardServerIncoming(msn, serversocket, this);
            outgoing = new SwitchBoardServerOutgoing(serversocket);
        }

        // need to send "ANS"
        // send user info
        if (isConnected())
        {
            msn.getChatlist().add(this);
            if (msn.getStatus().equals(this.getUserName()))
            {
                log.debug("<-------------- You are chatting with " + this.getUserName() + "-------------->");
                log.debug("");
            }

            this.sendAnsInfo(sessionID, IDString);
            incoming.startAutomaticListening();

        }

        notifyListenersConnected();

    }

    public void setHasJoined(boolean b)
    {
        this.hasJoined = b;
        _timeout.cancel();
        notifyListenersJoined();
    }

    /**
     * 
     */
    private void sendAnsInfo(String sessionID, String IDString)
    {
        //<<< RNG 11752013 207.46.108.38:1863 CKI 849102291.520491113 example@passport.com Example%20Name\r\n
        //>>> ANS 1 name_123@hotmail.com 849102291.520491113 11752013\r\n
        outgoing.send("ANS 1 " + msn.getUsername() + " " + IDString + " " + sessionID + "\r\n");
    }

    public void setChatId(String s)
    {
        chatid = s;
    }

    public void sendUserInfo()
    {
        // i have to send USR 1 example@passport.com 17262740.1050826919.32308\r\n
        outgoing.send("USR " + RoboMSN.getTrID() + " " + msn.getUsername() + " " +
                chatid + "\r\n");
    }

    public void inviteBuddy(String username)
    {
        // >>> CAL 2 name_123@hotmail.com\r\n
        outgoing.send("CAL " + RoboMSN.getTrID() + " " + username + "\r\n");
    }

    public void sendMessage(String message)
    {
        try
        {
            if (isConnected())
            {
                OutgoingMessage outmsg = new OutgoingMessage(message);
                outgoing.send(outmsg.getPayloadMessage());
            }
            else
            {
                log.debug("Chat - sendMessage - need to reconnect !!!");

                this.hasJoined = false;
                this.chatWindowOpen = true;
                this.updateChat();
                this.connectNewChat();

                while (!this.hasJoined)
                {
                    log.debug("Chat - sendMessage - user has not joined the chat yet!!!");
                    // user has not joined the chat yet, i should wait !
                    this.wait(300);
                }
                this.sendMessage(message);
            }
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }

    /**
     * @return
     */
    public ContactList getParticipants()
    {
        return participants;
    }

    /**
     * @return
     */
    public boolean isConnected()
    {
        boolean connected;
        
        try
        {
            connected = !serversocket.isClosed();
        }
        catch (Exception e)
        {
            log.error(e);
            connected = false;
        }
        this.connected = connected;
        return connected;
    }

    /**
     * @param b
     */
    public void setConnected(boolean b)
    {
        connected = b;
    }

    /** called by timer task when connect is taking too long */
    private void notifyTimeout(TimeoutTask timeoutTask)
    {
        notifyListenersTimeout();

    }

    private void notifyListenersTimeout()
    {
        log.debug("Chat - notifying listeners TIMOUT");
        if (_listener != null)
        {
            _listener.chatTimeout(this);
        }
    }

    private void notifyListenersJoined()
    {
        log.debug("Chat - notifying listeners joined");
        if (_listener != null)
        {
            _listener.chatJoined(this, connected);
        }
    }

    private void notifyListenersConnected()
    {
        log.debug("Chat - notifying listeners");
        if (_listener != null)
        {
            log.debug("Chat - listeners notified!!");
            _listener.chatConnected(this, connected);
        }
    }

    /**
     * @return
     */
    public IncomingMessageList getIncomingmessagelist()
    {
        return incomingmessagelist;
    }

    /**
     * 
     */
//    public void writeUnread()
//    {
//        // displays unread messages to the user
//        //Out.write("Chat - writeUnread - writing unreads!");
//        IncomingMessage in;
//
//        //Out.write("Chat - writeUnread - there are " + this.numberOfUnread() + " messages");
//
//        for (int i = 0; i < this.getIncomingmessagelist().size(); i++)
//        {
//            in = this.getIncomingmessagelist().get(i);
//            if (!in.isMessageDisplayed())
//            {
//                in.printMessage();
//                in.setMessageDisplayed(true);
//            }
//        }
//    }

    public void writeAll()
    {
        log.debug("<---------- History of chats with " + this.getUserName() + " ---------->");
        log.debug("");
        log.debug(this.getIncomingmessagelist().toString());
        log.debug("<------------------- ------------------->");
    }

    public String getUserName()
    {
        // returns the first username
        return this.getParticipants().get(0).getUsername().trim();
    }

    /**
     * @param adress
     * @param port
     * @param sessionID
     * @param IDString
     */
    public void reconnect(String adress, String port, 
    		String sessionID, String IDString)
    {
        this.connectExistingChat(adress, port, sessionID, IDString);

    }

    public boolean hasSwitchboardServer()
    {
        return hasSwitchboardServer;
    }

    public void setSwitchBoardServer(String url, String port)
    {
        serverurl = url;
        serverport = port;
        hasSwitchboardServer = true;
    }

    public boolean isChatWindowOpen()
    {
        return chatWindowOpen;
    }

    public void setChatWindowOpen(boolean open)
    {
        chatWindowOpen = open;
    }

    private class TimeoutTask extends TimerTask
    {

        private Chat _chat;

        public TimeoutTask(Chat chat)
        {
        	super();
            _chat = chat;
        }

        @Override
        public void run()
        {
            _chat.notifyTimeout(this);
        }
    }
}
