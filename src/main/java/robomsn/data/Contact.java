package robomsn.data;

import robomsn.util.StringUtil;

public class Contact
{
    /** MSN protocol status */
    public enum Status
    {

        ONLINE("NLN", "online"),
        BUSY("BSY", "busy"),
        IDLE("IDL", "idle"),
        BE_RIGHT_BACK("BRB", "be right back"),
        AWAY("AWY", "away"),
        ON_THE_PHONE("PHN", "on the phone"),
        OUT_TO_LUNCH("LUN", "out to lunch"),
        OFFLINE("", "offline");    // never get sent this status
        private String _protocol;
        private String _name;

        /** @param protocol is the protocol status string
        that we receive from the server */
        private Status(String protocol, String name)
        {
            _protocol = protocol;
            _name = name;
        }

        @Override
        public String toString()
        {
            return _name;
        }

        public String getName()
        {
            return _name;
        }
        
        public String getProtocol()
        {
            return _protocol;
        }

        public static Status getStatusFromName(String name)
        {
            if (name != null)
            {
                for (Status curr : Status.values())
                {
                    if (name.equals(curr.toString()))
                    {
                        return curr;
                    }
                }
            }
            return null;
        }

        public static Status getStatusFromProtocol(String s)
        {
            if (s != null)
            {
                for (Status curr : Status.values())
                {
                    if (s.equals(curr.getProtocol()))
                    {
                        return curr;
                    }
                }
            }
            return null;
        }
    }

    private String userName;
    private String displayName;
    private Status status;
    private int groupnumber;	// blocked group is "99"
    
    public Contact(String realName, String displayName, 
            Status status, int group)
    {
        this.userName = StringUtil.setblanks(realName);
        this.displayName = StringUtil.setblanks(displayName);
        this.status = (status == null) ? Status.OFFLINE : status;
        this.groupnumber = group;
    }

    @Override
    public String toString()
    {
        return "displayName: " + displayName + " [ " + userName + " ] " + " : status: " + status;
    }

    /**
     * @return
     */
    public String getDisplayname()
    {
        return displayName;
    }

    /**
     * @return
     */
    public int getGroupnumber()
    {
        return groupnumber;
    }

    /**
     * @return
     */
    public Status getStatus()
    {
        return status;
    }

    /**
     * @param string
     */
    public void setDisplayname(String string)
    {
        displayName = string;
    }

    /**
     * @param string
     */
    public void setGroupnumber(int group)
    {
        groupnumber = group;
    }

    /**
     * @param string
     */
    public void setStatus(Status stat)
    {
        status = stat;
    }
    
    public String getUsername()
    {
        return userName;
    }
    public int getGroupNumber()
    {
        return groupnumber;
    }
    
    public boolean isBlocked()
    {
        return groupnumber == 99;
    }
    
}
