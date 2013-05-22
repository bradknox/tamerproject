/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rlcommunity.rlviz.dynamicloading;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Given a grabber that returns Jars, this will find all of the classes in it
 * that match certain criteria.
 * @author mradkie
 */
public class ClassExtractor {

    private static boolean debugClassLoading = false;
    
    private AbstractResourceGrabber theJarGrabber;
   
    public String theMainUrl;
    public Vector<File> theJars;
    public Vector<URI> theJarURIs;
    
    public ClassExtractor(AbstractResourceGrabber theJarGrabber){
        this.theJarGrabber = theJarGrabber;
        
        theJars = new Vector<File>();
        theJarURIs = new Vector<URI>();
        
        refreshJars();
    }

    public void refreshJars(){
        theJarGrabber.refreshURIList();

        theJarURIs.clear();
        theJarURIs.addAll(theJarGrabber.getAllResourceURIs());
    }
    
    /**
     * getAllClassesThatImplement stands for get All Classes That Implement.
     * 
     * This method checks all Jars in theJardDir for classes that implement
     * toMatch. 
     * 
     * @param theJarDir
     * @param toMatch
     * @return a vector of classes that implement toMatch found in
     *         theJarDir
     * 
     */
    public Vector<ClassSourcePair> getAllClassesThatImplement(Vector<Class<?>> toMatch,Vector<Class<?>> toExclude) {
        
        
        Vector<ClassSourcePair> allClasses = new Vector<ClassSourcePair>();
        Vector<ClassSourcePair> matchingClasses = new Vector<ClassSourcePair>();
        
        for (URI thisURI : theJarURIs) {
            allClasses.addAll(getAllClassesFromJar(thisURI));
        }
        for (ClassSourcePair thisClassPair : allClasses) {
            if (checkIfDescendantOf(thisClassPair.getTheClass(), toMatch,toExclude)) {
                matchingClasses.add(thisClassPair);
            }
        }
        return matchingClasses;
    }

    public Vector<ClassSourcePair> getAllClassesThatImplement(Class<?> toMatch, Class<?> toFilter){
        Vector<Class<?>> theFilterVector=new Vector<Class<?>>();
        Vector<Class<?>> theMatchVector=new Vector<Class<?>>();
        theFilterVector.add(toFilter);
        theMatchVector.add(toMatch);
        return getAllClassesThatImplement(theMatchVector,theFilterVector);
    }

    public Vector<ClassSourcePair> getAllClassesThatImplement(Class<?> toMatch){
        Vector<Class<?>> theMatchVector=new Vector<Class<?>>();
        theMatchVector.add(toMatch);
        return getAllClassesThatImplement(theMatchVector,new Vector<Class<?>>());
    }


    /**
     * getAllClassesFromJar stands for get ALL Classes From Jar
     * 
     * This Method returns a Vector of classes that are contained within
     * a Jar
     * @param theJar - the jar to get classes from
     * @return
     */
    public static Vector<ClassSourcePair> getAllClassesFromJar(URI theURI) {
        Vector<ClassSourcePair> theClasses = new Vector<ClassSourcePair>();
        
        try {
            JarFile theJar = new JarFile(new File(theURI));
            Enumeration<JarEntry> allJarEntries = theJar.entries();

            while (allJarEntries.hasMoreElements()) {
                JarEntry thisEntry = allJarEntries.nextElement();
                Class<?> theClass = getClassFromJarEntry(thisEntry, theURI);
                if (theClass != null) {
                    theClasses.add(new ClassSourcePair(theClass, theURI));
                }
            }
        } catch (IOException ex) {
            System.out.println("IO Exception in getAllClassesFromJar: "+ex+" \n on file: "+theURI);
        }
        
        return theClasses;
    }

    public Set<String> getAncestorNames(Class<?> theClass) {
        Set<String> ancestorSet = new TreeSet<String>();
        getAllAncestors(ancestorSet, theClass);
        return ancestorSet;
    }

    /**
     * This Method calls getInterfaceNames to get a list of all interfaces 
     * implemented by thisEntry. If this set of interfaceNames contains
     * toMatch.getname() (the name of the interface to see of thisEntry 
     * implements) true is returned, otherwise false.
     * 
     * @param thisEntry - the class file we wish to check
     * @param theURL - the jar file the class file is located in
     * @param toMatch - the interface we want to check if thisEntry implements+
     * @return
     */
    public boolean checkIfDescendantOf(Class<?> sourceClass, Vector<Class<?>> toMatch,Vector<Class<?>> toExclude) {
        Set<String> ancestorSet = getAncestorNames(sourceClass);
        
        Set<String> matchNames=new TreeSet<String>();
        for (Class<?> thisMatch : toMatch) {
            matchNames.add(thisMatch.getName());
        }

        boolean matchesAnyExcludes=false;
        for (Class<?> thisExclude : toExclude) {
            String thisExcludeName=thisExclude.getName();
            matchesAnyExcludes|=ancestorSet.contains(thisExcludeName);
        }
        
        boolean matchesAll=ancestorSet.containsAll(matchNames);
        
        return matchesAll&&!matchesAnyExcludes;

    }

    /**
     * This method gets a list of all interfaces that the class implements
     * 
     * @param theSet - the list of all interfaces sourceClass implements
     * @param sourceClass
     */
    public void getAllAncestors(Set<String> theSet, Class sourceClass) {
        if (sourceClass == null) {
            return;
        }

        Class[] theInterfaces = sourceClass.getInterfaces();
        for (Class thisInterface : theInterfaces) {
            theSet.add(thisInterface.getName());
            getAllAncestors(theSet, thisInterface.getSuperclass());
        }
        Class superClass=sourceClass.getSuperclass();
        if(superClass!=null){
            theSet.add(superClass.getName());
            getAllAncestors(theSet, superClass);
        }
    }

    /**
     * This method loads a class (thisEntry) from a jar file (theURL)
     * 
     * @param thisEntry - the class file to load
     * @param theURL - the jar file to load it from
     * @return
     */
    public static Class<?> getClassFromJarEntry(JarEntry thisEntry, URI thisFileURI) {
        if (thisEntry.getName().endsWith(".class")) {
            //Cut off the .class
            String thisClassName = thisEntry.getName().substring(0, thisEntry.getName().length() - 6);
            thisClassName = thisClassName.replace("/", ".");
            thisClassName = thisClassName.replace("\\", ".");
            //Load the class file first and make sure it works
            Class<?> theClass = rlVizLib.general.JarClassLoader.loadClassFromFileQuiet(thisFileURI, thisClassName, debugClassLoading);
            return theClass;
        }
        return null;
    }
}
