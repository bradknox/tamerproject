package org.rlcommunity.environments.continuousgridworld;

import java.awt.geom.Rectangle2D;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import rlVizLib.general.ParameterHolder;



/*
 *  ContinuousGridWorld
 *
 *  Created by Brian Tanner on 02/03/07.
 *  Copyright 2007 Brian Tanner. All rights reserved.
 *
 */
import rlVizLib.general.hasVersionDetails;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;


public class ContinuousGridWorld extends AbstractContinuousGridWorld {

    public ContinuousGridWorld() {
        this(getDefaultParameters());
    }

    public ContinuousGridWorld(ParameterHolder theParams) {
        super(theParams);
    }

    public static ParameterHolder getDefaultParameters() {
        ParameterHolder myParams=AbstractContinuousGridWorld.getDefaultParameters();
        return myParams;
    }

    protected String makeTaskSpec() {
        TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
        theTaskSpecObject.setEpisodic();
        theTaskSpecObject.setDiscountFactor(1.0d);
        theTaskSpecObject.addContinuousObservation(new DoubleRange(getWorldRect().getMinX(), getWorldRect().getMaxX()));
        theTaskSpecObject.addContinuousObservation(new DoubleRange(getWorldRect().getMinY(), getWorldRect().getMaxY()));
        theTaskSpecObject.addDiscreteAction(new IntRange(0, 3));
        theTaskSpecObject.setRewardRange(new DoubleRange(-1, 1));
        theTaskSpecObject.setExtra("EnvName:ContinuousGridWorld");
        String taskSpecString = theTaskSpecObject.toTaskSpec();
        TaskSpec.checkTaskSpec(taskSpecString);
        return taskSpecString;

    }

    public static TaskSpecPayload getTaskSpecPayload(ParameterHolder P) {
        ContinuousGridWorld theGridWorld = new ContinuousGridWorld(P);
        String taskSpec = theGridWorld.makeTaskSpec();
        return new TaskSpecPayload(taskSpec, false, "");
    }

    @Override
    protected void addBarriersAndGoal(ParameterHolder theParams) {
        addRewardRegion(new Rectangle2D.Double(0.0d, 0.0d, 200.0d, 200.0d), -1.0d);
        addResetRegion(new Rectangle2D.Double(75.0, 75.0, 25.0, 25.0));
        addRewardRegion(new Rectangle2D.Double(75.0, 75.0, 25.0, 25.0), 1.0);
        addBarrierRegion(new Rectangle2D.Double(50.0, 50.0, 10.0, 100.0), 1.0);
        addBarrierRegion(new Rectangle2D.Double(50.0, 50.0, 100.0, 10.0), 1.0);
        addBarrierRegion(new Rectangle2D.Double(150.0, 50.0, 10.0, 100.0), 1.0);
    }
}
class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "Soft Obstacle Continuous Grid World 0.1";
    }

    public String getShortName() {
        return "Cont-Grid-World";
    }

    public String getAuthors() {
        return "Brian Tanner";
    }

    public String getInfoUrl() {
        return "http://library.rl-community.org/environments/continuousgridworld";
    }

    public String getDescription() {
        return "RL-Library Java Version of a continuous grid world with soft obstacles.";
    }
}