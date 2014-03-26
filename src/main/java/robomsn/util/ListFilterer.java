package robomsn.util;

import java.util.Iterator;
import java.util.List;

public abstract class ListFilterer
{

    public static void filter(List list, Filter filter)
    {

    }

    public static int getCount(List list, Filter criterion)
    {
	int count = 0;

	for (Object curr : list)
	{
	    if (criterion.matches(curr))
	    {
		count++;
	    }
	}

	return count;
    }

    public static void removeAll(List list, Filter criterion)
    {
	Iterator<Object> i = list.iterator();

	while (i.hasNext())
	{
	    Object curr = i.next();
	    if (criterion.matches(curr))
	    {
		i.remove();
	    }
	}

    }
}
