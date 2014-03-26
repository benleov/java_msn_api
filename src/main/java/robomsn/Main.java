/*
 */
package robomsn;

import java.util.HashSet;

import org.apache.log4j.Logger;

import robomsn.data.Chat;
import robomsn.data.ChatListener;
import robomsn.data.Contact;
import robomsn.data.ContactList;
import robomsn.msn.RoboMSN;
import settings.InvalidSettingArgumentException;
import settings.Settings;
import settings.SettingsInvalidException;
import settings.SettingsNotFoundException;
import settings.SettingsParser;

/**
 * 
 */
public final class Main
{
	private static HashSet<Contact> toProcess;
	private static org.apache.log4j.Logger log = Logger.getLogger(Main.class);

	private Object sync = new Object();

	public void start(String[] args)
	{
		SettingsParser parser = new SettingsParser();

		try
		{
			Settings settings = parser.parse(args);

			log.debug("roboMSN is starting");

			final RoboMSN m = new RoboMSN(settings.getInitStatus(), settings
					.getServerPort(), settings.getUsername(), settings
					.getPassword());

			if (m.connect())
			{
				
				ContactList contacts = m.getContactlist();
				
				toProcess = new HashSet<Contact>();
				
				if (settings.getBroadcastList().size() == 0)
				{
					log.info("No contacts specified; sending to all online");
					toProcess.addAll(contacts);	 // no users are specified, then default to all
				}
				else
				{
					for (String contact : settings.getBroadcastList())
					{
						Contact c = contacts.getContact(contact);
						
						if (c == null)
						{
							log.error("Null contact: " + contact);
						}
						else if (c.getStatus() == Contact.Status.OFFLINE)
						{
							log.info("Contact offline: " + c);
						}
						else
						{
							toProcess.add(c);
						}
					}
				}
	 
				for ( Contact curr : toProcess )
				{
						sendMessage(m, curr, settings.getMessage());
				}

				// wait for chats to process

				if (toProcess.size() > 0)
				{
					synchronized (sync)
					{
						try
						{
							sync.wait();
						} catch (InterruptedException e)
						{
							log.error(e);
						}
					}
				}

				m.disconnect();
			} else
			{
				log.warn("roboMSN could not connect");
			}
		} catch (SettingsInvalidException e)
		{
			log
					.info("usage: invalid config file. Please delete to create template");
		} catch (SettingsNotFoundException e)
		{
			log.info("usage: roboMSN config file not found. Template created");
			parser.createBlankConfigFile();
		} catch (InvalidSettingArgumentException e)
		{
			log.info("usage: roboMSN -m message");
		}
	}

	/**
	 * creates a new chat session and sends a message to the contact. NOTE: this
	 * could probabaly be done more effeciently by inviting contacts to a single
	 * chat session
	 */
	private void sendMessage(RoboMSN m, Contact c, final String message)
	{
		// Contact c = m.getContactlist().getContact(contact);
		if (c != null)
		{
			Chat chat = new Chat();

			// if contact doesnt exist on list, attempt to add him

			chat.setListener(new ChatListener()
			{

				public void chatConnected(Chat chat, boolean connected)
				{
					// connection to server made
				}

				public void chatJoined(Chat chat, boolean connected)
				{
					chat.sendMessage(message);
					chat.closeSocket();
					toProcess.remove(chat.getParticipants().get(0));
					notifyChatsDone();

				}

				public void chatTimeout(Chat chat)
				{
					log.debug("chat timeout!");
					chat.closeSocket();
					toProcess.remove(chat.getParticipants().get(0));
					notifyChatsDone();

				}

				private void notifyChatsDone()
				{
					synchronized (sync)
					{
						if (toProcess.size() == 0)
						{
							sync.notifyAll();
						}
					}
				}

			});

			chat.newChat(m, c);
			m.getChatlist().add(chat);
			chat.connectNewChat();
		} else
		{
			log.debug("sendMessage: invalid contact: " + c);
			toProcess.remove(c);
		}
	}

	private Main()
	{
	}

	/**
	 * @param Starts
	 *            roboMSN.
	 * @param args
	 *            will be parsed as the message to send to the broadcast group
	 */
	public static void main(String[] args)
	{
		Main main = new Main();
		String[] testArgs = { "This", " is", " a", " message" };
		main.start(testArgs);
	}

}
