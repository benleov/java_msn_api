package robomsn.data;

import java.util.Vector;

import org.apache.log4j.Logger;

import robomsn.util.Filter;
import robomsn.util.ListFilterer;

public class ChatList extends Vector<Chat>
{

    /**
     * 
     */
    private static final long serialVersionUID = 7600033469308238485L;
    
    private static Logger log = Logger.getLogger(ChatList.class);

    public ChatList()
    {
        super();
    }

    /**
     * returns the first chat that has no switchboard server
     */
    public Chat getChatWithNoServer()
    {

        Chat c = null;
        for (int i = 0; i < this.size(); i++)
        {
            c = this.get(i);
            if (!c.hasSwitchboardServer())
            {
                i = this.size();
            }
        }
        return c;
    }

    /** returns the chat where the username user is chatting!
    null if there is no chat */
    public Chat getChat(String username)
    {
        for (int i = 0; i < this.size(); i++)
        {
            Chat c = this.get(i);
            if (c.getParticipants().exists(username))
            {
                return c;
            }
        }

        log.debug("ChatList: chat not found: " + username);
        return null;
    }

    // return true if a chat exists
    public boolean exists(String username)
    {
        return this.getChat(username) != null;
    }

    /**
     * replace the old chat with the new one
     */
    public void replaceChat(Chat nchat)
    {
        Chat ochat = this.getChat(nchat.getUserName());
        this.remove(ochat);
        this.add(nchat);
    }

    public void displayChatsInfo()
    {
        this.removeClosedChats();

        log.debug("<---------- Chat infos ---------->");
        log.debug("");
        log.debug("There are " + this.size() + " chats running");
        log.debug("");

//        for (int i = 0; i < this.size(); i++)
//        {
//            Chat c = this.get(i);
//            
//            if (c.getIncomingmessagelist().hasUnreadMessages())
//            {
//                log.debug(i + " : " + this.get(i).getUserName() + " [" + c.numberOfUnread() + " new message]");
//            }
//            else
//            // no unread messages
//            {
//                log.debug(i + " : " + this.get(i).getUserName());
//            }
//        }

        log.debug("");
        // log.debug("You have " + this.newMessagesNumber() + " chats with unread messages");
        log.debug("");
        log.debug("<------- End of Chat infos ------->");
    }

    /**
     * 
     */
    private void removeClosedChats()
    {
	ListFilterer.removeAll(this, new Filter()
	{
	    public boolean matches(Object obj)
	    {
		return !((Chat) obj).isConnected();
	    }
	});
    }

    public void removeChat(final String username)
    {
	ListFilterer.removeAll(this, new Filter()
	{
	    public boolean matches(Object obj)
	    {
		return !((Chat) obj).getUserName().equals(username);
	    }
	});
    }
}
