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

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;
import org.rlcommunity.rlviz.settings.RLVizSettings;

import rlVizLib.dynamicLoading.Unloadable;
import rlVizLib.general.ParameterHolder;
import rlVizLib.general.RLVizVersion;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.agentShell.AgentShellListResponse;
import rlVizLib.messaging.agentShell.AgentShellLoadRequest;
import rlVizLib.messaging.agentShell.AgentShellLoadResponse;
import rlVizLib.messaging.agentShell.AgentShellMessageParser;
import rlVizLib.messaging.agentShell.AgentShellMessageType;
import rlVizLib.messaging.agentShell.AgentShellMessages;
import rlVizLib.messaging.agentShell.AgentShellTaskSpecCompatRequest;
import rlVizLib.messaging.agentShell.AgentShellTaskSpecCompatResponse;
import rlVizLib.messaging.agentShell.AgentShellUnLoadResponse;
import rlVizLib.messaging.agentShell.TaskSpecResponsePayload;

public class AgentShell implements AgentInterface, Unloadable {
    private boolean verboseLoading=false;
    static {
        RLVizVersion theLinkedLibraryVizVersion = rlVizLib.rlVizCore.getRLVizSpecVersion();
        RLVizVersion ourCompileVersion = rlVizLib.rlVizCore.getRLVizSpecVersionOfClassWhenCompiled(AgentShell.class);

        if (!theLinkedLibraryVizVersion.equals(ourCompileVersion)) {
            System.err.println("Warning :: Possible RLVizLib Incompatibility");
            System.err.println("Warning :: Runtime version used by AgentShell is:  " + theLinkedLibraryVizVersion);
            System.err.println("Warning :: Compile version used to build AgentShell is:  " + ourCompileVersion);
        }
    }
    private AgentInterface theAgent = null;
    Map<String, AgentLoaderInterface> mapFromUniqueNameToLoader = null;
    Map<String, String> mapFromUniqueNameToLocalName = null;
    Vector<AgentLoaderInterface> theAgentLoaders = null;
    Vector<String> agentNameVector = null;
    Vector<ParameterHolder> agentParamVector = null;

    public AgentShell() {
        if (RLVizSettings.isStringParamSet("agent-environment-jar-path")) {
            RLVizSettings.overrideStringSetting("agent-jar-path", RLVizSettings.getStringSetting("agent-environment-jar-path"));
        }
        System.out.println("AgentShell starting up... verbose loading is set to: "+RLVizSettings.getBooleanSetting("agentshell-verbose-loading"));
        refreshList();
    }


    public static void main(String[] args) {
        RLVizSettings.initializeSettings(args);
        RLVizSettings.addNewParameters(getSettings());


        AgentLoader L = new AgentLoader(new AgentShell());
        L.run();
    }

    public void agent_init(String taskSpecification) {
        theAgent.agent_init(taskSpecification);
    }

    public Action agent_start(Observation observation) {
        return theAgent.agent_start(observation);
    }

    public Action agent_step(double reward, Observation observation) {
        return theAgent.agent_step(reward, observation);
    }

    public void agent_end(double reward) {
        theAgent.agent_end(reward);
    }

    public void agent_cleanup() {
        theAgent.agent_cleanup();
    }

    public String agent_message(String theMessage) {
        /**
         * See if we can parse this message as an RL-Viz message.  If not, pass
         * it along to the underlying agent if there is one.
         *
         * If we can parse it, and it's for the Shell, handle it.  If we do parse
         * it, and it's for the underlying agent, pass it through.
         */
        GenericMessage theGenericMessage;
        try {
            theGenericMessage = new GenericMessage(theMessage);
        } catch (NotAnRLVizMessageException e) {
            if (theAgent == null) {
                System.err.println("Someone sent AgentShell a message that wasn't RL-Viz compatible");
                return "I only respond to RL-Viz messages!";
            } else {
                return theAgent.agent_message(theMessage);
            }
        }
        if (theGenericMessage.getTo().id() == MessageUser.kAgentShell.id()) {
            //Its for me
            AgentShellMessages theMessageObject = AgentShellMessageParser.makeMessage(theGenericMessage);

            //Handle a request for the list of agents
            if (theMessageObject.getTheMessageType() == AgentShellMessageType.kAgentShellListRequest.id()) {
                this.refreshList();
                AgentShellListResponse theResponse = new AgentShellListResponse(agentNameVector, agentParamVector);
                return theResponse.makeStringResponse();
            }

            //Handle a request to actually load the agent
            if (theMessageObject.getTheMessageType() == AgentShellMessageType.kAgentShellLoad.id()) {
                AgentShellLoadRequest theCastedRequest = (AgentShellLoadRequest) theMessageObject;

                String envName = theCastedRequest.getAgentName();
                ParameterHolder theParams = theCastedRequest.getParameterHolder();


                theAgent = loadAgent(envName, theParams);


                AgentShellLoadResponse theResponse = new AgentShellLoadResponse(theAgent != null);

                return theResponse.makeStringResponse();
            }

            //Handle a request to actually load the agent
            if (theMessageObject.getTheMessageType() == AgentShellMessageType.kAgentShellUnload.id()) {
                //Actually "load" the agent
                theAgent = null;

                AgentShellUnLoadResponse theResponse = new AgentShellUnLoadResponse();

                return theResponse.makeStringResponse();
            }

            //Handle a request to get a copy of the task spec from the environment
            if (theMessageObject.getTheMessageType() == AgentShellMessageType.kAgentShellTaskSpecCompat.id()) {
                AgentShellTaskSpecCompatRequest theCastedRequest = (AgentShellTaskSpecCompatRequest) theMessageObject;

                String agentName = theCastedRequest.getAgentName();
                ParameterHolder theParams = theCastedRequest.getParameterHolder();
                String theTaskSpec = theCastedRequest.getTaskSpec();

                AgentShellTaskSpecCompatResponse theResponse = checkCompat(agentName, theParams, theTaskSpec);

                return theResponse.makeStringResponse();
            }



            System.err.println(getClass().getName() + " doesn't know how to handle message: " + theMessage);
        }
        //IF it wasn't for me, pass it on
        return theAgent.agent_message(theMessage);
    }

    AgentShellTaskSpecCompatResponse checkCompat(String uniqueAgentName, ParameterHolder theParams, String TaskSpec) {
        AgentLoaderInterface thisAgentLoader = mapFromUniqueNameToLoader.get(uniqueAgentName);
        String localName = mapFromUniqueNameToLocalName.get(uniqueAgentName);

        TaskSpecResponsePayload theTSP = thisAgentLoader.loadTaskSpecCompat(localName, theParams, TaskSpec);
        return new AgentShellTaskSpecCompatResponse(theTSP);
    }

    private AgentInterface loadAgent(String uniqueAgentName, ParameterHolder theParams) {
        AgentLoaderInterface thisAgentLoader = mapFromUniqueNameToLoader.get(uniqueAgentName);
        String localName = mapFromUniqueNameToLocalName.get(uniqueAgentName);
        return thisAgentLoader.loadAgent(localName, theParams);
    }

    public void refreshList() {
        mapFromUniqueNameToLoader = new TreeMap<String, AgentLoaderInterface>();
        mapFromUniqueNameToLocalName = new TreeMap<String, String>();
        theAgentLoaders = new Vector<AgentLoaderInterface>();
        agentNameVector = new Vector<String>();
        agentParamVector = new Vector<ParameterHolder>();

        if (!theAgentLoaders.isEmpty()) {
            theAgentLoaders.clear();
        }
        //See if the environment variable for the path to the Jars has been defined
        theAgentLoaders.add(new LocalJarAgentLoader());

        if (RLVizSettings.getBooleanSetting("cpp-agent-loading")) {
            if(RLVizSettings.getBooleanSetting("agentshell-verbose-loading")){
                System.out.print("\t- C/C++ agent loading is been requested...");
            }
            try {
                theAgentLoaders.add(new LocalCPlusPlusAgentLoader());
                if(RLVizSettings.getBooleanSetting("agentshell-verbose-loading")){
                    System.out.println("library loaded successfully.");
                }
            } catch (UnsatisfiedLinkError failure) {
                if(RLVizSettings.getBooleanSetting("agentshell-verbose-loading")){
                    System.out.println("failed to load library.");
                }
                System.err.println("Unable to load libRLVizCPPAgentLoader.dylib, unable to load C/C++ environments: " + failure);
            }
        }

        for (AgentLoaderInterface thisAgentLoader : theAgentLoaders) {
            thisAgentLoader.makeList();

            Vector<String> thisAgentNameVector = thisAgentLoader.getNames();
            for (String localName : thisAgentNameVector) {
                String uniqueName = localName + " " + thisAgentLoader.getTypeSuffix();
                agentNameVector.add(uniqueName);
                mapFromUniqueNameToLocalName.put(uniqueName, localName);
                mapFromUniqueNameToLoader.put(uniqueName, thisAgentLoader);
            }

            Vector<ParameterHolder> thisParameterVector = thisAgentLoader.getParameters();
            for (ParameterHolder thisParam : thisParameterVector) {
                agentParamVector.add(thisParam);
            }
        }
    }

    public static ParameterHolder getSettings() {
        ParameterHolder agentShellSettings = new ParameterHolder();
        agentShellSettings.addStringParam("agent-jar-path", ".");
        agentShellSettings.addStringParam("agent-environment-jar-path");
        agentShellSettings.addBooleanParam("cpp-agent-loading", Boolean.FALSE);
        agentShellSettings.addBooleanParam("agentshell-verbose-loading",Boolean.FALSE);

        if (System.getProperty("RLVIZ_LIB_PATH") != null) {
            agentShellSettings.setStringParam("agent-jar-path", System.getProperty("RLVIZ_LIB_PATH"));
            System.err.println("Don't use the system property anymore, use the command line property agent-jar-path");
        }

        return agentShellSettings;
    }
}
