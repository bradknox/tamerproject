/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rlcommunity.rlviz.dynamicloading;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mradkie
 */
public class SpecificFileGrabber extends AbstractResourceGrabber {

    private URI theURI=null;
    
    public SpecificFileGrabber(String thePath){
        try {
            theURI = new URI(thePath);
        } catch (URISyntaxException ex) {
            Logger.getLogger(SpecificFileGrabber.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void refreshURIList() {
        addResourceURI(theURI);
    }

}
