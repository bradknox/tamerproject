/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rlcommunity.rlviz.dynamicloading;

import java.net.URI;
import java.util.Vector;

/**
 * This is an abstract class for all sorts of objects that seek out resources.
 * 
 * For example, JarGrabbers that take a directory or web URI and find all of the
 * Jars that are there.  Or find Dylibs.  In principle, they could be used to 
 * find whatever.
 * @author Brian Tanner
 */
public abstract class AbstractResourceGrabber {
    protected Vector<URI> validResourceURIs=new  Vector<URI>();
     /**
     * This method returns a vector of Files from theJarDir directory
     * @param theJarDir
     * @return
     */
    public abstract void refreshURIList();
    
    protected final void addResourceURI(URI newURI){
        validResourceURIs.add(newURI);
    }
    public Vector<URI> getAllResourceURIs(){
        return this.validResourceURIs;
    }
    
}
