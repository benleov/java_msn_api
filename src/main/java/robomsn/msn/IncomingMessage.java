package robomsn.msn;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;

import robomsn.util.TimeUtil;
import robomsn.util.StringList;

/**
 */
public class IncomingMessage
{
    
// sample message
//	MSG example@passport.com Mike 133\r\n
//	MIME-Version: 1.0\r\n
//	Content-Type: text/plain; charset=UTF-8\r\n
//	X-MMS-IM-Format: FN=Arial; EF=I; CO=0; CS=0; PF=22\r\n
//	\r\n
//	Hello! How are you?
    
	public enum Type
	{
		MESSAGE, TYPING;
	}
	
	private static Logger log = Logger.getLogger(IncomingMessage.class);
	
    private String payload;
    private String msg;
    private String username;
    private String displayname;
    private String time;
    private boolean messagedisplayed;	// true if the message has been displayed
    private boolean typingdisplayed;
    private Type type;
    
    public IncomingMessage(String payload, String msg)
    {
        // payload is everything before the message
        // message is the last line 

        this.payload = payload;
        this.msg = msg;

        StringList t = new StringList(payload);

        username = t.get(1);
        displayname = t.get(2);

        time = TimeUtil.getTime();
        //Out.write("IncomingMessage - constructor : msg = " + msg);

        type = Type.MESSAGE;

        messagedisplayed = false;
    }

    public IncomingMessage()
    {
        payload = "";
        msg = "";
    }

    public IncomingMessage(String message)
    {
        StringList t = new StringList(message);
        this.username = t.get(1);
        this.displayname = t.get(2);

        this.time = TimeUtil.getTime();

        if ((t.stringExists("TypingUser:")) && (t.stringExists(username.trim())))
        {
            this.type = Type.TYPING;
        }

        if (t.stringExists("X-MMS-IM-Format"))
        {
            this.type = Type.MESSAGE;
            this.msg = "";
            for (int i = t.indexOf("PF=22") + 1; i < t.size(); i++)
            {
                msg = msg + " " + t.get(i);
            }

        }


        messagedisplayed = false;
    }

    public void addPayload(String line)
    {
        payload = line;
    }

    public void addLine(String line)
    {
        msg = msg + line;
    }

    public void construct()
    {
        StringList t = new StringList(payload);
        username = t.get(1);
        displayname = t.get(2);

        time = TimeUtil.getTime();

        if (t.stringExists("TypingUser: " + username))
        {
            this.type = Type.TYPING;
        }

        if (t.stringExists("X-MMS-IM-Format"))
        {
            this.type = Type.MESSAGE;
        }

        messagedisplayed = false;

    }

    public void printMessage()
    {
        log.debug("[" + time + "] [ " + username + " ] : " + msg);
        messagedisplayed = true;
    }

    public void printAll()
    {
        log.debug("IncomingMessage - payload = " + payload);
        log.debug("IncomingMessage - msg = " + msg);
        log.debug("IncomingMessage - username = " + username);
        log.debug("IncomingMessage - displayname = " + displayname);
        log.debug("IncomingMessage - time = " + time);
        log.debug("IncomingMessage - type = " + type);
        log.debug("IncomingMessage - messagedisplayed = " + messagedisplayed);
        log.debug("IncomingMessage - typingdisplayed = " + typingdisplayed);
    }

    /**
     * @return
     */
    public String getDisplayname()
    {
        return displayname;
    }

    /**
     * @return
     */
    public String getMsg()
    {
        return msg;
    }

    public Type getType()
    {
    	return type;
    }
    
    /**
     * @return
     */
    public String getUsername()
    {
        return username;
    }

    public String getTime()
    {
        return time;
    }
}
