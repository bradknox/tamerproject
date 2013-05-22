/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rlVizLib.messaging.interfaces;

import java.util.Vector;


public interface ProvidesEpisodeSummariesInterface {
    /**
     * This method used to return a vector of strings, where each string was the 
     * log from a single step. In order to be flexible in formatting, we've
     * abandoned that for a string formatted however the environment pleases.
     * <p>
     * The new method also allow the string to be split up and sent in parts
     * which is convenient.
     * @return
     * @deprecated
     */
    public Vector<String> getEpisodeSummary();

    /**
     * Request for the String from  theStartCharacter to theStartCharacter+theChunkSize
     * @param theStartCharacter
     * @param theChunkSize
     * @return
     */public String getEpisodeSummary(long theStartCharacter, int theChunkSize);

}
