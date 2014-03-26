package robomsn.msn;

import robomsn.data.Contact;
import robomsn.data.Group;
import robomsn.data.ChatList;
import robomsn.data.Chat;
import robomsn.data.ContactList;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 */
public class RoboMSN
{
	
	private static Logger log = Logger.getLogger(RoboMSN.class);

	public enum Status
	{
		AVAILABLE, CHATTING;

		private String chatUser;

		public String getChatUser()
		{
			return chatUser;
		}

		public void setChatUser(String chatUser)
		{
			this.chatUser = chatUser;
		}

	}

	private Status _status; // our status
	private Contact.Status _clientStatus; // our clients MSN status (i.e
	// Online, busy, idle)

	private String _username; // login credentials
	private String _password;

	private ContactList _contactList; // the user contactlist
	private ArrayList<Group> _groupList;
	private ChatList _chatList; // the list of current chats
	private Incomming _incoming; // the incoming channel from msn server
	private Outgoing _outgoing; // the outgoing channel to msn server
	
	private static final String NOTIFICATION_SERVER_DEF = "messenger.hotmail.com";
	
	private String _notificationserverurl; // primary
	// server
	// url
	private int _notificationserverport = 1863; // primary server port
	private String dispatchserverurl = "gateway.messenger.hotmail.com"; // secondary
	// server
	// url
	private int _dispatchserverport = 80; // secondary server port
	private String _msnversion = "MSNP8"; // using msnP8
	
	private static int trID = 20; // trID is an identification integer
	
	private Socket _serversocket; // socket to the msn server
	private boolean _connected;
	private int _unreademails; // number of unread emails
	private String stmp; // a temporary string and yet stored as a class

	// variable!

	/**
	 * Connects to the notification server. if that server doesn't respond, a
	 * connect the dispatch server is attempted
	 * 
	 * @param url :
	 *            the notification server url
	 * @param port :
	 *            the notification server port
	 */
	public RoboMSN(Contact.Status clientStatus, int port, String passport,
			String password)
	{
		_notificationserverurl = NOTIFICATION_SERVER_DEF;

		_notificationserverport = port;
		_username = passport;
		_password = password;

		_clientStatus = clientStatus;
		_status = Status.AVAILABLE;

		_serversocket = null;
		_incoming = null;
		_outgoing = null;
		_connected = false;

		_contactList = new ContactList();
		_groupList = new ArrayList<Group>();
		_chatList = new ChatList();
	}

	public void setUnreadMails(int unreadMails)
	{
		this._unreademails = unreadMails;
	}

	public int getUnreadMailCount()
	{
		return _unreademails;
	}

	private boolean notificationConnect()
	{
		try
		{
			_serversocket = new Socket(_notificationserverurl,
					_notificationserverport);
			_connected = true;
		} catch (UnknownHostException e)
		{
			log.error(e);
			try
			{
				_serversocket = new Socket(dispatchserverurl, _dispatchserverport);
				_connected = true;
			} catch (UnknownHostException e2)
			{
				log.error(e2);
				_connected = false;
			} catch (IOException e2)
			{
				log.error(e2);
				_connected = false;
			}
		} catch (IOException e)
		{
			log.error(e);
			_connected = false;
		}

		if (!_connected)
		{
			log.debug("connect: cannot connect: aborting");
			return false;
		} else
		{
			startIncomingChannel();
			startOutgoingChannel();

			sendMsnVersion();
			stmp = _incoming.getNextLine();
			sendClientInfo();
			stmp = _incoming.getNextLine();
			sendUserInfo();
			stmp = _incoming.getNextLine();

			log.debug("Redirect line: " + stmp);

			if (stmp.startsWith("XFR")) // redirection
			{
				this.disconnect();
				log.debug("MSN: redirecting...");
				parseRedirection(stmp);
				return false;
			} else
			{
				log.debug("Redirection success!!");
				return true;
			}
		}
	}

	public boolean connect()
	{
		if (!notificationConnect())
		{
			boolean redirected = notificationConnect();
			if (!redirected) // try again
			{
				log.debug("MSN Connection failed");
				return false;
			} else
			{
				log.debug("MSN connected!!");
			}
		}

		SSLServerConnection sslConn = null;
		String strLoginSvr = null;
		String challengedhash = null;
		String strTicket = null;

		if (isConnected())
		{
			sslConn = new SSLServerConnection(
					SSLServerConnection.PASSPORT_LIST_SERVER_ADDRESS);
		}

		if (isConnected())
		{
			strLoginSvr = sslConn.getPassportLoginServer();
		}

		if (isConnected())
		{
			challengedhash = this.getChallengedHash(stmp);
		}

		if (isConnected())
		{
			strTicket = sslConn.requestAuthorizationTicket(strLoginSvr,
					_username, _password, challengedhash);
		}

		if (isConnected())
		{
			sendTicket(strTicket);
			stmp = getIncoming().getNextLine();
		}

		final Object waitForList = new Object();

		// so we are notified of when the list is received
		// when we received list send a ping, which retrives
		// present status from all our contacts
		_incoming.addListener(new IncommingListener()
		{
			@Override
			public void listReceived()
			{
				if (isConnected())
				{
					setPresence(_clientStatus);
					sendPing();
				}
			}

			@Override
			public void presenceUpdated()
			{
				synchronized (waitForList)
				{
					waitForList.notifyAll();
				}
			}
		});

		_incoming.startAutomaticListening();

		synch(); // requests list

		try
		{
			synchronized (waitForList)
			{
				waitForList.wait();
			}
		} catch (InterruptedException e)
		{
			log.error(e);
		}

		return true;
	}

	public void disconnect()
	{
		_connected = false;

		Iterator<Chat> i = _chatList.iterator();

		while (i.hasNext())
		{
			Chat curr = i.next();

			if (curr.isConnected())
			{
				curr.stop();
			}

			i.remove();
		}

		try
		{
			_serversocket.close();
		} catch (IOException e)
		{
			log.error(e);
		}

	}

	public void sendMsnVersion()
	{
		_outgoing.send("VER 0 MSNP8 CVR0\r\n");
	}

	/**
	 * @return
	 */
	public void startIncomingChannel()
	{
		_incoming = new Incomming(_serversocket, this);
	}

	public void startOutgoingChannel()
	{
		_outgoing = new Outgoing(_serversocket);
	}

	public void sendClientInfo()
	{
		_outgoing.send("CVR 2 0x0409 win 4.10 i386 MSNMSGR 5.0.0544 MSMSGS "
				+ _username + "\r\n");
	}

	public void sendUserInfo()
	{
		int tr = getTrID();
		_outgoing.send("USR " + tr + " TWN I " + _username + "\r\n");
	}

	/**
	 * happens when a server is busy, and sends a user to another server ...
	 * syntax is XFR 2 NS <new server ip>:<new server port> 0
	 * 207.46.104.20:1863\r\n
	 */
	private void parseRedirection(String redirectLine)
	{
		String newurl = redirectLine.substring(redirectLine.indexOf("NS") + 3,
				redirectLine.indexOf(":"));

		redirectLine = redirectLine.substring(redirectLine.indexOf(":"),
				redirectLine.length());

		int newport = Integer.parseInt(redirectLine.substring(1, redirectLine
				.indexOf(" ")));

		_notificationserverurl = newurl;
		_notificationserverport = newport;
	}

	/**
	 * returns the next id
	 */
	public static int getTrID()
	{
		trID++;
		return trID % 4294960;
	}

	public void sendTicket(String t)
	{
		_outgoing.send("USR " + getTrID() + " TWN S " + t + "\r\n");
	}

	/**
	 * After you send your initial status, you will receive an ILN command for
	 * every principal who is online, has you on their allow list, and you have
	 * on your forward list. You will also receive one after adding a principal
	 * to your forward list if they are online and had already allowed you to
	 * see their presence
	 */

	public void setPresence(Contact.Status status)
	{

		_clientStatus = status;
		_outgoing.send("CHG " + getTrID() + " " + _clientStatus.getProtocol()
				+ " 268435456\r\n");
	}

	public void sendPing()
	{
		_outgoing.send("PNG\r\n");
	}

	public void synch()
	{
		_outgoing.send("SYN 13 0\r\n");
	}

	public void sendChallengeAnswer(String s)
	{
		_outgoing.send(s);
	}

	public void setContactStatus(String username, String displayname,
			Contact.Status status)
	{
		_contactList.setContactStatus(username, displayname, status);
	}

	/** >>> XFR 15 SB\r\n */
	public void requestSessionBoard(int id, Chat c)
	{
		_outgoing.send("XFR " + id + " SB\r\n");
	}

	private String getChallengedHash(String s)
	{
		// line is like USR 3 TWN S
		// lc=1033,id=507,tw=40,fs=1,ru=http%3A%2F%2Fmessenger%2Emsn%2Ecom,ct=1062764229,kpp=1,kv=5,ver=2.1.0173.1,tpf=43f8a4c8ed940c04e3740be46c4d1619\r\n
		// and i have to take the "lc= .. 9"

		try
		{
			return s.substring(s.indexOf("lc"), s.length() - 2);
		} catch (RuntimeException e)
		{
			log.error(e);
			return "";
		}
	}

	/**
	 * overrides Thread.wait(), so we dont have to catch the exception every
	 * time TODO: we really should do this properly and sync on an object
	 */
	private void wait(int lg)
	{
		try
		{
			Thread.sleep(lg);
		} catch (InterruptedException e)
		{
			log.error(e);
		}
	}

	public boolean isConnected()
	{
		_connected = !this._serversocket.isClosed();
		return _connected;

	}

	public Outgoing getOutgoing()
	{
		return _outgoing;
	}

	public Incomming getIncoming()
	{
		return _incoming;
	}

	public ContactList getContactlist()
	{
		return _contactList;
	}

	public ArrayList<Group> getGrouplist()
	{
		return _groupList;
	}

	public ChatList getChatlist()
	{
		return _chatList;
	}

	public String getUsername()
	{
		return _username;
	}

	public Status getStatus()
	{
		return _status;
	}

	public void setStatus(Status status)
	{
		// Out.write("msn : setStatus : status = " + string);
		this._status = status;
	}

	/** returns our current MSN status */
	public Contact.Status getClientStatus()
	{
		return _clientStatus;
	}
}
