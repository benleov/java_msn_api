package robomsn.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeUtil
{

    /** TODOL use a proper date formatter to get this*/
    public static String getTime()
    {
        Calendar c = new GregorianCalendar();

        String hour = "" + c.get(Calendar.HOUR_OF_DAY);
        
        if (hour.length() == 1)
        {
            hour = "0" + hour;
        }

        String minute  = "" + c.get(Calendar.MINUTE);
        
        if (minute.length() == 1)
        {
            minute = "0" + minute;
        }
        
        String second = "" + c.get(Calendar.SECOND);
        
        if (second.length() == 1)
        {
            second = "0" + second;
        }
        
        return hour + ":" + minute + ":" + second;
    }
}
