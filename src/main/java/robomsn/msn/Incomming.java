package robomsn.msn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;

import org.apache.log4j.Logger;

import robomsn.data.Chat;
import robomsn.data.Contact;
import robomsn.data.Group;
import robomsn.data.Contact.Status;
import robomsn.util.StringList;
import robomsn.util.StringUtil;

public class Incomming extends Thread
{
	
	private static Logger log = Logger.getLogger(Incomming.class);
	private BufferedReader br; // the input bufferedreader
	private InputStreamReader reader;
	private Socket connection; // the socket with the notification server
	private RoboMSN msn;
	private int contactnumber; // to remember how many contacts have been found
	private HashSet<IncommingListener> listeners;

	public Incomming(Socket connection, RoboMSN msn)
	{
		this.connection = connection;
		this.msn = msn;

		this.setDaemon(true); // stop when main thread stopped

		listeners = new HashSet<IncommingListener>();

		try
		{
			reader = new InputStreamReader(this.connection.getInputStream(),
					"UTF-8");

			br = new BufferedReader(reader);

		} catch (IOException e)
		{
			log.error(e);
			msn.disconnect();
		}
	}

	public void startAutomaticListening()
	{
		this.start();
	}

	public void run()
	{
		String inputline;

		while (msn.isConnected())
		{
			inputline = this.getNextLine();

			if (inputline != null)
			{
				this.parseLine(inputline);
			} else
			{
				break;
			}
		}
	}

	public String getNextLine()
	{
		String in;
		try
		{
			in = br.readLine();
			log.debug("<<< " + in);

			if (in == null)
			{
				msn.disconnect();
				log.debug("NotificationServerIncoming : null line received... :-/");
			}

			return in;
		} catch (IOException e)
		{
			log.error(e);
			return null;
		}
	}

	/** parses incoming line from the server */
	public void parseLine(String l)
	{
		if (l != null)
		{
			StringList t = new StringList(l);

			if (l.startsWith("SYN"))
			{
				// number of contacts

				this.contactNumber(t);
			} else if (l.startsWith("LSG"))
			{
				// a contact group
				this.newContactGroup(t);
			} else if (l.startsWith("LST"))
			{
				this.newContactInfo(t); // a contact info
			} else if (l.startsWith("CHL"))
			{
				// this is a challenge ! hurry up to answer ! :)
				this.newChallenge(t);
			} else if (l.startsWith("ILN"))
			{
				// contact online status
				this.contactStatus(t);
			} else if (l.startsWith("FLN"))
			{
				// a contact goes offline
				this.contactOffline(t);
			} else if (l.startsWith("NLN"))
			{
				// user coming online
				this.contactOnline(t);
				
			} else if ( l.startsWith("<NOTIFICATION")) 
			{
				// server notification 
				// for some reason we can get other commands on the
				// end of this
				int nxtLineIdx = l.indexOf("</NOTIFICATION>");
				
				if ( nxtLineIdx != -1 && nxtLineIdx < l.length() -1 ) { 
					String nxtLine = l.substring(nxtLineIdx + 15);
					parseLine(nxtLine);
				}
				
			} else if (l.startsWith("QNG"))
			{
				// status updates received

				for (IncommingListener curr : listeners)
				{
					curr.presenceUpdated();
				}
			} else if (l.startsWith("XFR"))
			{
				// received when a chat is already created !
				// new switchboard session
				// <<< XFR 10 SB 207.46.108.37:1863 CKI
				// 17262740.1050826919.32308\r\n
				this.newSwitchBoardSession(t);
			} else if (l.startsWith("RNG"))
			{
				// invited to switchboard session
				// <<< RNG 11752013 207.46.108.38:1863 CKI 849102291.520491113
				// example@passport.com Example%20Name\r\n
				this.newSwitchBoardSession(t);
			} else if (l.startsWith("Inbox-Unread"))
			{
				this.emailsUnread(t); // <<< Inbox-Unread: 102
			}

			if (startsWithaNumber(t))
			{
				// Out.write("NSI - error!");
				this.checkError(t);
			}
		} else
		{
			// nothing
		}
	}

	private void checkError(StringList t)
	{
		int error = Integer.parseInt(t.get(0));

		if (error == 928)
		{
			log.debug("\r\nError : is your password correct ??\r\n");
		}
	}

	/**
	 * @param t
	 * @return
	 */
	private boolean startsWithaNumber(StringList t)
	{
		return false;
	}

	/**
	 * @param t
	 */
	private void emailsUnread(StringList t)
	{
		// <<< Inbox-Unread: 102
		try
		{
			msn.setUnreadMails(Integer.parseInt(t.get(1).trim()));
		} catch (NumberFormatException e)
		{
			msn.setUnreadMails(0);
			log.error(e);
		}
	}

	private void contactNumber(StringList t)
	{
		try
		{
			contactnumber = Integer.parseInt(t.get(3));

			msn.getContactlist().setListNumber(Integer.parseInt(t.get(3)));
		} catch (NumberFormatException e)
		{
			log.error(e);
		}
	}

	private void newContactGroup(StringList t)
	{
		String grn = t.get(1);
		int grni = 0;
		try
		{
			grni = Integer.parseInt(grn);
		} catch (NumberFormatException e)
		{
			log.error(e);
		}

		String gr = t.get(2);

		Group g = new Group(gr, grni);

		msn.getGrouplist().add(g);
	}

	private void newContactInfo(StringList t)
	{

		contactnumber--;

		String realname = t.get(1);
		String displayname = t.get(2);

		String list = t.get(3).trim();

		if (list.equals("3") || list.equals("11"))
		{
			// allowed people

			String groupnumber = t.get(4);
			int groupnumberi = 0;

			groupnumberi = Integer.parseInt(groupnumber);

			msn.getContactlist().add(
					new Contact(realname, displayname, Status.OFFLINE,
							groupnumberi));
			
		} else if (list.equals("4"))
		{
			// blocked contacts ... list 99
			msn.getContactlist().add(
					new Contact(realname, displayname, Status.OFFLINE, 99));
		}
		
		// notify listeners that this is the last contact
		
		if (contactnumber == 0)
		{
			for (IncommingListener curr : listeners)
			{
				curr.listReceived();
			}
		}
	}

	private void newChallenge(StringList t)
	{
		String s = "QRY " + t.get(1) + " msmsgs@msnmsgr.com 32\r\n";
		String md5 = StringUtil.MD5sum(t.get(2) + "Q1P7W2E4J9R8U3S5");
		s = s + md5;

		msn.sendChallengeAnswer(s);
	}

	private void contactStatus(StringList t)
	{

		// <<< ILN 0 BSY funkie444@hotmail.com
		// (#)(#)(#)(#)...8-|-->%2014.7%20-->%208-|%20...

		String s = t.get(2);
		Status stat = Status.getStatusFromProtocol(s);

		// if status = "", then there is a problem !
		if (stat == null)
		{
			log.debug("\r\nIncoming - contactstatus - problem !!! t = "
					+ t.get(0) + " " + t.get(1) + " " + t.get(2) + "\r\n");
		}

		msn.getContactlist().setContactStatus(t.get(3), t.get(4), stat);

	}

	/** <<< FLN bob@passport.com\r\n */
	private void contactOffline(StringList t)
	{
		msn.getContactlist().setContactStatus(t.get(1),
				msn.getContactlist().getContact(t.get(1)).getDisplayname(),
				Status.OFFLINE);
	}

	/** <<< NLN NLN ilovesubs@hotmail.com Olivier 268435500 */
	public void contactOnline(StringList t)
	{
		StringList t2 = new StringList();
		t2.add("NLN");
		t2.add("0");
		t2.add(t.get(1));
		t2.add(t.get(2));
		t2.add(t.get(3));
		t2.add(t.get(4));
		contactStatus(t2);
	}

	private void newSwitchBoardSession(StringList t)
	{
		String address;
		String port;
		String sessionID = ""; // for RNG
		String IDString; // for both
		String username = ""; // for XFR

		boolean transfer = t.get(0).equals("XFR");

		Chat c;
		if (transfer)
		{
			address = t.get(3).substring(0, t.get(3).indexOf(":"));
			port = t.get(3).substring(t.get(3).indexOf(":") + 1,
					t.get(3).length());

			IDString = t.get(5);

			// get the chat that doesn't have a server
			c = msn.getChatlist().getChatWithNoServer();
			c.setChatId(IDString);
			c.setSwitchBoardServer(address, port);
		} else
		{
			address = t.get(2).substring(0, t.get(2).indexOf(":"));
			port = t.get(2).substring(t.get(2).indexOf(":") + 1,
					t.get(2).length());

			sessionID = t.get(1);
			IDString = t.get(4);
			username = t.get(5);
			// if chat exists, then it has to be reconnected
			// if it doesn't it has to be created and connected!
			c = new Chat();

			// Out.write("NSI - newSBS - un chat exist = " +
			// msn.getChatlist().exists(username));
			if (msn.getChatlist().exists(username))
			{
				// reconnect
				c = msn.getChatlist().getChat(username);
				c.reconnect(address, port, sessionID, IDString);
			} else
			{
				// chat does not exist, i need to connect !
				// msn.status may be on something !

				// but first, i have to see if the guy may see my status!
				// (if he is blocked!)

				Contact contact = msn.getContactlist().getContact(username);

				if (!contact.isBlocked())
				{
					// i take the output window
					if (msn.getStatus() == RoboMSN.Status.AVAILABLE)
					{
						msn.setStatus(RoboMSN.Status.CHATTING);
						RoboMSN.Status.CHATTING.setChatUser(username);
					}

					c.newChat(msn, msn.getContactlist().getContact(username));
					c.connectExistingChat(address, port, sessionID, IDString);
				}
			}
		}
	}

	public void addListener(IncommingListener listener)
	{
		listeners.add(listener);
	}

}
