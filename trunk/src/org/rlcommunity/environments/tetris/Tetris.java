/*
Copyright 2007 Brian Tanner
http://rl-library.googlecode.com/
brian@tannerpages.com
http://brian.tannerpages.com

Modified by Brad Knox

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
package org.rlcommunity.environments.tetris;

import java.net.URL;
import java.util.Vector;

import org.rlcommunity.environments.tetris.messages.TetrisStateResponse;
import org.rlcommunity.environments.tetris.visualizer.TetrisVisualizer;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;

import rlVizLib.Environments.EnvironmentBase;
import rlVizLib.general.ParameterHolder;
import rlVizLib.general.hasVersionDetails;
import rlVizLib.messaging.environment.EnvironmentMessageParser;
import rlVizLib.messaging.environment.EnvironmentMessages;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;
import rlVizLib.messaging.interfaces.HasAVisualizerInterface;
import rlVizLib.messaging.interfaces.HasImageInterface;

public class Tetris extends EnvironmentBase implements HasAVisualizerInterface, HasImageInterface {

    private int currentScore = 0;
    protected TetrisState gameState = null;
    static final int terminalScore = 0;

    public Tetris() {
        this(getDefaultParameters());
    }

    public Tetris(ParameterHolder p) {
        super();
        gameState = new TetrisState();
    }

    /**
     * Tetris doesn't really have any parameters
     * @return
     */
    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        rlVizLib.utilities.UtilityShop.setVersionDetails(p, new DetailsProvider());
        return p;
    }

    public static TaskSpecPayload getTaskSpecPayload(ParameterHolder P) {
        Tetris theWorld = new Tetris(P);
        String taskSpec = theWorld.makeTaskSpec();
        return new TaskSpecPayload(taskSpec, false, "");
    }

    /*Base RL-Glue Functions*/
    public String env_init() {
        return makeTaskSpec();
    }

    public Observation env_start() {
        gameState.reset();
        gameState.spawn_block();
        gameState.blockMobile = true;
        currentScore = 0;

        Observation o = gameState.get_observation();
        return o;
    }

    public Reward_observation_terminal env_step(Action actionObject) {
        int theAction = 0;
        try {
            theAction = actionObject.intArray[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error: Action was expected to have 1 dimension but got ArrayIndexOutOfBoundsException when trying to get element 0:" + e);
	    //System.err.println("Error: Choosing action 0");                                                                                                                
            System.err.println("\nExiting Tetris.");
            System.exit(0);
            theAction = 0;
        }

        if (theAction > 5 || theAction < 0) {
            System.err.println("Invalid action selected in Tetrlais: " + theAction);
            theAction = gameState.getRandom().nextInt(5);
        }

        if (gameState.blockMobile) {
            gameState.take_action(theAction);
            gameState.update();
        } else {
            gameState.spawn_block();
        }

        Reward_observation_terminal ro = new Reward_observation_terminal();

        ro.terminal = 1;
        ro.o = gameState.get_observation();

        if (!gameState.gameOver()) {
            ro.terminal = 0;
            ro.r = gameState.get_score() - currentScore;
            currentScore = gameState.get_score();
        } else {
            ro.r = Tetris.terminalScore;
            currentScore = 0;
        }

        return ro;
    }

    public void env_cleanup() {
    }

    public String env_message(String theMessage) {
        EnvironmentMessages theMessageObject;
        try {
            theMessageObject = EnvironmentMessageParser.parseMessage(theMessage);
        } catch (Exception e) {
            System.err.println("Someone sent Tetris a message that wasn't RL-Viz compatible");
            return "I only respond to RL-Viz messages!";
        }


        if (theMessageObject.canHandleAutomatically(this)) {
            return theMessageObject.handleAutomatically(this);
        }

        if (theMessageObject.getTheMessageType() == rlVizLib.messaging.environment.EnvMessageType.kEnvCustom.id()) {

            String theCustomType = theMessageObject.getPayLoad();

            if (theCustomType.equals("GETTETRLAISSTATE")) {
                //It is a request for the state
                TetrisStateResponse theResponseObject = new TetrisStateResponse(currentScore, gameState.getWidth(), gameState.getHeight(), gameState.getNumberedStateSnapShot(), gameState.getCurrentPiece());
    		/*if (gameState.getPreviousBlock() == null) {
        		System.out.println("previousBlock is null in Tetris.java");
		}*/
                theResponseObject.setPreviousBlock(gameState.getPreviousBlock());
                // theResponseObject.setSecToLastBlock(gameState.getSecToLastBlock());
                /*if (theResponseObject.getSecToLastBlock() != null) {
        		System.out.println("secToLastBlock is instantiated in TetrisStateResponse.get() from Tetris.java");
		}*/

                return theResponseObject.makeStringResponse();
            }
            System.out.println("We need some code written in Env Message for Tetrlais.. unknown custom message type received");
            Thread.dumpStack();

            return null;
        }

        System.out.println("We need some code written in Env Message for  Tetrlais!");
        Thread.dumpStack();

        return null;
    }

    /*End of Base RL-Glue Functions */
    /*RL-Viz Methods*/
    @Override
    protected Observation makeObservation() {
        return gameState.get_observation();
    }

    public String getVisualizerClassName() {
        return TetrisVisualizer.class.getName();
    }

    public URL getImageURL() {
        URL imageURL = Tetris.class.getResource("/images/tetris.png");
        return imageURL;
    }

    private String makeTaskSpec() {
        int boardSize = gameState.getHeight() * gameState.getWidth();
        int numPieces = gameState.possibleBlocks.size();

        TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
        theTaskSpecObject.setEpisodic();
        theTaskSpecObject.setDiscountFactor(1.0d);
        // First add the binary variables for the board and the 7 other variables that dictate state
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, 1, boardSize));
		// blockMobile
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, 1));
		// currentBlockID
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, numPieces));
		// currentRotation
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, 4));
		// currentX
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, gameState.getWidth() - 1));
		// currentY
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, gameState.getHeight() - 1));
        //Now the actual board size in the observation. The reason this was here is/was because
        //there was no way to add meta-data to the task spec before.
        //First height
        theTaskSpecObject.addDiscreteObservation(new IntRange(gameState.getHeight(), gameState.getHeight()));
        //Then width
        theTaskSpecObject.addDiscreteObservation(new IntRange(gameState.getWidth(), gameState.getWidth()));

        theTaskSpecObject.addDiscreteAction(new IntRange(0, 4)); // FALL action has been removed
        //This is actually a lie... the rewards aren't in that range.
        theTaskSpecObject.setRewardRange(new DoubleRange(0, 8.0d));

        //This is a better way to tell the rows and cols
        theTaskSpecObject.setExtra("EnvName:Tetris HEIGHT:" + gameState.getHeight() + " WIDTH:" + gameState.getWidth() + " Revision: " + this.getClass().getPackage().getImplementationVersion());

        String taskSpecString = theTaskSpecObject.toTaskSpec();

        TaskSpec.checkTaskSpec(taskSpecString);
        return taskSpecString;
    }

    public static void main(String[] args) {
        EnvironmentLoader L = new EnvironmentLoader(new Tetris());
        L.run();
    }
}

class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "Tetris 1.1";
    }

    public String getShortName() {
        return "Tetris";
    }

    public String getAuthors() {
        return "Brian Tanner, Leah Hackman, Matt Radkie, Andrew Butcher";
    }

    public String getInfoUrl() {
        return "http://library.rl-community.org/tetris";
    }

    public String getDescription() {
        return "Tetris problem from the reinforcement learning library.";
    }
}
