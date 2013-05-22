/*
 * (c) 2009 Marc G. Bellemare.
 */

package org.rlcommunity.environments.continuousgridworld;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
import org.rlcommunity.rlviz.dynamicloading.Unloadable;
import rlVizLib.general.ParameterHolder;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;

/** An extension of the Continuous grid world that includes a specified goal
 *   and the possibility to provide distance-based potential function reward
 *   to the agent.
 *
 * @author Marc G. Bellemare (mg17 at cs ualberta ca)
 */
public class PotentialFuncContinuousGridWorld extends ContinuousGridWorld implements Unloadable {
    public static final int MAP_EMPTY = 0;
    public static final int MAP_CUP   = 1;

    public static final int numActions = 4;
    
    protected boolean usePotentialFunction;
    protected int mapNumber;

    protected final Random randomMaker = new Random();

    protected Point2D lastAgentPos;
    protected Point2D goalPos;
    protected Point2D startPos;
    
    protected double shapingRewardScale;
    protected double randomActionProbability;
    protected double movementNoise;

    protected double discountFactor = 1.0;
    
    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = ContinuousGridWorld.getDefaultParameters();

        // Default goal (centered at 87.5)
        p.addDoubleParam("cont-grid-world-goalX", 87.5);
        p.addDoubleParam("cont-grid-world-goalY", 87.5);
        p.addDoubleParam("cont-grid-world-startX", 0.1);
        p.addDoubleParam("cont-grid-world-startY", 0.1);
        p.addDoubleParam("potential-function-scale", 1.0);
        p.addBooleanParam("give-potential-function-reward", false);
        p.addIntegerParam("map-number", MAP_EMPTY);
        p.addDoubleParam("random-action-prob", 0.0);
        p.addDoubleParam("movement-noise", 0.0);
        
        return p;
    }

    public PotentialFuncContinuousGridWorld() {
        this(getDefaultParameters());
    }

    public PotentialFuncContinuousGridWorld(ParameterHolder theParams) {
        super(theParams);
    }

    @Override
    public void addBarriersAndGoal(ParameterHolder theParams) {
        addRewardRegion(new Rectangle2D.Double(0.0d, 0.0d, 200.0d, 200.0d), -1.0d);
        double width = theParams.getDoubleParam("cont-grid-world-width");
        double height = theParams.getDoubleParam("cont-grid-world-height");
        double goalX = theParams.getDoubleParam("cont-grid-world-goalX");
        double goalY = theParams.getDoubleParam("cont-grid-world-goalY");
        double startX = theParams.getDoubleParam("cont-grid-world-startX");
        double startY = theParams.getDoubleParam("cont-grid-world-startY");

        goalPos = new Point2D.Double(goalX, goalY);
        startPos = new Point2D.Double(startX, startY);
        
        usePotentialFunction = theParams.getBooleanParam("give-potential-function-reward");
        mapNumber = theParams.getIntegerParam("map-number");

        randomActionProbability = theParams.getDoubleParam("random-action-prob");
        movementNoise = theParams.getDoubleParam("movement-noise");
        
        double goalWidth, goalHeight;
        goalWidth = goalHeight = 25.0;

        addResetRegion(new Rectangle2D.Double(
                goalX-goalWidth/2,
                goalY-goalHeight/2,
                goalWidth, goalHeight));
        addRewardRegion(new Rectangle2D.Double(
                goalX-goalWidth/2,
                goalY-goalHeight/2,
                goalWidth, goalHeight), 1.0);

        createMap(mapNumber);

        // Set the shaping reward scale, which is the maximum distance in the world
        shapingRewardScale = theParams.getDoubleParam("potential-function-scale");
    }

    private void createMap(int number) {
        switch (number) {
            case MAP_EMPTY: // Empty map
                break;
            case MAP_CUP: // Cup map
                addBarrierRegion(new Rectangle2D.Double(50.0d, 50.0d, 10.0d, 100.0d), 1.0d);
                addBarrierRegion(new Rectangle2D.Double(50.0d, 50.0d, 100.0d, 10.0d), 1.0d);
                addBarrierRegion(new Rectangle2D.Double(150.0d, 50.0d, 10.0d, 100.0d), 1.0d);
                break;
            default:
                throw new IllegalArgumentException ("Map number "+number);
        }
    }

    @Override
    public String env_init() {
        return makeTaskSpec();
    }

    @Override
    public Observation env_start() {
		setAgentPosition(startPos);

        return makeObservation();

    }

    protected String makeTaskSpec() {
        TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
        theTaskSpecObject.setEpisodic();
        theTaskSpecObject.setDiscountFactor(discountFactor);
        theTaskSpecObject.addContinuousObservation(new DoubleRange(getWorldRect().getMinX(), getWorldRect().getMaxX()));
        theTaskSpecObject.addContinuousObservation(new DoubleRange(getWorldRect().getMinY(), getWorldRect().getMaxY()));
        theTaskSpecObject.addDiscreteAction(new IntRange(0, 3));
        theTaskSpecObject.setRewardRange(new DoubleRange(-1, 1));
        theTaskSpecObject.setExtra("EnvName:PotentialFuncContinuousGridWorld");
        String taskSpecString = theTaskSpecObject.toTaskSpec();
        TaskSpec.checkTaskSpec(taskSpecString);
        return taskSpecString;

    }

    public static TaskSpecPayload getTaskSpecPayload(ParameterHolder P) {
        PotentialFuncContinuousGridWorld theGridWorld =
                new PotentialFuncContinuousGridWorld(P);
        String taskSpec = theGridWorld.makeTaskSpec();
        return new TaskSpecPayload(taskSpec, false, "");
    }

    @Override
    public Reward_observation_terminal env_step(Action action) {
        lastAgentPos = agentPos;
        // Fudge the action a bit
        int theAction;
        if (randomActionProbability > 0 && 
                randomMaker.nextDouble() < randomActionProbability) {
            theAction = (int)(randomMaker.nextDouble() * numActions);
        }
        else
            theAction = action.intArray[0];

        double dx = 0;
        double dy = 0;

        if (theAction == 0) {
            dx = walkSpeed;
        }
        if (theAction == 1) {
            dx = -walkSpeed;
        }
        if (theAction == 2) {
            dy = walkSpeed;
        }
        if (theAction == 3) {
            dy = -walkSpeed;        //Add a small bit of random noise
        }
        double noiseX = randomMaker.nextGaussian() * movementNoise;
        double noiseY = randomMaker.nextGaussian() * movementNoise;

        dx += noiseX;
        dy += noiseY;
        Point2D nextPos = new Point2D.Double(agentPos.getX() + dx, agentPos.getY() + dy);


        nextPos = updateNextPosBecauseOfWorldBoundary(nextPos);
        nextPos = updateNextPosBecauseOfBarriers(nextPos);

        agentPos = nextPos;
        updateCurrentAgentRect();
        boolean inResetRegion = false;

        for (int i = 0; i < resetRegions.size(); i++) {
            if (resetRegions.get(i).contains(currentAgentRect)) {
                inResetRegion = true;
            }
        }

        return makeRewardObservation(getReward(), inResetRegion);
    }

    /** Provide the agent with some reward, possibly with a potential function
     *    bonus added in
     * 
     * @return The reward for the current state
     */
    @Override
    protected double getReward() {
        double baseReward = super.getReward();
        double shapingReward=0.0d;

        if (usePotentialFunction) {
            // Multiply the next potential by the discount factor
            double lastDist = lastAgentPos.distance(goalPos);
            double dist = discountFactor*agentPos.distance(goalPos);

            shapingReward = (lastDist - dist) / shapingRewardScale;
        }

        return baseReward + shapingReward;
    }
}
