/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robomsn.data;

/**
 * @author karrotcake
 */
public interface ChatListener
{
    /** connection on server made */
    public void chatConnected ( Chat chat, boolean connected );

    /** contact has joined chat */
    public void chatJoined(Chat chat, boolean connected);

    /** chat has timed out */
    public void chatTimeout(Chat chat);
         
}
