/* Helicopter Domain  for RL - Competition - RLAI's Port of Pieter Abbeel's code submission
 * Copyright (C) 2007, Pieter Abbeel
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */
package org.rlcommunity.environments.helicopter;

import java.net.URL;
import org.rlcommunity.environments.helicopter.messages.HelicopterRangeResponse;
import org.rlcommunity.environments.helicopter.visualizer.HelicopterVisualizer;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
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


public class Helicopter extends EnvironmentBase implements  HasAVisualizerInterface, HasImageInterface {

    Observation o;
    HelicopterState heli = new HelicopterState();
    Reward_observation_terminal ro;

    public Helicopter() {
        this(getDefaultParameters());
    }

    public Helicopter(ParameterHolder p) {
        if (p != null) {
            if (!p.isNull()) {
                setWind0(p.getDoubleParam("wind0"));
                setWind1(p.getDoubleParam("wind1"));
            }
        }
    }

    public static TaskSpecPayload getTaskSpecPayload(ParameterHolder P) {
        Helicopter theWorld = new Helicopter(P);
        String taskSpec = theWorld.makeTaskSpec();
        return new TaskSpecPayload(taskSpec, false, "");
    }

    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        rlVizLib.utilities.UtilityShop.setVersionDetails(p, new DetailsProvider());

        p.addDoubleParam("Wind 0 [-1.0d,1.0d]", 0.0d);
        p.setAlias("wind0", "Wind 0 [-1.0d,1.0d]");
        p.addDoubleParam("Wind 1 [-1.0d,1.0d]", 0.0d);
        p.setAlias("wind1", "Wind 1 [-1.0d,1.0d]");
        return p;
    }

    public void setWind0(double newValue) {
        heli.wind[0] = newValue * HelicopterState.WIND_MAX;
    }

    public void setWind1(double newValue) {
        heli.wind[1] = newValue * HelicopterState.WIND_MAX;
    }

    public String env_init() {
        o = new Observation(0, 12);


        return makeTaskSpec();
    }

    public Observation env_start() {
        // start at origin, zero velocity, zero angular rate, perfectly level and facing north
        heli.reset();
        return makeObservation();
    }

    public Reward_observation_terminal env_step(Action action) {
        heli.stateUpdate(action);
        heli.num_sim_steps++;
        heli.env_terminal = heli.env_terminal || (heli.num_sim_steps == HelicopterState.NUM_SIM_STEPS_PER_EPISODE);

        int isTerminal = 0;
        if (heli.env_terminal) {
            isTerminal = 1;
        }

        ro = new Reward_observation_terminal(getReward(), makeObservation(), isTerminal);
        return ro;
    }

    public void env_cleanup() {
    }

    public String env_message(String theMessage) {
        EnvironmentMessages theMessageObject;
        try {
            theMessageObject = EnvironmentMessageParser.parseMessage(theMessage);
        } catch (NotAnRLVizMessageException e) {
            System.err.println("Someone sent Helicopter a message that wasn't RL-Viz compatible");
            return "I only respond to RL-Viz messages!";
        }

        if (theMessageObject.canHandleAutomatically(this)) {
            return theMessageObject.handleAutomatically(this);
        }

        //		If it wasn't handled automatically, maybe its a custom Helicopter Message
        if (theMessageObject.getTheMessageType() == rlVizLib.messaging.environment.EnvMessageType.kEnvCustom.id()) {

            String theCustomType = theMessageObject.getPayLoad();

            if (theCustomType.equals("GETHELIRANGE")) {
                HelicopterRangeResponse theResponseObject = new HelicopterRangeResponse(heli.mins, heli.maxs);
                return theResponseObject.makeStringResponse();
            }

        }
        System.err.println("We need some code written in Env Message for Helicopter. unknown request received: " + theMessage);
        Thread.dumpStack();
        return null;
    }

    // goal state is all zeros, quadratically penalize for deviation:
    double getReward() {
        double reward = 0;
        if (!heli.env_terminal) { // not in terminal state
            reward -= heli.velocity.x * heli.velocity.x;
            reward -= heli.velocity.y * heli.velocity.y;
            reward -= heli.velocity.z * heli.velocity.z;
            reward -= heli.position.x * heli.position.x;
            reward -= heli.position.y * heli.position.y;
            reward -= heli.position.z * heli.position.z;
            reward -= heli.angular_rate.x * heli.angular_rate.x;
            reward -= heli.angular_rate.y * heli.angular_rate.y;
            reward -= heli.angular_rate.z * heli.angular_rate.z;
            reward -= heli.q.x * heli.q.x;
            reward -= heli.q.y * heli.q.y;
            reward -= heli.q.z * heli.q.z;
        } else { // in terminal state, obtain very negative reward b/c the agent will exit, we have to give out reward for all future times
            reward = -3.0f * HelicopterState.MAX_POS * HelicopterState.MAX_POS +
                    -3.0f * HelicopterState.MAX_RATE * HelicopterState.MAX_RATE +
                    -3.0f * HelicopterState.MAX_VEL * HelicopterState.MAX_VEL -
                    (1.0f - HelicopterState.MIN_QW_BEFORE_HITTING_TERMINAL_STATE * HelicopterState.MIN_QW_BEFORE_HITTING_TERMINAL_STATE);
            reward *= (float) (HelicopterState.NUM_SIM_STEPS_PER_EPISODE - heli.num_sim_steps);

        //System.out.println("Final reward is: "+reward+" NUM_SIM_STEPS_PER_EPISODE="+HelicopterState.NUM_SIM_STEPS_PER_EPISODE +"  heli.num_sim_steps="+ heli.num_sim_steps);
        }
        return reward;

    }

    //This method creates the object that can be used to easily set different problem parameters
    @Override
    protected Observation makeObservation() {
        return heli.makeObservation();
    }

    public double getMaxValueForQuerableVariable(int dimension) {
        switch (dimension) {
            case 0:
                return HelicopterState.MAX_VEL;
            case 1:
                return HelicopterState.MAX_VEL;
            case 2:
                return HelicopterState.MAX_VEL;
            case 3:
                return HelicopterState.MAX_POS;
            case 4:
                return HelicopterState.MAX_POS;
            case 5:
                return HelicopterState.MAX_POS;
            case 6:
                return HelicopterState.MAX_RATE;
            case 7:
                return HelicopterState.MAX_RATE;
            case 8:
                return HelicopterState.MAX_RATE;
            case 9:
                return HelicopterState.MAX_QUAT;
            case 10:
                return HelicopterState.MAX_QUAT;
            case 11:
                return HelicopterState.MAX_QUAT;
            case 12:
                return HelicopterState.MAX_QUAT;
            default:
                System.out.println("Invalid Dimension in getMaxValueForQuerableVariable for Helicopter");
                return Double.POSITIVE_INFINITY;
        }
    }

    public double getMinValueForQuerableVariable(int dimension) {
        switch (dimension) {
            case 0:
                return -HelicopterState.MAX_VEL;
            case 1:
                return -HelicopterState.MAX_VEL;
            case 2:
                return -HelicopterState.MAX_VEL;
            case 3:
                return -HelicopterState.MAX_POS;
            case 4:
                return -HelicopterState.MAX_POS;
            case 5:
                return -HelicopterState.MAX_POS;
            case 6:
                return -HelicopterState.MAX_RATE;
            case 7:
                return -HelicopterState.MAX_RATE;
            case 8:
                return -HelicopterState.MAX_RATE;
            case 9:
                return -HelicopterState.MAX_QUAT;
            case 10:
                return -HelicopterState.MAX_QUAT;
            case 11:
                return -HelicopterState.MAX_QUAT;
            case 12:
                return -HelicopterState.MAX_QUAT;
            default:
                System.out.println("Invalid Dimension in getMaxValueForQuerableVariable for Helicopter");
                return Double.NEGATIVE_INFINITY;
        }
    }

    public String getVisualizerClassName() {
        return HelicopterVisualizer.class.getName();
    }
    public URL getImageURL() {
       URL imageURL = Helicopter.class.getResource("/images/helicopter.png");
       return imageURL;
   }      

    private String makeTaskSpec() {

        TaskSpecVRLGLUE3 taskSpecObject = new TaskSpecVRLGLUE3();
        taskSpecObject.setEpisodic();
        taskSpecObject.setDiscountFactor(1.0d);
//The 3 velocities: forward, sideways, up
//# forward velocity
//# sideways velocity (to the right)
//# downward velocity
        taskSpecObject.addContinuousObservation(new DoubleRange(-HelicopterState.MAX_VEL, HelicopterState.MAX_VEL, 3));
//# helicopter x-coord position - desired x-coord position -- helicopter's x-axis points forward
//# helicopter y-coord position - desired y-coord position -- helicopter's y-axis points to the right
//# helicopter z-coord position - desired z-coord position -- helicopter's z-axis points down
        taskSpecObject.addContinuousObservation(new DoubleRange(-HelicopterState.MAX_POS, HelicopterState.MAX_POS, 3));
//# angular rate around helicopter's x axis
//# angular rate around helicopter's y axis
//# angular rate around helicopter's z axis
        taskSpecObject.addContinuousObservation(new DoubleRange(-HelicopterState.MAX_RATE, HelicopterState.MAX_RATE, 3));
//quaternion x,y,z entries
        taskSpecObject.addContinuousObservation(new DoubleRange(-HelicopterState.MAX_QUAT, HelicopterState.MAX_QUAT, 3));

        taskSpecObject.addContinuousAction(new DoubleRange(-HelicopterState.MAX_ACTION, HelicopterState.MAX_ACTION, 4));
//Apparently we're not specifying the rewards
        DoubleRange theRewardRange = new DoubleRange(0, 0);
        theRewardRange.setMinUnspecified();
        taskSpecObject.setRewardRange(theRewardRange);
        String newTaskSpecString = taskSpecObject.toTaskSpec();
        TaskSpec.checkTaskSpec(newTaskSpecString);


        return newTaskSpecString;
    }

    public static void main(String[] args){
        EnvironmentLoader L=new EnvironmentLoader(new Helicopter());
        L.run();
    }
}

class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "Helicopter Hovering 1.0";
    }

    public String getShortName() {
        return "Helicopter";
    }

    public String getAuthors() {
        return "Pieter Abbeel, Mark Lee, Brian Tanner";
    }

    public String getInfoUrl() {
        return "http://library.rl-community.org/helicopter";
    }

    public String getDescription() {
        return "Helicopter Hovering Reinforcement Learning problem.";
    }
}
