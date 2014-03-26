package settings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import robomsn.Main;
import robomsn.data.Contact;

public class SettingsParser
{
    private static org.apache.log4j.Logger _log = Logger.getLogger(Main.class);

    private static final String PROPERTIES_FILE = "robomsn.conf";
    private static final String PROP_USERNAME = "login.username";
    private static final String PROP_PASSWORD = "login.password";
    private static final String PROP_BROADCAST = "broadcast.group";
    private static final String PROP_STATE = "login.state";
    private static final String DEFAULT_STATE = "away";
    private static final String DEFAULT_USERNAME = "username";
    private static final String DEFAULT_PASSWORD = "password";
    private static ArrayList<String> DEFAULT_BROADCAST = new ArrayList<String>();

    static
    {
	DEFAULT_BROADCAST.add("user@domain.com");
	DEFAULT_BROADCAST.add("user2@domain.com");
    }

    /**
     * parses settings. if
     * 
     * @throws SettingsInvalidException
     * @throws SettingsNotFoundException
     * @throws InvalidSettingArgumentException
     */
    public Settings parse(String[] args) throws SettingsInvalidException,
	    SettingsNotFoundException, InvalidSettingArgumentException
    {
	if (args != null && args.length > 1)
	{
	   
	    String message = "";
	    String state, username;
	    String password;
	    List<String> broadcastList;

	    try
	    {

		for (int x = 1; x < args.length; x++)
		{
		    message += args[x] + " ";
		}

		PropertiesConfiguration config = new PropertiesConfiguration(
			PROPERTIES_FILE);

		state = config.getString(PROP_STATE, DEFAULT_STATE);
		
		
		username = config.getString(PROP_USERNAME, DEFAULT_USERNAME);
		password = config.getString(PROP_PASSWORD, DEFAULT_PASSWORD);
		broadcastList = config.getList(PROP_BROADCAST,
			DEFAULT_BROADCAST);
		
		if ( broadcastList == null || broadcastList.get(0).trim().equals(""))
		{
			broadcastList = new LinkedList<String>();
		}

		if (state != null && !username.equals(DEFAULT_USERNAME)
			&& !password.equals(DEFAULT_PASSWORD)
			&& !broadcastList.equals(DEFAULT_BROADCAST))
		{
		    Settings set = new Settings();
		    set.setInitStatus ( Contact.Status.getStatusFromName(state) );
		    set.setUsername( username );
		    set.setPassword ( password );
		    set.setBroadcastList ( broadcastList );
		    set.setMessage ( message );
		    
		    return set;
		}
		else
		{
		    throw new SettingsInvalidException();
		}
	    }
	    catch (ConfigurationException ex)
	    {
		throw new SettingsNotFoundException();

	    }
	}
	else
	{
	    throw new InvalidSettingArgumentException();
	}

    }
    public void createBlankConfigFile()
    {
        try
        {
            PropertiesConfiguration config = new PropertiesConfiguration();
            config.addProperty(PROP_USERNAME, DEFAULT_USERNAME);
            config.addProperty(PROP_PASSWORD, DEFAULT_PASSWORD);
            config.addProperty(PROP_BROADCAST, DEFAULT_BROADCAST);
            config.addProperty(PROP_STATE, DEFAULT_STATE);
            config.save(PROPERTIES_FILE);
        }
        catch (ConfigurationException ex)
        {
            _log.error(ex);
        }
    }
}
