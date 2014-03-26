package robomsn.msn;

public interface IncommingListener
{
    /** called by incomming class when the MSN list has been
     * received */
    void listReceived();
    
    /** called when presents of contacts have been update */
    void presenceUpdated();
}
