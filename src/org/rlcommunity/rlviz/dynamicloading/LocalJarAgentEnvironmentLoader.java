package org.rlcommunity.rlviz.dynamicloading;

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
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import rlVizLib.general.ParameterHolder;
import rlVizLib.general.RLVizVersion;
import rlVizLib.utilities.UtilityShop;

/**
 * My belief is that our classes are leaking like crazy because we are forever 
 * loading them using new classloaders and a bunch of the statically allocated 
 * stuff is just hanging around.
 * 
 * I want to cache loaded classes so we only load them a single time.
 * @author btanner
 *
 */
public class LocalJarAgentEnvironmentLoader implements DynamicLoaderInterface {

    protected Vector<String> thePublicNames = null;
    protected Map<String, ClassSourcePair> publicNameToClassSource = null;
    protected Map<String, ParameterHolder> publicNameToParameterHolder = null;
    private Vector<URI> theUriList = new Vector<URI>();
    private ClassExtractor theClassExtractor;
    //This seems like we're breaking OO rules
    private EnvOrAgentType theLoaderType;

    public String getClassName(String theName) {
        StringTokenizer theTokenizer = new StringTokenizer(theName, ".");
        String name = "undefined";
        while (theTokenizer.hasMoreTokens()) {
            name = theTokenizer.nextToken();
        }
        return name;
    }

    public LocalJarAgentEnvironmentLoader(Vector<URI> uriList, EnvOrAgentType theLoaderType) {
        theUriList.addAll(uriList);
        this.theLoaderType = theLoaderType;

        CompositeResourceGrabber theCompJarGrabber = new CompositeResourceGrabber();

        FileFilter theJarFileFilter = new JarFileFilter();
        for (URI uri : uriList) {
            LocalDirectoryGrabber thisGrabber = new LocalDirectoryGrabber(uri);
            thisGrabber.addFilter(theJarFileFilter);

            theCompJarGrabber.add(thisGrabber);

        }
        theClassExtractor = new ClassExtractor(theCompJarGrabber);
    }

    public boolean makeList() {
        thePublicNames = new Vector<String>();
        publicNameToClassSource = new TreeMap<String, ClassSourcePair>();
        publicNameToParameterHolder = new TreeMap<String, ParameterHolder>();

        Vector<ClassSourcePair> allMatching = new Vector<ClassSourcePair>();
        Vector<Class<?>> excludeList=new Vector<Class<?>>();

        excludeList.add(rlVizLib.dynamicLoading.Unloadable.class);
        excludeList.add(org.rlcommunity.rlviz.dynamicloading.Unloadable.class);

        Vector<Class<?>> envAsList=new Vector<Class<?>>();
        Vector<Class<?>> agentAsList=new Vector<Class<?>>();

        envAsList.add(EnvironmentInterface.class);
        agentAsList.add(AgentInterface.class);
        
        if (theLoaderType.id() == EnvOrAgentType.kBoth.id()) {
            //System.out.println("-------Loading both types");
            allMatching = theClassExtractor.getAllClassesThatImplement(envAsList, excludeList);
            allMatching.addAll(theClassExtractor.getAllClassesThatImplement(agentAsList, excludeList));
        }
        if (theLoaderType.id() == EnvOrAgentType.kEnv.id()) {
            //System.out.println("-------Loading kEnv types");
            allMatching = theClassExtractor.getAllClassesThatImplement(envAsList, excludeList);
        }
        if (theLoaderType.id() == EnvOrAgentType.kAgent.id()) {
            //System.out.println("-------Loading kAgent types");
            allMatching = theClassExtractor.getAllClassesThatImplement(agentAsList, excludeList);
        }


        //Filter out all of the abstract classes
        Map<String, Vector<ClassSourcePair>> shortNameCounts = new TreeMap<String, Vector<ClassSourcePair>>();

        for (ClassSourcePair thisClassDetails : allMatching) {
            if (!isAbstractClass(thisClassDetails.getTheClass())) {

                String longName = thisClassDetails.getTheClass().getName();
                String shortName = getClassName(longName);

                if (!shortNameCounts.containsKey(shortName)) {
                    Vector<ClassSourcePair> thisShortNameList = new Vector<ClassSourcePair>();
                    shortNameCounts.put(shortName, thisShortNameList);
                }
                shortNameCounts.get(shortName).add(thisClassDetails);
            }
        }

        SortedSet<String> sortedShortNames = new TreeSet<String>(shortNameCounts.keySet());


        for (String thisShortName : sortedShortNames) {
            Vector<ClassSourcePair> thisShortNameClasses = shortNameCounts.get(thisShortName);

            for (int i = 0; i < thisShortNameClasses.size(); i++) {
                String suffix = "";
                if (i > 0) {
                    suffix = " [" + i + "]";
                }
                String thisPublicName = thisShortName + suffix;
                thePublicNames.add(thisPublicName);
                ClassSourcePair thisClassDetails = thisShortNameClasses.get(i);
                publicNameToClassSource.put(thisPublicName, thisClassDetails);

                checkVersions(thisClassDetails.getTheClass());
                ParameterHolder thisP = loadParameterHolderFromFile(thisClassDetails.getTheClass());

                String sourceJarPath = "unknown";
                sourceJarPath = thisClassDetails.getURI().normalize().toString();

                UtilityShop.addSourceDetails(thisP, thisClassDetails.getTheClass().getName(), sourceJarPath);
                publicNameToParameterHolder.put(thisPublicName, thisP);
            }

        }


        return true;
    }


    public Vector<String> getNames() {
        if (thePublicNames == null) {
            makeList();
        }

        return thePublicNames;
    }


    public Object load(String shortName, ParameterHolder theParams) {
        if (thePublicNames == null) {
            makeList();
        }

        ClassSourcePair theClassDetails = publicNameToClassSource.get(shortName);
        assert (theClassDetails != null);
        return loadFromClass(theClassDetails.getTheClass(), theParams);
    }

    private boolean isAbstractClass(Class<?> theClass) {
        int theModifiers = theClass.getModifiers();
        return Modifier.isAbstract(theModifiers);
    }

    private ParameterHolder loadParameterHolderFromFile(Class<?> theClass) {
        ParameterHolder theParamHolder = null;

        Class<?>[] emptyParams = new Class<?>[0];

        try {
            Method paramMakerMethod = theClass.getDeclaredMethod("getDefaultParameters", emptyParams);
            if (paramMakerMethod != null) {
                theParamHolder = (ParameterHolder) paramMakerMethod.invoke((Object[]) null, (Object[]) null);
            }
        } catch (Exception e) {
            return null;
        }

        return theParamHolder;
    }

    /**
     * Creates an object of type theClass using theParams if possible.  Also checks to see if the version of RLVizLib
     * that was used to compile theClass is the same as the current runtime version.  For now, doesn't do anything except 
     * print a warning if they don't match.
     * @param theClass Class to instantiate
     * @param theParams ParameterHolder to initialize the new object with
     * @return newly instantiated class of type theClass
     */
    private Object loadFromClass(Class<?> theClass, ParameterHolder theParams) {
        //before we do this, lets check compatibility
        Object theModule = null;

//Try to load a constructor that takes a parameterholder
        try {
            Constructor<?> paramBasedConstructor = theClass.getConstructor(ParameterHolder.class);
            theModule = (Object) paramBasedConstructor.newInstance(theParams);
        } catch (Exception paramBasedE) {
            //There is no ParameterHolder constructor


            if (theParams != null) {
                if (!theParams.isNull()) {
                    System.err.println("Loading Class: " + theClass.getName() + " :: A parameter holder was provided, but the JAR doesn't have a constructor that takes a parameter holder");
                    System.err.println(paramBasedE);
                    System.err.println("Nested exception: " + paramBasedE.getCause());
                }
            }
            try {
                Constructor<?> emptyConstructor = theClass.getConstructor();
                if (emptyConstructor == null) {
                    System.err.println("WTF emptyConstructor is null");
                }
                theModule = (Object) emptyConstructor.newInstance();
            } catch (Exception noParamsE) {
                System.err.println("Could't load instance of: " + theClass.getName() + " with parameters or without");
                System.err.println("Exception was: " + noParamsE);
                System.err.println("\tNested exception: " + noParamsE.getCause());
            }


        }
        return theModule;

    }

    private boolean checkVersions(Class<?> theClass) {
        RLVizVersion theLinkedLibraryVizVersion = rlVizLib.rlVizCore.getRLVizSpecVersion();
        RLVizVersion ourCompileVersion = rlVizLib.rlVizCore.getRLVizSpecVersionOfClassWhenCompiled(theClass);

        if (!theLinkedLibraryVizVersion.equals(ourCompileVersion)) {
            System.err.println("Warning :: Possible RLVizLib Incompatibility");
            System.err.println("Warning :: Runtime version used by " + theClass.getName() + " is:  " + theLinkedLibraryVizVersion);
            System.err.println("Warning :: Compile version used to build " + theClass.getName() + " is:  " + ourCompileVersion);
            return false;
        }
        return true;
    }

    public String getTypeSuffix() {
        return "- Java";
    }

    public Vector<ParameterHolder> getParameters() {
        Vector<ParameterHolder> theParams = new Vector<ParameterHolder>();
        for (String thisPublicName : thePublicNames) {
            theParams.add(publicNameToParameterHolder.get(thisPublicName));
        }
        return theParams;
    }
}


