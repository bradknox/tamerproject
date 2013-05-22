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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
@author btanner
 */
public class JarClassLoader {
    /**
     * @deprecated
     * @param theFile
     * @param className
     * @return
     * @throws rlVizLib.general.CouldNotLoadJarException
     */
    public static Class<?> loadClassFromFile(File theFile, String className) throws CouldNotLoadJarException {
        return loadClassFromFile(theFile.toURI(), className);
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
        boolean shouldCache=true;
        
        if(shouldCache){
            return CachingJarClassLoader.loadClassFromFile(theFileURI, className);
        }
        
        URLClassLoader urlLoader = null;

        try {
            
            urlLoader = new URLClassLoader(new URL[]{theFileURI.toURL()});
            theClass = urlLoader.loadClass(className);
        } catch (Throwable e) {
            String errorString = "SERIOUS ERROR: When JarClassLoader tried to load class: " + className + " from file: " + theFileURI.toString();
            throw new CouldNotLoadJarException(errorString, e);
        }
        return theClass;
    }

    /**
    @param theJarFile A File object that is the Jar we want to load from
    @param theClassName The fully qualified name of the class to load
    @return Like loadClassFromFile, except returns null if there is a problem instead of throwing an Exception
     */
    public static Class<?> loadClassFromFileQuiet(File theJarFile, String theClassName) {
        return loadClassFromFileQuiet(theJarFile, theClassName, false);
    }

    /**
     *
    @deprecated
    @param theJarFile A File object that is the Jar we want to load from
    @param theClassName The fully qualified name of the class to load
    @param dumpTheStack If this is true, a stack trace will be produced if there is an error
    @return Like loadClassFromFile, except returns null if there is a problem instead of throwing an Exception
     */
    public static Class<?> loadClassFromFileQuiet(File theJarFile, String theClassName, boolean dumpTheStack) {
        return loadClassFromFileQuiet(theJarFile.toURI(), theClassName, dumpTheStack);
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
        }catch(Throwable T){
            System.out.println("Misc. problem loading class: "+thisClassName+" \n"+T);
            return null;
        }
        return theClass;
    }
}
