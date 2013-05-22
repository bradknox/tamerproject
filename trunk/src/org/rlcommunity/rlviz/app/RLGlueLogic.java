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

import java.util.Vector;

import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlviz.app.gluestepper.GlueStepper;
import org.rlcommunity.rlviz.settings.RLVizSettings;

import rlVizLib.general.ParameterHolder;
import rlVizLib.general.RLVizVersion;
import rlVizLib.general.TinyGlue;
import rlVizLib.messaging.agent.AgentVersionSupportedRequest;
import rlVizLib.messaging.agent.AgentVersionSupportedResponse;
import rlVizLib.messaging.agent.AgentVisualizerNameRequest;
import rlVizLib.messaging.agent.AgentVisualizerNameResponse;
import rlVizLib.messaging.agentShell.AgentShellListRequest;
import rlVizLib.messaging.agentShell.AgentShellListResponse;
import rlVizLib.messaging.agentShell.AgentShellLoadRequest;
import rlVizLib.messaging.agentShell.AgentShellLoadResponse;
import rlVizLib.messaging.agentShell.AgentShellUnLoadRequest;
import rlVizLib.messaging.environment.EnvVersionSupportedRequest;
import rlVizLib.messaging.environment.EnvVersionSupportedResponse;
import rlVizLib.messaging.environment.EnvVisualizerNameRequest;
import rlVizLib.messaging.environment.EnvVisualizerNameResponse;
import rlVizLib.messaging.environmentShell.EnvShellListRequest;
import rlVizLib.messaging.environmentShell.EnvShellListResponse;
import rlVizLib.messaging.environmentShell.EnvShellLoadRequest;
import rlVizLib.messaging.environmentShell.EnvShellLoadResponse;
import rlVizLib.messaging.environmentShell.EnvShellUnLoadRequest;
import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.interfaces.DynamicControlTarget;

public class RLGlueLogic {

//	Singleton pattern, should make sure its thread safe
    static RLGlueLogic theGlobalGlueLogic = null;

    static public RLGlueLogic getGlobalGlueLogic() {
        if (theGlobalGlueLogic == null) {
            theGlobalGlueLogic = new RLGlueLogic();
        }
        return theGlobalGlueLogic;
    }
    private boolean debugLocal = false;
    private TinyGlue myGlueState = null;
    private AbstractVisualizer theEnvVisualizer = null;
    private DynamicControlTarget theEnvVisualizerControlTarget = null;
    private AbstractVisualizer theAgentVisualizer = null;
    private DynamicControlTarget theAgentVisualizerControlTarget = null;
    private Vector<visualizerLoadListener> envVisualizerLoadListeners = new Vector<visualizerLoadListener>();
    private Vector<visualizerLoadListener> agentVisualizerLoadListeners = new Vector<visualizerLoadListener>();
    private RLVizVersion theEnvVersion = null;
    private RLVizVersion theAgentVersion = null;
    GlueStepper theTimeKeeper = null;

    protected RLGlueLogic() {
        theTimeKeeper = new GlueStepper(this);
    }

    public TinyGlue getGlueState() {
        return myGlueState;
    }

    public RLVizVersion getEnvVersion() {
        return theEnvVersion;
    }

    public RLVizVersion getAgentVersion() {
        return theAgentVersion;
    }

    public AbstractVisualizer getAgentVisualizer(){
        return theAgentVisualizer;
    }
    public AbstractVisualizer getEnvVisualizer(){
        return theEnvVisualizer;
    }

    public void step() {
        myGlueState.step();
    }
    private EnvShellListResponse theEnvListResponseObject = null;
    private AgentShellListResponse theAgentListResponseObject = null;

    public Vector<String> getEnvNameList() {
        //Get the Environment Names
        if (theEnvListResponseObject == null) {
            theEnvListResponseObject = EnvShellListRequest.Execute();
        }
        return theEnvListResponseObject.getTheEnvList();
    }

    public Vector<ParameterHolder> getEnvParamList() {
        //Get the Environment Parameters
        if (theEnvListResponseObject == null) {
            theEnvListResponseObject = EnvShellListRequest.Execute();
        }
        return theEnvListResponseObject.getTheParamList();
    }

    public Vector<String> getAgentNameList() {
        //Get the Agent Names
        if (theAgentListResponseObject == null) {
            theAgentListResponseObject = AgentShellListRequest.Execute();
        }
        return theAgentListResponseObject.getTheAgentList();
    }

    public Vector<ParameterHolder> getAgentParamList() {
        //Get the Agent Parameters
        if (theAgentListResponseObject == null) {
            theAgentListResponseObject = AgentShellListRequest.Execute();
        }
        return theAgentListResponseObject.getTheParamList();
    }

    public void loadEnvironmentVisualizer() {
        //Get the visualizer name if I Can
        EnvVisualizerNameResponse theNameResponse = EnvVisualizerNameRequest.Execute();
        String theVisualizerName = theNameResponse.getTheVisualizerClassName();

        //Only load the env visualizer if someone will care to draw it
        if (envVisualizerLoadListeners.size() > 0) {
            theEnvVisualizer = VisualizerFactory.createEnvVisualizerFromClassName(theVisualizerName, myGlueState, theEnvVisualizerControlTarget);

            if (theEnvVisualizer != null) {
                notifyEnvVisualizerListenersNewEnv();
            } else {
                System.out.println("Caught a NULL ENV visualizer. Vizualiser not Loaded");
            }
        }
    }

    public void loadAgentVisualizer() {
        //Get the visualizer name if I Can
        AgentVisualizerNameResponse theNameResponse = AgentVisualizerNameRequest.Execute();
        String theVisualizerName = theNameResponse.getTheVisualizerClassName();
        //Only load the agent visualizer if someone will care to draw it
        if (agentVisualizerLoadListeners.size() > 0) {

            theAgentVisualizer = VisualizerFactory.createAgentVisualizerFromClassName(theVisualizerName, myGlueState, theAgentVisualizerControlTarget);

            if (theAgentVisualizer != null) {
                notifyAgentVisualizerListenersNewAgent();
            } else {
                System.out.println("Caught a NULL AGENT visualizer. Vizualiser not Loaded");
            }
        }
    }

    public boolean loadEnvironment(String envName, ParameterHolder currentParams) {

        EnvShellLoadResponse theLoadResponse = EnvShellLoadRequest.Execute(envName, currentParams);
        if (!theLoadResponse.getTheResult()) {
            return false;
        }
        EnvVersionSupportedResponse versionResponse = EnvVersionSupportedRequest.Execute();

        //this shouldn't happen anyway
        if (versionResponse != null) {
            theEnvVersion = versionResponse.getTheVersion();
        } else {
            theEnvVersion = RLVizVersion.NOVERSION;
        }
        return true;

    }

    public boolean loadAgent(String agentName, ParameterHolder agentParams) {
        AgentShellLoadResponse theLoadResponse = AgentShellLoadRequest.Execute(agentName, agentParams);
        if (!theLoadResponse.getTheResult()) {
            return false;
        }
        AgentVersionSupportedResponse versionResponse = AgentVersionSupportedRequest.Execute();

//		//this shouldn't happen anyway
        if (versionResponse != null) {
            theEnvVersion = versionResponse.getTheVersion();
        } else {
            theAgentVersion = RLVizVersion.NOVERSION;
        }
        return true;
    }

    public void setNewStepDelay(int stepDelay) {
        theTimeKeeper.setNewStepDelay(stepDelay);
    }

    public void setEnvironmentVisualizerControlTarget(DynamicControlTarget theTarget) {
        theEnvVisualizerControlTarget = theTarget;
    }

    public void setAgentVisualizerControlTarget(DynamicControlTarget theTarget) {
        theAgentVisualizerControlTarget = theTarget;
    }

    void RL_init() {
        RLGlue.RL_init();
    }

    void startVisualizers() {
        //This is not ideal.. getting bad fast
        if (theEnvVisualizer != null) {
            if (!theEnvVisualizer.isCurrentlyRunning()) {
                theEnvVisualizer.startVisualizing();
            }
        }
        if (theAgentVisualizer != null) {
            if (!theAgentVisualizer.isCurrentlyRunning()) {
                theAgentVisualizer.startVisualizing();
            }
        }
    }

    private void notifyEnvVisualizerListenersNewEnv() {
        for (visualizerLoadListener thisListener : envVisualizerLoadListeners) {
            thisListener.notifyVisualizerLoaded(theEnvVisualizer);
        }
    }

    private void notifyAgentVisualizerListenersNewAgent() {
        for (visualizerLoadListener thisListener : agentVisualizerLoadListeners) {
            thisListener.notifyVisualizerLoaded(theAgentVisualizer);
        }
    }

    public void addEnvVisualizerLoadListener(visualizerLoadListener thisListener) {
        envVisualizerLoadListeners.add(thisListener);
    }

    public void addAgentVisualizerLoadListener(visualizerLoadListener thisListener) {
        agentVisualizerLoadListeners.add(thisListener);
    }

    public void startNewExperiment() {
        myGlueState = new TinyGlue();
    }

    public void unloadExperiment() {
        notifyEnvVisualizerListenersUnloadEnv();
        notifyAgentVisualizerListenersUnloadAgent();

        startUnloadEnvironment();
        startUnloadAgent();

        myGlueState = null;

        //Only cleanup if we have inited before
        if (RLGlue.isInited()) {
            RLGlue.RL_cleanup();
        }
        finishUnloadEnvironment();
        finishUnloadAgent();
    }

    private void startUnloadEnvironment() {
        if (theEnvVisualizer != null) {
            theEnvVisualizer.stopVisualizing();
        }
        theEnvVisualizer = null;
    }

    private void startUnloadAgent() {
        if (theAgentVisualizer != null) {
            theAgentVisualizer.stopVisualizing();
        }
        theAgentVisualizer = null;
    }

    private void finishUnloadEnvironment() {

        if (RLVizSettings.getBooleanSetting("list-environments")) {
            EnvShellUnLoadRequest.Execute();
        }
    }

    private void finishUnloadAgent() {
        if (RLVizSettings.getBooleanSetting("list-agents")) {
            AgentShellUnLoadRequest.Execute();
        }
    }

    private void notifyEnvVisualizerListenersUnloadEnv() {
        for (visualizerLoadListener thisListener : envVisualizerLoadListeners) {
            thisListener.notifyVisualizerUnLoaded();
        }
    }

    private void notifyAgentVisualizerListenersUnloadAgent() {
        for (visualizerLoadListener thisListener : agentVisualizerLoadListeners) {
            thisListener.notifyVisualizerUnLoaded();
        }
    }

    public void start() {
        theTimeKeeper.start();
    }

    public void stop() {
        theTimeKeeper.stop();
    }

}





