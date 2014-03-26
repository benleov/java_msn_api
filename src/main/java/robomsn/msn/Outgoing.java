package robomsn.msn;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 */
public class Outgoing
{
    private Socket connection;	// the socket with the notification server
    private static Logger log = Logger.getLogger(Outgoing.class);

    public Outgoing(Socket connection)
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
            PrintWriter pw =
                    new PrintWriter(
                    new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            
            pw.write(s);
            pw.flush();

            log.debug(">>> " + s);
        }
        catch (Exception e)
        {
            log.error(e);
            System.exit(0);
        }
    }
}
