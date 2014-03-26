package robomsn.msn;

import java.util.Vector;

/**
 */
public class IncomingMessageList extends Vector<IncomingMessage>
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 7700045167845867322L;

	public IncomingMessageList()
    {
        super();
    }
    
    public String toString()
    {
    	String toString = "";
        for (int i = 0; i < this.size(); i++)
        {
            toString += "[" + 
            this.get(i).getTime() + "] " + 
            this.get(i).getMsg();
        }
        return toString;
    }
}
