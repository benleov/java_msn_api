package robomsn.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

public class StringUtil
{
	private static Logger log = Logger.getLogger(StringUtil.class);
	
	private static final String NEW_LINE = "\n";
	private static final String RETURN = "\r";
	
    public static String setblanks(String s)
    {
    	if ( s != null )
    	{
    		return s.replaceAll("%20", " ");
    	}
    	else
    	{
    		return null;
    	}
    }

    /** removes new line and carriage returns characters */
    public static String removeNewLine(String s)
    {
    	s = s.replaceAll(RETURN, " ");
    	s = s.replaceAll(NEW_LINE, " ");
    	return s;
    }
    /**
     * Calculates the MD5 checksum of the string, and returns a
     * string of its hexadecimal representation.
     *
     * @param toHash The string that is to be checksummed.
     * @return String representation of the hexadecimal MD5 checksum of the string.
     */
    public static String MD5sum(String toHash)
    {

        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");

            return byteArrayToHexString(md.digest(toHash.getBytes()));
        }
        catch (NoSuchAlgorithmException e)
        {
            log.error(e);
        }

        return "";
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param bytes The byte array to convert.
     * @return String representation of the hexadecimal representation of the byte array.
     */
    public static String byteArrayToHexString(byte[] bytes)
    {

        String hexString = "";

        for (int i = 0; i < bytes.length; i++)
        {
            byte b = bytes[i];
            hexString = hexString + byteToHexString(b & 0xf, (b >> 4) & 0xf);
        }

        return hexString;
    }

    /** 
     * Converts a single byte into a hexadecimal string.
     *
     * @param nib1 The first nibble of the byte.
     * @param nib2 The second nibble of the byte.
     * @return String representation of the hexadecimal representation of a byte.
     */
    public static String byteToHexString(int nib1, int nib2)
    {

        char char1, char2;
        char[] chars = new char[2];

        char1 = nibbleToChar(nib1);
        char2 = nibbleToChar(nib2);
        chars[0] = char2;
        chars[1] = char1;

        return (new String(chars));
    }

    /**
     * Converts a nibble into a character.
     *
     * @param nibble The nibble.
     * @return A character representation of the hexadecimal nibble.
     */
    public static char nibbleToChar(int nibble)
    {

        if (nibble < 10)
        {
            return (Integer.toString(nibble)).charAt(0);
        }
        else
        {
            int nib = nibble - 10;

            return (char) (((char) nib) + 'a');
        }
    }
    
}
