package org.rlcommunity.environments.cartpole.visualizer;

import org.rlcommunity.environments.cartpole.messages.CartpoleTrackRequest;
import org.rlcommunity.environments.cartpole.messages.CartpoleTrackResponse;
import org.rlcommunity.environments.cartpole.messages.StateRequest;
import org.rlcommunity.environments.cartpole.messages.StateResponse;
import org.rlcommunity.rlglue.codec.types.Action;

import rlVizLib.general.TinyGlue;
import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.GenericScoreComponent;
import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.interfaces.GlueStateProvider;

public class CartPoleVisualizer extends AbstractVisualizer implements GlueStateProvider {

    private TinyGlue theGlueState = null;
    private CartpoleTrackResponse trackResponse = null;
    private StateResponse theCurrentState = null;

    /**
     * Creates a new Cart Pile Visualizer
     * @param theGlueState Global glue state object
     */
    public CartPoleVisualizer(TinyGlue theGlueState) {
        super();
        this.theGlueState = theGlueState;

        SelfUpdatingVizComponent theTrackVisualizer = new CartPoleTrackComponent(this);
        SelfUpdatingVizComponent theCartVisualizer = new CartPoleCartComponent(this);

        //SelfUpdatingVizComponent scoreComponent = new GenericScoreComponent(this);

        super.addVizComponentAtPositionWithSize(theTrackVisualizer, 0, 0, 1.0, 1.0);
        super.addVizComponentAtPositionWithSize(theCartVisualizer, 0, 0, 1.0, 1.0);
        //super.addVizComponentAtPositionWithSize(scoreComponent, 0, 0, 1.0, 1.0);
    }

    public void checkCoreData() {
        if (trackResponse == null) {
            trackResponse = CartpoleTrackRequest.Execute();
        }
        if (theCurrentState == null) {
            updateState();
        }
    }

    void updateState() {
        theCurrentState = StateRequest.Execute();
    }

    public double getLeftCartBound() {
        checkCoreData();
        return trackResponse.getLeftGoal();
    }

    public double getRightCartBound() {
        checkCoreData();
        return trackResponse.getRightGoal();
    }

    public double currentXPos() {
        checkCoreData();
        return theCurrentState.getX();
    }

    public double getMinAngle() {
        checkCoreData();
        return translateAngle(trackResponse.getMinAngle());
    }

    public double getMaxAngle() {
        checkCoreData();
        return translateAngle(trackResponse.getMaxAngle());
    }

    public double getAngle() {
        checkCoreData();
        return translateAngle(theCurrentState.getAngle());
    }

    /**
     * Simple translation makes it easier to draw.
     * @return
     */
    private static double translateAngle(double origAngle) {
        return origAngle - 2.0 * Math.PI / 4.0;
    }

    public int getLastAction() {
        int lastAction = -1;
        Action lastActionObject = getTheGlueState().getLastAction();
        //This might be null at the first step of an episode                                                                         
        if (lastActionObject != null) {
            lastAction = lastActionObject.intArray[0];
        }
        return lastAction;
    }

    public TinyGlue getTheGlueState() {
        return theGlueState;
    }

    @Override
    public String getName() {
        return "Cart-Pole 1.0 (DEV)";
    }
}
