package robomsn.msn;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;


/**
 */
public class SwitchBoardServerOutgoing
{
	
	private static Logger log = Logger.getLogger(SwitchBoardServerOutgoing.class);
    private Socket connection;	// the socket with the switchboard server

    public SwitchBoardServerOutgoing(Socket connection)
    {
        this.connection = connection;
    }

    /**
     * sends the string s to the server
     * @param s
     */
    public void send(String s)
    {
        try
        {
            PrintWriter pw = new PrintWriter(new 
                    OutputStreamWriter(connection.getOutputStream(), "UTF-8"));

            pw.write(s);

            pw.flush();

            log.debug(">>> " + s);


        }
        catch (Exception e)
        {
            log.error(e);
        }
    }

    public void send(OutgoingMessage m)
    {
        try
        {
            PrintWriter pw = new PrintWriter(new 
                    OutputStreamWriter(connection.getOutputStream(), "UTF-8"));

            pw.write(m.getPayloadMessage());

            pw.flush();

            log.debug(">>> " + m.getMessageContent());
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }
}
