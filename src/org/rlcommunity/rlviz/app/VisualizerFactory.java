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


package org.rlcommunity.rlviz.app;

import java.lang.reflect.Constructor;
import java.util.Vector;

import org.rlcommunity.rlviz.settings.RLVizSettings;
import org.rlcommunity.rlviz.dynamicloading.ClassExtractor;
import org.rlcommunity.rlviz.dynamicloading.ClassSourcePair;
import org.rlcommunity.rlviz.dynamicloading.JarFileFilter;
import org.rlcommunity.rlviz.dynamicloading.LocalDirectoryGrabber;
import org.rlcommunity.rlviz.dynamicloading.Unloadable;

import rlVizLib.general.TinyGlue;
import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.interfaces.DynamicControlTarget;

public class VisualizerFactory {
    static    boolean debugThis = true;

    static String defaultEnvVisualizerClassName = "org.rlcommunity.visualizers.generic.GenericEnvVisualizer";
    static String defaultAgentVisualizerClassName = "org.rlcommunity.visualizers.generic.GenericAgentVisualizer";
    public static AbstractVisualizer createEnvVisualizerFromClassName(String VizClassName, TinyGlue theGlueState, DynamicControlTarget theControlTarget) {
        String envJarPath=RLVizSettings.getStringSetting("environment-jar-path");
        return createVisualizerFromClassName(VizClassName, defaultEnvVisualizerClassName, theGlueState, theControlTarget,envJarPath);
    }

    public static AbstractVisualizer createAgentVisualizerFromClassName(String VizClassName, TinyGlue theGlueState, DynamicControlTarget theControlTarget) {
        String agentJarPath=RLVizSettings.getStringSetting("agent-jar-path");
        return createVisualizerFromClassName(VizClassName, defaultAgentVisualizerClassName, theGlueState, theControlTarget,agentJarPath);
    }

    public static AbstractVisualizer createVisualizerFromClassName(String theVisualizerClassName, String defaultClassName, TinyGlue theGlueState, DynamicControlTarget theControlTarget, String libPath) {

        if (debugThis){
            System.out.println("AbstractVisualizer::createVisualizerFromClassName");
            System.out.println("\t\t theVisualizerClassName=" + theVisualizerClassName);
            System.out.println("\t\t defaultClassName=" + defaultClassName);
        }

        AbstractVisualizer theViz = null;
//        String libPath = rlVizLib.utilities.UtilityShop.getLibraryPath();

        LocalDirectoryGrabber theJarGrabber=new LocalDirectoryGrabber(libPath);
        theJarGrabber.addFilter(new JarFileFilter());

        ClassExtractor theClassExtractor = new ClassExtractor(theJarGrabber);

        Vector<ClassSourcePair> allViz = theClassExtractor.getAllClassesThatImplement(AbstractVisualizer.class, Unloadable.class);

        Class<?> GenericVisualizer = null;
        Class<?> theClass = null;

        for (ClassSourcePair thisClassDetails : allViz) {
            if (thisClassDetails.getTheClass().getName().equals(theVisualizerClassName)) {
                theClass = thisClassDetails.getTheClass();
            }

            //Might as well look for this while we're looping through the vizualizers
            if (thisClassDetails.getTheClass().getName().equals(defaultClassName)) {
                GenericVisualizer = thisClassDetails.getTheClass();
            }
        }

        if (theClass == null) {
            System.err.println("Couldn't find: " + theVisualizerClassName + " falling back to generic");
            theClass = GenericVisualizer;
        }

        if (theClass == null) {
            System.err.println("Couldn't find the generic visualizer either: " + defaultClassName + " :: no viz for you");
            return null;
        }

        //If we couldn't load the class they asked for (shock!) we should load the default visualizer

        //First try and load the one with all parameters
        theViz = createVisualizer(theClass, theGlueState, theControlTarget);

        //IF that didn't work, try without theControlTarget
        if (theViz == null) {
            if (debugThis) {
                System.out.println("\t Failed to load triple constructor");
            }
            theViz = createVisualizer(theClass, theGlueState);
        }
        //IF that didn't work, try without theGlueState
        if (theViz == null) {
            if (debugThis) {
                System.out.println("\t Failed to load double constructor");
            }
            theViz = createVisualizer(theClass);
        }

        if (theViz == null) {
            if (debugThis) {
                System.out.println("\t Failed to load single constructor");
            }
            //Ok at this point, either we didn't load the class for the real visualizer or we couldn't load any constructors
            //In either case, move on to the default visualizer
        }

        //Not sure what we ended up with, but in any case, return it
        return theViz;
    }

    public static AbstractVisualizer createVisualizer(Class<?> theVizClass) {
        
        AbstractVisualizer theVisualizer = null;
        Class<?>[] emptyParams = new Class<?>[0];

        Constructor<?> theConstructor = null;
        try {
            theConstructor = theVizClass.getConstructor(emptyParams);
            theVisualizer = (AbstractVisualizer) theConstructor.newInstance();
        } catch (Exception e) {
            if(debugThis){
                System.out.println(e);
            }
            return null;
        }
        return theVisualizer;
    }

    public static AbstractVisualizer createVisualizer(Class<?> theVizClass, TinyGlue theGlueState) {
        AbstractVisualizer theVisualizer = null;
        Class<?>[] emptyParams = new Class<?>[0];

        Constructor<?> theConstructor = null;
        try {
            theConstructor = theVizClass.getConstructor(TinyGlue.class);
            theVisualizer = (AbstractVisualizer) theConstructor.newInstance(theGlueState);
        } catch (Exception e) {
            if(debugThis){
                System.out.println(e);
            }
            return null;
        }
        return theVisualizer;
    }

    public static AbstractVisualizer createVisualizer(Class<?> theVizClass, TinyGlue theGlueState, DynamicControlTarget theControlTarget) {
        AbstractVisualizer theVisualizer = null;
        Class<?>[] emptyParams = new Class<?>[0];

        Constructor<?> theConstructor = null;
        try {
            theConstructor = theVizClass.getConstructor(TinyGlue.class, DynamicControlTarget.class);
            theVisualizer = (AbstractVisualizer) theConstructor.newInstance(theGlueState, theControlTarget);
        } catch (Exception e) {
            if(debugThis){
                System.out.println(e);
                if(e.getCause()!=null){
                    System.out.println(e.getCause());
                    e.getCause().printStackTrace();
                }
            }
            return null;
        }
        return theVisualizer;
    }
}


