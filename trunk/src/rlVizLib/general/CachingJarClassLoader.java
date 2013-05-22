/*
Copyright 2007 Brian Tanner
brian@tannerpages.com
http://brian.tannerpages.com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/


package rlVizLib.general;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
@author btanner
 */
public class CachingJarClassLoader {
static final Map<URI, ClassLoader> theClassLoaders;
static final Map<URI,Map<String,Class>> theClasses;
       
static{
    theClassLoaders=new HashMap<URI,ClassLoader>();
    theClasses=new HashMap<URI,Map<String,Class>>();
}
    /**
     * All loading goes through here.  Good to know.
    @param theFile A File object that is the Jar we want to load from
    @param className The fully qualified name of the class to load
    @return If this method returns anything it is the class requested.
    @throws CouldNotLoadJarException
     */
    public static Class<?> loadClassFromFile(URI theFileURI, String className) throws CouldNotLoadJarException {
        Class<?> theClass = null;
        ClassLoader urlLoader = null;
         Map<String,Class> thisClassList=null;
         
        //First, lets see if we have the class cached.
        if(theClasses.containsKey(theFileURI)){
            //We've seen this jar file before
            thisClassList=theClasses.get(theFileURI);
            if(thisClassList.containsKey(className)){
                //We've seen this exact class!
                theClass=thisClassList.get(className);
                return theClass;
            }
            //Otherwise we'll need to load it with a class loader.
        }


        //See if we have an appropriate URL loader cached.
        if(theClassLoaders.containsKey(theFileURI)){
            urlLoader=theClassLoaders.get(theFileURI);
        }else{
            try {
                urlLoader = new URLClassLoader(new URL[]{theFileURI.toURL()});
                //Cache the class loader for next time...
                theClassLoaders.put(theFileURI,urlLoader);
            } catch (MalformedURLException ex) {
            String errorString = "SERIOUS ERROR: When JarClassLoader tried to load class: " + className + " from file: " + theFileURI.toString();
            throw new CouldNotLoadJarException(errorString, ex);
            }
        }
        try {
            //So now we have a class loader or we threw an exeception
            theClass = urlLoader.loadClass(className);
                 //Cache it for next time
                 if(thisClassList==null){
                     thisClassList=new HashMap<String,Class>();
                     theClasses.put(theFileURI, thisClassList);
                 }
                 thisClassList.put(className, theClass);
                 return theClass;
        } catch (ClassNotFoundException ex) {
            String errorString = "SERIOUS ERROR: When JarClassLoader tried to load class: " + className + " from file: " + theFileURI.toString();
            throw new CouldNotLoadJarException(errorString, ex);
        }
    }


    public static Class<?> loadClassFromFileQuiet(URI jarFileURI, String thisClassName, boolean dumpTheStack) {
        Class<?> theClass = null;
        try {
            theClass = loadClassFromFile(jarFileURI, thisClassName);
        } catch (CouldNotLoadJarException e) {
            System.out.println(e.getErrorString());
            System.out.println(e.getOriginalProblem());
            if (dumpTheStack) {
                Thread.dumpStack();
            }
            return null;
        }
        return theClass;
    }
}
