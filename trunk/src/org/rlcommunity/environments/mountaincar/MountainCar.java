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
package org.rlcommunity.environments.mountaincar;

import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import org.rlcommunity.environments.mountaincar.messages.MCGoalResponse;
import org.rlcommunity.environments.mountaincar.messages.MCHeightResponse;
import org.rlcommunity.environments.mountaincar.messages.MCStateResponse;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

import java.util.Random;
import org.rlcommunity.environments.mountaincar.visualizer.MountainCarVisualizer;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;

import rlVizLib.Environments.EnvironmentBase;
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
 * July 2007
 * This is the Java Version MountainCar Domain from the RL-Library.  
 * Brian Tanner ported it from the Existing RL-Library to Java.
 * I found it here: http://rlai.cs.ualberta.ca/RLR/environment.html
 * 
 * 
 * This is quite an advanced environment in that it has some fancy visualization
 * capabilities which have polluted the code a little.  What I'm saying is that 
 * this is not the easiest environment to get started with.
 */

public class MountainCar extends EnvironmentBase implements
        getEnvMaxMinsInterface,
        getEnvObsForStateInterface,
        HasAVisualizerInterface,
        HasImageInterface {

    static final int numActions = 3;
    protected MountainCarState theState = null;    //Used for env_save_state and env_save_state, which don't exist anymore, but we will one day bring them back
    //through the messaging system and RL-Viz.
    protected Vector<MountainCarState> savedStates = null;
    //Problem parameters have been moved to MountainCar State
    private Random randomGenerator = new Random();
    private static final long randomSeed = 140823235235345435L;
    private Random seededRdmGen = new Random(randomSeed);
    private int epCounter = 0;
    private static final int epsUntilRepeat = 4000;
    private static final boolean repeatInitState = false;

	private int maxSteps = 500;
    private int completedStepsThisEp;

    public static TaskSpecPayload getTaskSpecPayload(ParameterHolder P) {
        MountainCar theMC = new MountainCar(P);
        String taskSpec = theMC.makeTaskSpec();
        return new TaskSpecPayload(taskSpec, false, "");
    }

    private String makeTaskSpec() {
        TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
        theTaskSpecObject.setEpisodic();
        theTaskSpecObject.setDiscountFactor(1.0d);
        theTaskSpecObject.addContinuousObservation(new DoubleRange(theState.minPosition, theState.maxPosition));
        theTaskSpecObject.addContinuousObservation(new DoubleRange(theState.minVelocity, theState.maxVelocity));
        theTaskSpecObject.addDiscreteAction(new IntRange(0, 2));
        theTaskSpecObject.setRewardRange(new DoubleRange(-1, 0));
        theTaskSpecObject.setExtra("EnvName:Mountain-Car Revision:" + this.getClass().getPackage().getImplementationVersion());

        String taskSpecString = theTaskSpecObject.toTaskSpec();
        TaskSpec.checkTaskSpec(taskSpecString);

        return taskSpecString;

    }

    public String env_init() {
        savedStates = new Vector<MountainCarState>();
        return makeTaskSpec();

    }

    /**
     * Restart the car on the mountain.  Pick a random position and velocity if
     * randomStarts is set.
     * @return
     */

    public Observation env_start() {
		
        if (theState.randomStarts) {
			do {
				double randStartPosition;
				if (repeatInitState) {
					if (epCounter % epsUntilRepeat == 0) {
						seededRdmGen = new Random(randomSeed);
					}
					randStartPosition = (seededRdmGen.nextDouble() * (theState.startRegionRtBorder + Math.abs((theState.startRegionLftBorder)))
										 - Math.abs(theState.startRegionLftBorder));
				}
				else {
					randStartPosition = (randomGenerator.nextDouble() * (theState.startRegionRtBorder + Math.abs((theState.startRegionLftBorder)))
										 - Math.abs(theState.startRegionLftBorder));
				}
				theState.position = randStartPosition;
			} while (theState.inGoalRegion()); 
		}    
		else {
			theState.position = theState.defaultInitPosition;
        }
		//System.out.println("Initial Position: " + theState.position + ". Episode Counter: " + epCounter + ".");
        theState.velocity = theState.defaultInitVelocity;
		epCounter++; // Therefore the first episode is counted as number 0 (not 1).
		this.completedStepsThisEp = 1;
        return makeObservation();
    }
	
    /** public Observation env_start() {
        if (theState.randomStarts) {
            double randStartPosition = (randomGenerator.nextDouble() * (theState.maxPosition + Math.abs((theState.minPosition))) - Math.abs(theState.minPosition));
            theState.position = theState.minVelocity;
        } else {
            theState.position = theState.defaultInitPosition;
        }
        theState.velocity = theState.defaultInitVelocity;

        return makeObservation();
	}**/

    /**
     * Takes a step.  If an invalid action is selected, choose a random action.
     * @param theAction
     * @return
     */
    public Reward_observation_terminal env_step(Action theAction) {

        int a = theAction.intArray[0];
        if (a > 2 || a < 0) {
            a = randomGenerator.nextInt(3);
        }
        theState.update(a);
		this.completedStepsThisEp += 1;
		boolean terminal = theState.inGoalRegion() || (this.completedStepsThisEp > this.maxSteps);

        return makeRewardObservation(theState.getReward(), terminal);
    }

    /**
     * Return the ParameterHolder object that contains the default parameters for
     * mountain car.  The only parameter is random start states.
     * @return
     */
    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        rlVizLib.utilities.UtilityShop.setVersionDetails(p, new DetailsProvider());

        p.addBooleanParam("randomStartStates", true);
        return p;
    }

    /**
     * Create a new mountain car environment using parameter settings in p.
     * @param p
     */
    public MountainCar(ParameterHolder p) {
        super();
        theState = new MountainCarState(randomGenerator);
        if (p != null) {
            if (!p.isNull()) {
                theState.randomStarts = p.getBooleanParam("randomStartStates");
            }
        }
    }

    public MountainCar() {
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
            System.err.println("Someone sent mountain Car a message that wasn't RL-Viz compatible");
            return "I only respond to RL-Viz messages!";
        }

        if (theMessageObject.canHandleAutomatically(this)) {
            String theResponseString = theMessageObject.handleAutomatically(this);
            return theResponseString;
        }

        //If it wasn't handled automatically, maybe its a custom Mountain Car Message
        if (theMessageObject.getTheMessageType() == rlVizLib.messaging.environment.EnvMessageType.kEnvCustom.id()) {

            String theCustomType = theMessageObject.getPayLoad();

            if (theCustomType.equals("GETMCSTATE")) {
                //It is a request for the state
                double position = theState.position;
                double velocity = theState.velocity;
                double height = this.getHeight();
                double deltaheight = theState.getHeightAtPosition(position + .05);
                MCStateResponse theResponseObject = new MCStateResponse(position, velocity, height, deltaheight);
                return theResponseObject.makeStringResponse();
            }

            if (theCustomType.startsWith("GETHEIGHTS")) {
                Vector<Double> theHeights = new Vector<Double>();

                StringTokenizer theTokenizer = new StringTokenizer(theCustomType, ":");
                //throw away the first token
                theTokenizer.nextToken();

                int numQueries = Integer.parseInt(theTokenizer.nextToken());
                for (int i = 0; i < numQueries; i++) {
                    double thisPoint = Double.parseDouble(theTokenizer.nextToken());
                    theHeights.add(theState.getHeightAtPosition(thisPoint));
                }

                MCHeightResponse theResponseObject = new MCHeightResponse(theHeights);
                return theResponseObject.makeStringResponse();
            }

            if (theCustomType.startsWith("GETMCGOAL")) {
                MCGoalResponse theResponseObject = new MCGoalResponse(theState.goalPosition);
                return theResponseObject.makeStringResponse();
            }

        }
        System.err.println("We need some code written in Env Message for MountainCar.. unknown request received: " + theMessage);
        Thread.dumpStack();
        return null;
    }

    public static void main(String[] args) {
        EnvironmentLoader L = new EnvironmentLoader(new MountainCar());
        L.run();
    }

    /**
     * Turns theState object into an observation.
     * @return
     */
    @Override
    protected Observation makeObservation() {
        Observation currentObs = new Observation(0, 2);

        currentObs.doubleArray[0] = theState.position;
        currentObs.doubleArray[1] = theState.velocity;

        return currentObs;
    }

    public void env_cleanup() {
        if (savedStates != null) {
            savedStates.clear();
        }
    }

    /**
     * The value function will be drawn over the position and velocity.  This 
     * method provides the max values for those variables.
     * @param dimension
     * @return
     */
    public double getMaxValueForQuerableVariable(int dimension) {
        if (dimension == 0) {
            return theState.maxPosition;
        } else {
            return theState.maxVelocity;
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
            return theState.minPosition;
        } else {
            return theState.minVelocity;
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

    /**
     * Used by MCHeightRequest Message
     * @return
     */
    private double getHeight() {
        return theState.getHeightAtPosition(theState.position);
    }

    public String getVisualizerClassName() {
        return MountainCarVisualizer.class.getName();
    }

    private Random getRandomGenerator() {
        return randomGenerator;
    }

    /**
     * So we can draw a pretty image in the visualizer before we start
     * @return
     */
    public URL getImageURL() {
        URL imageURL = MountainCar.class.getResource("/images/mountaincar.png");
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
        return "Mountain Car 1.30";
    }

    public String getShortName() {
        return "Mount-Car";
    }

    public String getAuthors() {
        return "Richard Sutton, Adam White, Brian Tanner";
    }

    public String getInfoUrl() {
        return "http://library.rl-community.org/environments/mountaincar";
    }

    public String getDescription() {
        return "RL-Library Java Version of the classic Mountain Car RL-Problem.";
    }
}

