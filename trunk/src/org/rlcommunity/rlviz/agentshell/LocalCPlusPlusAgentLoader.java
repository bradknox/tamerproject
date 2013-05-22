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

package org.rlcommunity.rlviz.agentshell;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlviz.dynamicloading.CompositeResourceGrabber;
import org.rlcommunity.rlviz.dynamicloading.SharedLibraryGrabber;

import rlVizLib.general.ParameterHolder;
import rlVizLib.messaging.agentShell.TaskSpecResponsePayload;

/**
 *	Java class to talk to a C++ counterpart, 
 *
 *	agentShell -> LocalCPlusPlusAgentLoader (java) -> CPlusPlusAgentLoader (C++)
 */
public class LocalCPlusPlusAgentLoader implements AgentLoaderInterface {

    // C++ functions to be called from within Java
    public native String JNIgetAgentParams(String fullFilePath);

    Vector<URI> allCPPAgentURIs = new Vector<URI>();
    private Vector<String> theNames = new Vector<String>();
    private Vector<ParameterHolder> theParamHolders = new Vector<ParameterHolder>();
    private Map<String, URI> publicNameToFullName = new TreeMap<String, URI>();
    private Set<URI> allFullURIName = new TreeSet<URI>();

    /**
     * CPPENV.dylib is assumed to be in the same directory as the envShell jar.
     */
    public LocalCPlusPlusAgentLoader() {
        loadLoader();
    }


    /**
     * The libRLVizCPPAgentLoader.dylib is the library that allows the c++ agents to
     * be used in java
     */
    private void loadLoader() {
        String CPPLibDir=AgentShellPreferences.getInstance().getJNILoaderLibDir() + File.separator+ "libRLVizCPPAgentLoader.dylib";
        System.load(CPPLibDir);
    }

    private String getShortAgentNameFromURI(URI theURI) {
        String pathAsString = theURI.getPath();

        StringTokenizer toke = new StringTokenizer(pathAsString, File.separator);

        String fileName = "";
        while (toke.hasMoreTokens()) {
            fileName = toke.nextToken();
        }

        //Lets drop the .dylib or .so
        int lastDot=fileName.lastIndexOf(".");
        fileName=fileName.substring(0,lastDot);
        return fileName;
    }


    private String addFullNameToMap(URI theURI) {
        int num = 0;
        String theEndName = getShortAgentNameFromURI(theURI);

        String proposedShortName = theEndName;

        while (publicNameToFullName.containsKey(proposedShortName)) {
            num++;
            proposedShortName = theEndName + "(" + num + ")";
        }
        publicNameToFullName.put(proposedShortName, theURI);
        return proposedShortName;
    }

    /**
     * refresh the EnvUrlList to ensure its up to date, then for each URI in
     * the list, find all the dylibs in that directory.
     * 
     * @return true if there were no errors
     */
    public boolean makeList() {
        Vector<URI> allPlacesToLook=AgentShellPreferences.getInstance().getList();
        
        CompositeResourceGrabber compGrabber=new CompositeResourceGrabber();
        for (URI uri : allPlacesToLook) {
            SharedLibraryGrabber thisGrabber=new SharedLibraryGrabber(uri);
            //Make sure it will only find shared libraries with agents
            thisGrabber.addContentsFilter(new JNIAgentSharedLibraryContentFilter());
            compGrabber.add(thisGrabber);
        }
        compGrabber.refreshURIList();
        allCPPAgentURIs = compGrabber.getAllResourceURIs();
        

        for (URI thisURI : allCPPAgentURIs) {
            allFullURIName.add(thisURI);
            String shortName = addFullNameToMap(thisURI);
            theNames.add(shortName);

            String ParamHolderString = JNIgetAgentParams(thisURI.getPath());//JNI call like getParamHolderInStringFormat(i)
            ParameterHolder thisParamHolder = new ParameterHolder(ParamHolderString);
            theParamHolders.add(thisParamHolder);
        }
        return true;
    }

    /**
     * This method gets a count of the number of environments, then for each
     * of the environments it gets its name through a JNI call.
     * 
     * @return a Vector of env names
     */
    public Vector<String> getNames() {
        return theNames;
    }

    /**
     * 
     * @return
     */
    public Vector<ParameterHolder> getParameters() {
        return theParamHolders;
    }

    /**
     * 
     * @param agentName
     * @param theParams
     * @return
     */
    public AgentInterface loadAgent(String agentName, ParameterHolder theParams) {
        URI theAgentURI=publicNameToFullName.get(agentName);
        
        JNIAgent theAgent = new JNIAgent(theAgentURI.getPath(), theParams);
        if (theAgent.isValid()) {
            return theAgent;
        } else {
            return null;
        }
    }

    public String getTypeSuffix() {
        return "- C++";
    }

    public TaskSpecResponsePayload loadTaskSpecCompat(String localName, ParameterHolder theParams, String TaskSpec) {
         return TaskSpecResponsePayload.makeUnsupportedPayload();
    }
}
