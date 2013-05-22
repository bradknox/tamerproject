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
package edu.utexas.cs.tamerProject.environments.loopmaze;

import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Random;
import java.util.Arrays;

import edu.utexas.cs.tamerProject.environments.loopmaze.messages.StateResponse;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

import edu.utexas.cs.tamerProject.environments.loopmaze.visualizer.LoopMazeVisualizer;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;

import rlVizLib.Environments.EnvironmentBase;
import rlVizLib.Environments.SampleableEnvBase;
import rlVizLib.general.ParameterHolder;
import rlVizLib.general.hasVersionDetails;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvironmentMessageParser;
import rlVizLib.messaging.environment.EnvironmentMessages;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;
import rlVizLib.messaging.interfaces.HasAVisualizerInterface;
import rlVizLib.messaging.interfaces.HasImageInterface;
import rlVizLib.messaging.interfaces.getEnvMaxMinsInterface;
import rlVizLib.messaging.interfaces.getEnvObsForStateInterface;

/*
 * October 2009
 * This is the Java Version of the Puddle World Domain from the RL-Library.
 *
 * Brian Tanner ported it from the old RL-Library Environment Shelf to Java.
 * I found it here: http://rlai.cs.ualberta.ca/RLR/environment.html
 *
 * I have taken some liberties in this port.  This version will have the exact
 * same noise model if the transition noise parameter is set to 0.2 (the default).
 *
 * I'm not sure of the exact penalty model from the original paper (it's a bit unclear),
 * so I'm using the same as the software implementation I'm porting from.  Negative rewards
 * stack, so being in both puddles is worse than being in only one.
 *
 * This version has a rectangular goal region which by default is of size .01
 *
 * If random start states are turned on, the agent will start anywhere uniformly
 * that is not in the goal.
 * 
 */

public class LoopMaze extends SampleableEnvBase implements
        getEnvMaxMinsInterface,
        getEnvObsForStateInterface,
        HasAVisualizerInterface,
        HasImageInterface {

    static final int numActions = 4;
    protected final LoopMazeState theState;
    private Random randomGenerator = new Random();

    
    /**
     * Return the ParameterHolder object that contains the default parameters for
     * loop maze.
     * @return
     */
    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        rlVizLib.utilities.UtilityShop.setVersionDetails(p, new DetailsProvider());

        p.addIntegerParam("RandomSeed(0 means random)", 0);
        p.addBooleanParam("RandomStartStates", false);
        p.addDoubleParam("TransitionNoise[0,1]", 0.0d);
        p.setAlias("noise", "TransitionNoise[0,1]");
        p.setAlias("seed", "RandomSeed(0 means random)");
        return p;
    }
    
    public static TaskSpecPayload getTaskSpecPayload(ParameterHolder P) {
        LoopMaze theMC = new LoopMaze(P);
        String taskSpecString = theMC.makeTaskSpec().getStringRepresentation();
        return new TaskSpecPayload(taskSpecString, false, "");
    }

    public TaskSpec makeTaskSpec() {
        TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
        theTaskSpecObject.setEpisodic();
        theTaskSpecObject.setDiscountFactor(1.0d);
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, theState.worldDims[0] - 1));
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, theState.worldDims[1] - 1));
        //theTaskSpecObject.addContinuousObservation(new DoubleRange(0, theState.worldDims[0]));
        //theTaskSpecObject.addContinuousObservation(new DoubleRange(0, theState.worldDims[1]));

        theTaskSpecObject.addDiscreteAction(new IntRange(0, 3));
        theTaskSpecObject.setRewardRange(new DoubleRange(-1, 0));
        theTaskSpecObject.setExtra("EnvName:Loop-Maze Revision:" + this.getClass().getPackage().getImplementationVersion());

        String taskSpecString = theTaskSpecObject.toTaskSpec();
        TaskSpec.checkTaskSpec(taskSpecString);

        return new TaskSpec(theTaskSpecObject);

    }

    public LoopMazeState getState() {
        return theState;
    }

    public String env_init() {
        return makeTaskSpec().getStringRepresentation();
    }

    /**
     * Restart the car on the mountain.  Pick a random position and velocity if
     * randomStarts is set.
     * @return
     */
    public Observation env_start() {
    	System.out.println("*********Env start called******** " + String.format("%f", System.currentTimeMillis() / 1000.0)); 
    	System.out.flush();
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

        if (a > 3 || a < 0) {
            System.err.println("Invalid action selected in Loop Maze: " + a);
            a = randomGenerator.nextInt(4);
        }

        theState.update(a);

        if (theState.inGoalRegion() || theState.inFailRegion()) {
        	System.out.println("*****Env returning terminal******* " + String.format("%f", System.currentTimeMillis() / 1000.0));
        	System.out.flush();
        }

        return makeRewardObservation(theState.getReward(), (theState.inGoalRegion() || theState.inFailRegion()));
    }


    /**
     * Create a new mountain car environment using parameter settings in p.
     * @param p
     */
    public LoopMaze(ParameterHolder p) {
    	this(p, false);
    }
    
    public LoopMaze(ParameterHolder p, boolean allowSecretPaths) {
        super();
        boolean randomStartStates = false;
        double transitionNoise = 0.0d;
        long randomSeed = 0L;

        if (p != null) {
            if (!p.isNull()) {
                randomStartStates = p.getBooleanParam("RandomStartStates");
                transitionNoise = p.getDoubleParam("noise");
                randomSeed = p.getIntegerParam("seed");
            }
        }
        theState = new LoopMazeState(randomStartStates, transitionNoise, randomSeed, allowSecretPaths);

    }

    public LoopMaze() {
        this(getDefaultParameters());
        System.out.println("Completed constructor.");
    }


	private void processArgs(String[] args) {
		System.out.println("\n[------LoopMaze command line args------] " + Arrays.toString(args));
		for (int i = 0; i < args.length; i++) {
			String argType = args[i];
			if (argType.equals("-stateMapPath") && (i+1) < args.length){
				String filePath = args[i+1];
				System.out.println("Getting state map from: " + filePath);
				this.theState.loadMapFromFile(filePath);
			}
		}
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
            System.err.println("Someone sent Loop Maze a message that wasn't RL-Viz compatible");
            return "I only respond to RL-Viz messages!";
        }

        if (theMessageObject.canHandleAutomatically(this)) {
            String theResponseString = theMessageObject.handleAutomatically(this);
            return theResponseString;
        }

        //If it wasn't handled automatically, maybe its a custom Mountain Car Message
        if (theMessageObject.getTheMessageType() == rlVizLib.messaging.environment.EnvMessageType.kEnvCustom.id()) {

            String theCustomType = theMessageObject.getPayLoad();

            if (theCustomType.equals("GETPWSTATE")) {
                //It is a request for the state

                int[] agentPosition=LoopMazeState.getDefaultInitPosition();
                if(theState!=null){
                    agentPosition=theState.getPosition();
                }
                StateResponse theResponseObject = new StateResponse(agentPosition);
                return theResponseObject.makeStringResponse();
            }

        }
        System.err.println("We need some code written in Env Message for LoopMaze.. unknown request received: " + theMessage);
        Thread.dumpStack();
        return null;
    }

    public static void main(String[] args) {
        System.out.println("in main...");
		LoopMaze world = new LoopMaze();
		world.processArgs(args);
		EnvironmentLoader L = new EnvironmentLoader(world);
        System.out.println("loaded...");
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
     * The value function will be drawn over the position and velocity.  This 
     * method provides the max values for those variables.
     * @param dimension
     * @return
     */
    public double getMaxValueForQuerableVariable(int dimension) {
        if (dimension == 0) {
            return theState.worldDims[0];
        } else {
            return theState.worldDims[1];
        }
    }

    /**
     * The value function will be drawn over the position and velocity.  This 
     * method provides the min values for those variables.
     * @param dimension
     * @return
     */
    public double getMinValueForQuerableVariable(int dimension) {
        if (dimension == 0) {
            return 0;
        } else {
            return 0;
        }
    }

    /**
     * Given a state, return an observation.  This is trivial in mountain car
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
        return 2;
    }

    public String getVisualizerClassName() {
        return LoopMazeVisualizer.class.getName();
    }

    /**
     * So we can draw a pretty image in the visualizer before we start
     * @return
     */
    public URL getImageURL() {
        URL imageURL = LoopMaze.class.getResource("/images/puddleworld.png");
        return imageURL;
    }

    
    public Reward_observation_terminal sampleStartObs() {
    	return sampleStartObs(false);
    }
    
	public Reward_observation_terminal sampleStartObs(boolean envAsking) {
		Reward_observation_terminal rot=new Reward_observation_terminal();
		rot.o = LoopMazeState.sampleEnvStart(envAsking);
		rot.r = 0;
		rot.terminal = 0;
		return rot;
	}

	@Override
	public Reward_observation_terminal sampleNextObs(Observation obs,
			Action act) {
		Reward_observation_terminal rot=new Reward_observation_terminal();
		rot.o = LoopMazeState.sampleNextObs(obs, act);
		rot.terminal = 0;

		/*
		 * Get reward
		 */
		rot.r = LoopMazeState.getReward(rot.o.intArray);
		
		
		/*
         * Check for terminal state
         */
        if (LoopMazeState.inGoalRegion(rot.o.intArray) || LoopMazeState.inFailRegion(rot.o.intArray))
        	rot.terminal = 1;
        
        
		return rot;
	}

	@Override
	public boolean isObsLegal(Observation obs) {
		return LoopMazeState.isStateLegal(obs.intArray[0], obs.intArray[1]);
	}

	@Override
	public boolean isObsTerminal(Observation obs) {
		return (LoopMazeState.inGoalRegion(obs.intArray) || LoopMazeState.inFailRegion(obs.intArray));
	}
}

/**
 * This is a little helper class that fills in the details about this environment
 * for the fancy print outs in the visualizer application.
 * @author btanner
 */
class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "Loop Maze 0.1";
    }

    public String getShortName() {
        return "Loop-Maze";
    }

    public String getAuthors() {
        return "Brad Knox. Adapted from Puddle World by Richard Sutton, Adam White, Brian Tanner";
    }

    public String getInfoUrl() {
        return "http://library.rl-community.org/wiki/Grid_World_(Java)";
    }

    public String getDescription() {
        return "RL-Library Java Version of the classic Loop Maze RL-Problem.";
    }
}

