package robomsn.msn;

public class OutgoingMessage
{
    private String message;
    private String justmessage;

    public OutgoingMessage()
    {
        message = "";
    }

    public OutgoingMessage(String s)
    {
        message = "";
        justmessage = s;

        int payloadsize = 0;
        String payloadl1 = "MIME-Version: 1.0\r\n";
        String payloadl2 = "Content-Type: text/plain; charset=UTF-8\r\n";
        String payloadl3 = "X-MMS-IM-Format: FN=Arial; EF=I; CO=0; CS=0; PF=22\r\n";
        String payloadl4 = "\r\n";
        String payloadl5 = s;

        String payload = payloadl1 + payloadl2 + payloadl3 + payloadl4 + payloadl5;

        payloadsize = payload.length();

        String line1 = "MSG " + RoboMSN.getTrID() + " A " + payloadsize + "\r\n";

        message = line1 + payload;
    }

    public String getPayloadMessage()
    {
        return message;
    }

//	MSG 4 A 133\r\n
//	MIME-Version: 1.0\r\n
//	Content-Type: text/plain; charset=UTF-8\r\n
//	X-MMS-IM-Format: FN=Arial; EF=I; CO=0; CS=0; PF=22\r\n
//	\r\n
//	Hello! How are you?
    
    /**
     * @return Message Content
     */
    public String getMessageContent()
    {
        return justmessage;
    }
}
