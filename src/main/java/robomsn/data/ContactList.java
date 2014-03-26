package robomsn.data;

import java.util.ArrayList;

import robomsn.data.Contact.Status;
import robomsn.util.Filter;
import robomsn.util.ListFilterer;

public class ContactList extends ArrayList<Contact>
{

    private static final long serialVersionUID = -4270777083284109943L;
    private int listNumber;

    // blocked group is 99
    public ContactList()
    {
	super();
    }

    public String toString(String status)
    {
	String toString = "";

	for (int i = 0; i < this.size(); i++)
	{
	    Contact c = this.get(i);
	    if (c.getStatus().toString().equals(status))
	    {
		toString += c.toString();
	    }
	}

	return toString;
    }

    /**
     * returns the contact identified by username if exists null else
     * 
     * @param username
     * @return contact with username
     */
    public Contact getContact(String username)
    {
	for (int i = 0; i < this.size(); i++)
	{
	    Contact c = get(i);

	    if (c.getUsername().equals(username))
	    {
		return c;
	    }
	}

	// Contact not found

	return null;
    }

    public void setContactStatus(String username, String displayname,
	    Status status)
    {
	Contact c = this.getContact(username);
	c.setDisplayname(displayname);
	c.setStatus(status);
    }

    public boolean exists(String username)
    {
	return (this.getContact(username) != null);
    }

    /**
     * returns the number of people online
     */
    public int getOnlineCount()
    {
	
	return ListFilterer.getCount(this, new Filter()
	{
	    public boolean matches(Object obj)
	    {
		return ((Contact) obj).getStatus().equals(
			Status.ONLINE.getName());
	    }
	});
    }

    /**
     * returns the number of people offline
     */
    public int getOfflineCount()
    {
	return ListFilterer.getCount(this, new Filter()
	{
	    public boolean matches(Object obj)
	    {
		return ((Contact) obj).getStatus().equals(
			Status.OFFLINE.getName());
	    }
	});
    }

    /**
     * @return
     */
    public int getListNumber()
    {
	return listNumber;
    }

    /**
     * @param
     */
    public void setListNumber(int listNumber)
    {
	this.listNumber = listNumber;
    }
}
