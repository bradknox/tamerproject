/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rlcommunity.rlviz.dynamicloading;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
 * This class will filter through shared libraries and return all of the ones
 * that pass the filename and contents tests.
 * @author Brian Tanner
 */
public class SharedLibraryGrabber extends LocalDirectoryGrabber{
    List<SharedLibraryContentFilter> contentFilters=new ArrayList<SharedLibraryContentFilter>();

    public SharedLibraryGrabber(String theDirectoryString) {
        super(theDirectoryString);
        super.addFilter(new SharedLibraryFileFilter());
    }

    public SharedLibraryGrabber(URI uri) {
        this(uri.getPath());
    }

    public void addContentsFilter(SharedLibraryContentFilter theContentsFilter){
        contentFilters.add(theContentsFilter);
    }

    @Override
    public void refreshURIList() {
        //First get all shared libraries
        super.refreshURIList();


        List<URI> invalidLibraries=new ArrayList<URI>();
        //Filter out invalid shared libraries
            for (URI thisFileURI : validResourceURIs) {
                boolean passesContentFilters=true;
                for (SharedLibraryContentFilter thisContentFilter : contentFilters) {
                    passesContentFilters &= thisContentFilter.accept(thisFileURI);
                }
                if (!passesContentFilters) {
                    invalidLibraries.add(thisFileURI);
                }
            }
            super.validResourceURIs.removeAll(invalidLibraries);
        }

}
