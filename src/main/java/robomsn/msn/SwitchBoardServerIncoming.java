package robomsn.msn;

import robomsn.data.Chat;
import robomsn.msn.IncomingMessage.Type;
import robomsn.util.StringUtil;
import robomsn.util.StringList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.log4j.Logger;

/*
 */
public class SwitchBoardServerIncoming extends Thread
{
	
	private static Logger log = Logger.getLogger(SwitchBoardServerIncoming.class);
    private BufferedReader br;	// the input bufferedreader
    private RoboMSN msn;
    private Chat chat;			// the chat

    //private int contactnumber;	// to remember how many contacts have been found !
    public SwitchBoardServerIncoming(RoboMSN msn, 
    		Socket connection, Chat chat)
    {
        this.msn = msn;
        this.chat = chat;

        try
        {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        }
        catch (IOException e)
        {
            log.error(e);
            chat.stop();
            msn.getChatlist().remove(chat);
        }
    }

    public void startAutomaticListening()
    {
        this.start();
    }

    public void run()
    {
        String inputline;

        while (chat.isConnected())
        {
            // read a line
            inputline = this.getALine();

            if (inputline != null)
            {
                this.parseLine(inputline);
            }
            else
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    log.error(e);
                }
            }
        }
    }

    public String getALine()
    {
        // returns the next incoming string
        String in = null;

        try
        {
            if (chat.isConnected())
            {
                in = br.readLine();
                if (in == null)
                {
                    log.debug("SwitchBoardServerIncoming - getALine - null line received");
                    chat.stop();
                }
                log.debug("<<< " + in);
            }

        }
        catch (Exception e)
        {
            log.error(e);
        }

        return in;
    }

    public void parseLine(String l)
    {
        StringList t = new StringList(l);

        try
        {
            if (l == null)
            {
                log.debug("SwitchBoardServerIncoming - parseLine : null line received :-/");
            }

            if (l.startsWith("CAL"))
            {
                this.callingUser(t);
            }

            if (l.startsWith("JOI"))
            {
                this.userJoining(t);
            }

            if (l.startsWith("MSG"))
            {
                //je suis ici ... 
                this.newIncomingMessage(l);
            }

            if (l.startsWith("ANS"))
            {
                //connected to the switchboard server!
                this.connectedSBS(t);
            }

            if (l.startsWith("BYE"))
            {
                // a user is getting off the chat!
                this.disconnectUser(t);
            }
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }

    /**
     * @param t
     */
    private void disconnectUser(StringList t)
    {
        // the user has just gone off the chat!
        //<<< BYE mtownbelgium@hotmail.com

        log.debug("");
        log.debug("[ " + t.get(1) + " ] has quit the chat");

        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            log.error(e);
        }

        String username = t.get(1);
        Chat c = msn.getChatlist().getChat(username);
        msn.getChatlist().getChat(username).closeSocket();
    }

    /**
     * @param t
     */
    private void connectedSBS(StringList t)
    {
        // <<< ANS 1 OK\r\n
        try
        {
            if (t.get(2).equals("OK"))
            {
                log.debug("Connected to chat");
            }
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }

    public void callingUser(StringList t)
    {
        //CAL 2 RINGING 11752013\r\n
        log.debug("Requesting chat ... ");
    }

    public void userJoining(StringList t)
    {
        // <<< JOI name_123@hotmail.com Name_123\r\n
        chat.setHasJoined(true);
        log.debug(t.get(2) + " [ " + t.get(1) + " ] has joined the chat");
    }

    public String getMessageLine()
    {
        char[] chartab = new char[1664];
        try
        {
            br.read(chartab);
        }
        catch (Exception e)
        {
            log.error(e);
        }

        String s = "";
        for (int i = 0; i < 1664; i++)
        {
            s = s + chartab[i];
        }

        return s.trim();
    }

    public void newIncomingMessage(String line)
    {
        //	MSG example@passport.com Mike 133\r\n
        //	MIME-Version: 1.0\r\n
        //	Content-Type: text/plain; charset=UTF-8\r\n
        //	X-MMS-IM-Format: FN=Arial; EF=I; CO=0; CS=0; PF=22\r\n
        //	\r\n
        //	Hello! How are you?

//		It's very important that a client pareses incoming data 
//		from the switchboard by looking at message lengths. Messages, 
//		like every other command, can be split up into multiple 
//		packets are combined with other commands into one packet. 
//		You absolutely must read the specified message length, and 
//		read that many bytes out of the socket after the initial newline.

        int length;

        line = StringUtil.removeNewLine(line);
        StringList t = new StringList(line);
        
        try
        {
            length = Integer.parseInt(t.get(t.size() - 1));
        }
        catch (NumberFormatException e)
        {
            length = 0;
            log.error(e);
        }
        
        String in = line + " " + this.readBytes(length);
        in = StringUtil.removeNewLine(in);

        IncomingMessage inmsg = new IncomingMessage(in);

        if (inmsg.getType() == Type.MESSAGE)
        {
            chat.getIncomingmessagelist().add(inmsg);
        }

        if (msn.getStatus().equals(inmsg.getUsername()))
        {

            if (inmsg.getType()  == Type.MESSAGE)
            {
                inmsg.printMessage();
            }
        }
    }

    /**
     * @param length
     * @return
     */
    private String readBytes(int length)
    {
        char[] ct = new char[length];

        try
        {
            br.read(ct);
        }
        catch (IOException e)
        {
            log.error(e);
        }

        String s = "";

        try
        {
            for (int i = 0; i < ct.length; i++)
            {
                s = s + ct[i];
            }
        }
        catch (Exception e1)
        {
            log.error(e1);
        }

        return s;
    }
}
