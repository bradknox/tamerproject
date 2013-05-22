/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rlcommunity.rlviz.dynamicloading;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.Vector;

/**
 * A LocalDirectoryGrabber is intended to find all of the jars in a directory
 * and then find all of the eligible classes inside those jars.
 * @author mradkie
 */
public class LocalDirectoryGrabber extends AbstractResourceGrabber {
    private boolean DEBUG=false;
    
    String theDirectoryString = ".";

    Vector<FileFilter> theFilters=new Vector<FileFilter>();
    
    public void addFilter(FileFilter newFilter){
        theFilters.add(newFilter);
    }
            
    public LocalDirectoryGrabber(String theDirectoryString) {
        super();
        this.theDirectoryString = theDirectoryString;
    }

    public LocalDirectoryGrabber(URI uri) {
        this(uri.getPath());
    }

    /**
     * This method returns a vector of Files from theJarDir directory
     * @param theJarDir
     * @return
     */
    public void refreshURIList() {
        validResourceURIs.clear();

        if(DEBUG)System.out.println("Looking for things in : "+theDirectoryString);
        //create a list of all files in theJarDir
        File directoryObject = new File(theDirectoryString);
        assert (directoryObject.isDirectory());
        File[] theFileList = directoryObject.listFiles();

        if (theFileList == null) {
            return;
        }

        for (File thisFile : theFileList) {
            boolean matchAll=true;
            for (FileFilter thisFilter : theFilters)matchAll&=thisFilter.accept(thisFile);
            
            if(matchAll) validResourceURIs.add(thisFile.toURI());
        }
    }
}
