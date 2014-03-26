package settings;

import java.util.List;

import robomsn.data.Contact.Status;

public class Settings 
{

	private static final int SERVER_PORT_DEFAULT = 1863;
	private String notificationServerURL;
	private int notificationServePort;
	private String username, password, message;
	
	private List<String> broadcastList;	

	private Status initStatus;
	private int serverPort;
	
	
	public Settings()
	{
	    serverPort = SERVER_PORT_DEFAULT;
	}

	public void setServerPort( int serverPort )
	{
	    this.serverPort = serverPort;
	}
	
	public int getServerPort()
	{
	    return serverPort;
	}
	
	public void setUsername(String username)
	{
	    this.username = username;
	}

	public void setPassword(String password)
	{
	    this.password = password;
	}

	public void setBroadcastList(List<String> list)
	{
	    this.broadcastList = list;
	}

	public void setInitStatus(Status status)
	{
	    initStatus = status;
	}

	public void setMessage(String message)
	{
	    this.message = message;
	}

	public String getNotificationServerURL()
	{
	    return notificationServerURL;
	}

	public int getNotificationServePort()
	{
	    return notificationServePort;
	}

	public String getUsername()
	{
	    return username;
	}

	public String getPassword()
	{
	    return password;
	}

	public String getMessage()
	{
	    return message;
	}

	public List<String> getBroadcastList()
	{
	    return broadcastList;
	}

	public Status getInitStatus()
	{
	    return initStatus;
	}
	
}
