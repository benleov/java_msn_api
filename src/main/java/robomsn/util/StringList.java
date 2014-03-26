package robomsn.util;


import java.util.StringTokenizer;
import java.util.Vector;

public class StringList extends Vector<String>
{

	public StringList()
	{
		super();
	}
	
    public StringList(String contents)
    {
        parseFromString(contents);
    }
    
    /** adds string into this vector */
    private void parseFromString(String s)
    {
        // sets in the tab all words that are separated by " "

    	StringTokenizer st = new StringTokenizer(s, " ");
    	
    	while ( st.hasMoreElements() )
    	{
    		this.add(st.nextToken());
    	}
    }

    /** @return true if @parm s exists within any of the strings contained
     * in this vector */
    public boolean stringExists(String s)
    {
    	for ( String curr : this )
    	{
    		if ( curr.indexOf(s)  != -1 )
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }

}
