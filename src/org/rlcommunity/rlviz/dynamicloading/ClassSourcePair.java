/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rlcommunity.rlviz.dynamicloading;

import java.net.URI;

/**
 *
 * @author btanner
 */
public class ClassSourcePair {
    private final Class<?> theClass;
    private final URI sourceURI;

    public ClassSourcePair(Class<?> theClass, URI theSource){
        this.theClass=theClass;
        this.sourceURI=theSource;
    }

    public Class<?> getTheClass(){
        return theClass;
    }

    public URI getURI(){
        return sourceURI;
    }

    public String getFullSignature(){
        return sourceURI.toString()+":"+theClass.getName();
    }

}
