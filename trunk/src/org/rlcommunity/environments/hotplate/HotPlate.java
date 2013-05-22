/*
Copyright 2007 Brian Tanner
http://rl-library.googlecode.com/
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
package org.rlcommunity.environments.hotplate;

import java.net.URL;
import org.rlcommunity.environments.hotplate.messages.StateResponse;
import rlVizLib.Environments.EnvironmentBase;
import rlVizLib.general.ParameterHolder;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvironmentMessageParser;
import rlVizLib.messaging.environment.EnvironmentMessages;
import rlVizLib.messaging.interfaces.HasAVisualizerInterface;
import rlVizLib.messaging.interfaces.getEnvMaxMinsInterface;
import rlVizLib.messaging.interfaces.getEnvObsForStateInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

import org.rlcommunity.environments.hotplate.visualizer.HotPlateVisualizer;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;
import rlVizLib.general.hasVersionDetails;

import rlVizLib.messaging.environmentShell.TaskSpecPayload;
import rlVizLib.messaging.interfaces.HasImageInterface;


public class HotPlate extends EnvironmentBase implements
        getEnvMaxMinsInterface,
        getEnvObsForStateInterface,
        HasAVisualizerInterface,
        HasImageInterface {

    protected HotPlateState theState = null;

    public static TaskSpecPayload getTaskSpecPayload(ParameterHolder P) {
        HotPlate theHotPlate = new HotPlate(P);
        String taskSpecString = theHotPlate.makeTaskSpec().getStringRepresentation();
        return new TaskSpecPayload(taskSpecString, false, "");
    }

    public TaskSpec makeTaskSpec() {
        int numDimensions=theState.getNumDimensions();
        int numActions=theState.getNumActions();
        
        TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
        theTaskSpecObject.setEpisodic();
        theTaskSpecObject.setDiscountFactor(1.0d);
        theTaskSpecObject.addContinuousObservation(new DoubleRange(0.0d,1.0d,numDimensions));

        if(theState.getSignaled()){
            theTaskSpecObject.addDiscreteObservation(new IntRange(0,1<<numDimensions));
        }else{
        }

        theTaskSpecObject.addDiscreteAction(new IntRange(0, numActions-1));
        theTaskSpecObject.setRewardRange(new DoubleRange(-1.0d, 1.0d));
        theTaskSpecObject.setExtra("EnvName:HotPlate");

        String taskSpecString = theTaskSpecObject.toTaskSpec();
        TaskSpec.checkTaskSpec(taskSpecString);

        return new TaskSpec(theTaskSpecObject);

    }

    public HotPlateState getState() {
        return theState;
    }

    public String env_init() {
        return makeTaskSpec().getStringRepresentation();

    }

    /**
     * Reset the state of the world to initial conditions.
     * @return
     */
    public Observation env_start() {
        theState.reset();
        return makeObservation();
    }

    /**
     * Takes a step.  If an invalid action is selected, choose a random action.
     * @param theAction
     * @return
     */
    public Reward_observation_terminal env_step(Action theAction) {

        int a = theAction.intArray[0];


        if (a >= theState.getNumActions() || a < 0) {
            System.err.println("Invalid action selected in HotPlate: " + a);
            //Take the null action
            a = theState.getNumActions()-1;
        }

        theState.update(a);

        return makeRewardObservation(theState.getReward(), theState.inGoalRegion());
    }

    /**
     * Return the ParameterHolder object that contains the default parameters for
     * this environment.
     * @return
     */
    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        rlVizLib.utilities.UtilityShop.setVersionDetails(p, new DetailsProvider());


//        p.addIntegerParam("Dimensions", 2);
        p.addBooleanParam("Signaled", false);
        p.addIntegerParam("RandomSeed(0 means random)", 0);
        p.addBooleanParam("RandomStartStates", false);
        p.addDoubleParam("TransitionNoise[0,1]", 0.0d);
        p.setAlias("noise", "TransitionNoise[0,1]");
        p.setAlias("seed", "RandomSeed(0 means random)");
        return p;
    }

    /**
     * Create a new environment using parameter settings in p.
     * @param p
     */
    public HotPlate(ParameterHolder p) {
        super();
        int dimensions=2;
        boolean signaled=false;

        boolean randomStartStates = false;
        double transitionNoise = 0.0d;
        long randomSeed = 0L;



        if (p != null) {
            if (!p.isNull()) {
                signaled = p.getBooleanParam("Signaled");
//                dimensions=p.getIntegerParam("Dimensions");
                randomStartStates = p.getBooleanParam("RandomStartStates");
                transitionNoise = p.getDoubleParam("noise");
                randomSeed = p.getIntegerParam("seed");
             }
        }
        theState = new HotPlateState(dimensions,signaled,randomStartStates,transitionNoise,randomSeed);
    }

    public HotPlate() {
        this(getDefaultParameters());
    }

    /**
     * Handles messages that find out the version, what visualizer is available, 
     * etc.
     * @param theMessage
     * @return
     */
    public String env_message(String theMessage) {
        EnvironmentMessages theMessageObject;
        try {
            theMessageObject = EnvironmentMessageParser.parseMessage(theMessage);
        } catch (NotAnRLVizMessageException e) {
            System.err.println("Someone sent "+getClass().getName()+" a message that wasn't RL-Viz compatible");
            return "I only respond to RL-Viz messages!";
        }

        if (theMessageObject.canHandleAutomatically(this)) {
            String theResponseString = theMessageObject.handleAutomatically(this);
            return theResponseString;
        }

        //If it wasn't handled automatically, maybe its a custom message
        if (theMessageObject.getTheMessageType() == rlVizLib.messaging.environment.EnvMessageType.kEnvCustom.id()) {

            String theCustomType = theMessageObject.getPayLoad();

            if (theCustomType.equals("GETHOTPLATESTATE")) {
                //It is a request for the state
                StateResponse theResponseObject = new StateResponse(theState.getLastAction(), theState.getPosition(),theState.getSafeZone(),theState.getSignaled());
                return theResponseObject.makeStringResponse();
            }

        }
        System.err.println("We need some code written in Env Message for "+getClass().getName()+"... unknown request received: " + theMessage);
        Thread.dumpStack();
        return null;
    }

    public static void main(String[] args) {
        EnvironmentLoader L = new EnvironmentLoader(new HotPlate());
        L.run();
    }

    /**
     * Turns theState object into an observation.
     * @return
     */
    @Override
    protected Observation makeObservation() {
        return theState.makeObservation();
    }

    public void env_cleanup() {
        theState.reset();
    }

    /**
     * @param dimension
     * @return
     */
    public double getMaxValueForQuerableVariable(int dimension) {
        return 1.0d;
    }

    /**
     * @param dimension
     * @return
     */
    public double getMinValueForQuerableVariable(int dimension) {
        return 0.0d;
    }

    /**
     * Given a state, return an observation.  This is trivial 
     * because the observation is the same as the internal state 
     * @param theState
     * @return
     */
    public Observation getObservationForState(Observation theState) {
        return theState;
    }

    /**
     * How many state variables are there (used for value function drawing)
     * @return
     */
    public int getNumVars() {
        int numDims=theState.getNumDimensions();
        if(numDims>2)numDims=2;
        return numDims;
    }

    public String getVisualizerClassName() {
        return HotPlateVisualizer.class.getName();
    }

    /**
     * So we can draw a pretty image in the visualizer before we start
     * @return
     */
    public URL getImageURL() {
        URL imageURL = HotPlate.class.getResource("/images/splashscreen_hotplate.png");
        return imageURL;
    }
}
/**
 * This is a little helper class that fills in the details about this environment
 * for the fancy print outs in the visualizer application.
 * @author btanner
 */
class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "Hot Plate 0.1";
    }

    public String getShortName() {
        return "HotPlate";
    }

    public String getAuthors() {
        return "Brian Tanner (inspired by Tyler Streeter)";
    }

    public String getInfoUrl() {
        return "http://library.rl-community.org/";
    }

    public String getDescription() {
        return "RL-Library Java Version of the Hot Plate problem.";
    }
}

