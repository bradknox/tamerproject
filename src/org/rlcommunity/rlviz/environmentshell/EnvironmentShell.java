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
package org.rlcommunity.rlviz.environmentshell;

import java.util.Map;

import java.util.TreeMap;
import java.util.Vector;

import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;
import org.rlcommunity.rlviz.settings.RLVizSettings;

import rlVizLib.dynamicLoading.Unloadable;
import rlVizLib.general.ParameterHolder;
import rlVizLib.general.RLVizVersion;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environmentShell.EnvShellListResponse;
import rlVizLib.messaging.environmentShell.EnvShellLoadRequest;
import rlVizLib.messaging.environmentShell.EnvShellLoadResponse;
import rlVizLib.messaging.environmentShell.EnvShellMessageType;
import rlVizLib.messaging.environmentShell.EnvShellRefreshResponse;
import rlVizLib.messaging.environmentShell.EnvShellTaskSpecRequest;
import rlVizLib.messaging.environmentShell.EnvShellTaskSpecResponse;
import rlVizLib.messaging.environmentShell.EnvShellUnLoadResponse;
import rlVizLib.messaging.environmentShell.EnvironmentShellMessageParser;
import rlVizLib.messaging.environmentShell.EnvironmentShellMessages;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;

public class EnvironmentShell implements EnvironmentInterface, Unloadable {

    protected String libDir;

    static {
        RLVizVersion theLinkedLibraryVizVersion = rlVizLib.rlVizCore.getRLVizSpecVersion();
        RLVizVersion ourCompileVersion = rlVizLib.rlVizCore.getRLVizSpecVersionOfClassWhenCompiled(EnvironmentShell.class);

        if (!theLinkedLibraryVizVersion.equals(ourCompileVersion)) {
            System.err.println("Warning :: Possible RLVizLib Incompatibility");
            System.err.println("Warning :: Runtime version used by EnvironmentShell is:  " + theLinkedLibraryVizVersion);
            System.err.println("Warning :: Compile version used to build EnvironmentShell is:  " + ourCompileVersion);
        }
    }
    private EnvironmentInterface theEnvironment = null;
    Map<String, EnvironmentLoaderInterface> mapFromUniqueNameToLoader = null;
    Map<String, String> mapFromUniqueNameToLocalName = null;
    Vector<EnvironmentLoaderInterface> theEnvironmentLoaders = null;
    Vector<String> envNameVector = null;
    Vector<ParameterHolder> envParamVector = null;

    public EnvironmentShell() {
        if (RLVizSettings.isStringParamSet("agent-environment-jar-path")) {
            RLVizSettings.overrideStringSetting("environment-jar-path", RLVizSettings.getStringSetting("agent-environment-jar-path"));
        }
        this.refreshList();
    }

    public String env_init() {
        return theEnvironment.env_init();
    }

    public Observation env_start() {
        Observation o = theEnvironment.env_start();
        return o;
    }

    public Reward_observation_terminal env_step(Action arg0) {
		System.out.println("step a");
        Reward_observation_terminal RO = theEnvironment.env_step(arg0);
        return RO;
    }

    public void env_cleanup() {
        theEnvironment.env_cleanup();
    }

    public String env_message(String theMessage) {
        /**
         * See if we can parse this message as an RL-Viz message.  If not, pass
         * it along to the underlying environment if there is one.
         *
         * If we can parse it, and it's for the Shell, handle it.  If we do parse
         * it, and it's for the underlying environment, pass it through.
         */
        GenericMessage theGenericMessage;
        try {
            theGenericMessage = new GenericMessage(theMessage);
        } catch (NotAnRLVizMessageException e) {
            if (theEnvironment == null) {
                System.err.println("Someone sent EnvironmentShell a message that wasn't RL-Viz compatible");
                return "I only respond to RL-Viz messages!";
            } else {
                return theEnvironment.env_message(theMessage);
            }
        }
        /**
         * Check if this message is destined for the environment shell or the environment
         */
        if (theGenericMessage.getTo().id() == MessageUser.kEnvShell.id()) {
            /**
             * This message is for the environment shell
             */
            EnvironmentShellMessages theMessageObject = EnvironmentShellMessageParser.makeMessage(theGenericMessage);

            //Handle a request for the list of environments
            if (theMessageObject.getTheMessageType() == EnvShellMessageType.kEnvShellListQuery.id()) {
                this.refreshList();
                EnvShellListResponse theResponse = new EnvShellListResponse(envNameVector, envParamVector);

                return theResponse.makeStringResponse();
            }

            //Handle a request to actually load the environment
            if (theMessageObject.getTheMessageType() == EnvShellMessageType.kEnvShellLoad.id()) {
                EnvShellLoadRequest theCastedRequest = (EnvShellLoadRequest) theMessageObject;

                String envName = theCastedRequest.getEnvName();
                ParameterHolder theParams = theCastedRequest.getParameterHolder();

                theEnvironment = loadEnvironment(envName, theParams);

                EnvShellLoadResponse theResponse = new EnvShellLoadResponse(theEnvironment != null);

                return theResponse.makeStringResponse();
            }

            //Handle a request to actually unload the environment
            if (theMessageObject.getTheMessageType() == EnvShellMessageType.kEnvShellUnLoad.id()) {
                //Actually "load" the environment
                theEnvironment = null;

                EnvShellUnLoadResponse theResponse = new EnvShellUnLoadResponse();

                return theResponse.makeStringResponse();
            }

            if (theMessageObject.getTheMessageType() == EnvShellMessageType.kEnvShellRefresh.id()) {
                this.refreshList();

                EnvShellRefreshResponse theResponse = new EnvShellRefreshResponse(true);

                return theResponse.makeStringResponse();
            }

            //Handle a request to get a copy of the task spec from the environment
            if (theMessageObject.getTheMessageType() == EnvShellMessageType.kEnvShellTaskspec.id()) {
                EnvShellTaskSpecRequest theCastedRequest = (EnvShellTaskSpecRequest) theMessageObject;

                String envName = theCastedRequest.getEnvName();
                ParameterHolder theParams = theCastedRequest.getParameterHolder();

                EnvShellTaskSpecResponse theResponse = loadTaskSpec(envName, theParams);

                return theResponse.makeStringResponse();
            }

            System.err.println(getClass().getName() + " doesn't know how to handle message: " + theMessage);
        }
        /**
         * This message was not for the environment shell so we pass it along to the environment.
         */
        String response = theEnvironment.env_message(theMessage);
        return response;
    }

    public EnvironmentInterface loadEnvironment(String uniqueEnvName, ParameterHolder theParams) {
        EnvironmentLoaderInterface thisEnvLoader = mapFromUniqueNameToLoader.get(uniqueEnvName);
        String localName = mapFromUniqueNameToLocalName.get(uniqueEnvName);
        return thisEnvLoader.loadEnvironment(localName, theParams);
    }

    EnvShellTaskSpecResponse loadTaskSpec(String uniqueEnvName, ParameterHolder theParams) {
        EnvironmentLoaderInterface thisEnvLoader = mapFromUniqueNameToLoader.get(uniqueEnvName);
        String localName = mapFromUniqueNameToLocalName.get(uniqueEnvName);

        TaskSpecPayload theTSP = thisEnvLoader.loadTaskSpecPayload(localName, theParams);
        return new EnvShellTaskSpecResponse(theTSP);
    }

    public void refreshList() {
        mapFromUniqueNameToLoader = new TreeMap<String, EnvironmentLoaderInterface>();
        mapFromUniqueNameToLocalName = new TreeMap<String, String>();
        theEnvironmentLoaders = new Vector<EnvironmentLoaderInterface>();
        envNameVector = new Vector<String>();
        envParamVector = new Vector<ParameterHolder>();

        if (!theEnvironmentLoaders.isEmpty()) {
            theEnvironmentLoaders.clear();
        }
        //See if the environment variable for the path to the Jars has been defined
        theEnvironmentLoaders.add(new LocalJarEnvironmentLoader());

        if (RLVizSettings.getBooleanSetting("cpp-env-loading")) {
            try {
                theEnvironmentLoaders.add(new LocalCPlusPlusEnvironmentLoader());
                System.out.println("Successfully loaded the C++ loader library.");
            } catch (UnsatisfiedLinkError failure) {
                System.err.println("Unable to load libRLVizCPPEnvLoader, unable to load C/C++ environments: " + failure);
            }
        }

        for (EnvironmentLoaderInterface thisEnvLoader : theEnvironmentLoaders) {
            thisEnvLoader.makeList();
            Vector<String> thisEnvNameVector = thisEnvLoader.getNames();
            for (String localName : thisEnvNameVector) {
                String uniqueName = localName + " " + thisEnvLoader.getTypeSuffix();
                envNameVector.add(uniqueName);
                mapFromUniqueNameToLocalName.put(uniqueName, localName);
                mapFromUniqueNameToLoader.put(uniqueName, thisEnvLoader);
            }

            Vector<ParameterHolder> thisParameterVector = thisEnvLoader.getParameters();
            for (ParameterHolder thisParam : thisParameterVector) {
                envParamVector.add(thisParam);
            }

        }
    }

    public static ParameterHolder getSettings() {
        ParameterHolder envShellSettings = new ParameterHolder();
        envShellSettings.addStringParam("environment-jar-path", ".");
        envShellSettings.addStringParam("agent-environment-jar-path");
        envShellSettings.addBooleanParam("cpp-env-loading", Boolean.FALSE);
        envShellSettings.addBooleanParam("envshell-verbose-loading",Boolean.FALSE);

        if (System.getProperty("RLVIZ_LIB_PATH") != null) {
            System.err.println("Don't use the system property anymore, use the command line property environment-jar-path");
            envShellSettings.setStringParam("environment-jar-path", System.getProperty("RLVIZ_LIB_PATH"));
        }

        return envShellSettings;
    }

    public Vector<String> getEnvNames() {
        refreshList();
        return envNameVector;
    }

    public Vector<ParameterHolder> getEnvParams() {
        refreshList();
        return envParamVector;
    }

    public static void main(String[] args) {
        RLVizSettings.initializeSettings(args);
        RLVizSettings.addNewParameters(getSettings());

        EnvironmentLoader L = new EnvironmentLoader(new EnvironmentShell());
        L.run();
    }
}
