package org.rlcommunity.environments.octopus.components;

import org.rlcommunity.environments.octopus.config.*;
import org.rlcommunity.environments.octopus.odeframework.*;

/**
 * <p>A particle not subject to any kinematic constraints. Its motion
 * is fully determined by the forces acting on it.</p>
 */
public class UnconstrainedNode extends Node implements ODEEquationPart {
    
    private final static int STATE_LENGTH = 4;

    private double assumedTime;
    
    public UnconstrainedNode(NodeSpec spec) {
        super(spec);
        assumedTime = 0;
    }

    /**
     * Returns the current (assumed) state of this node, in the form
     * [positionX, positionY, velocityX, velocityY].
     *
     * @see ODEFramework.ODEEquationPart#getCurrentODEState()
     */
    public ODEState getCurrentODEState() {
        return new ODEState( new double[] {
            position.getX(), position.getY(),
            velocity.getX(), velocity.getY()
        } );
    }

    public ODEState getODEStateDerivative() {
        Vector2D accel = this.getNetForce().scale(1/this.getMass());
        return new ODEState( new double[] {
            velocity.getX(), velocity.getY(),
            accel.getX(), accel.getY()
        } );
    }

    public void assumeTimeAndState(double time, ODEState state) {
        this.assumedTime = time;

        double[] s = state.getArray();
        assert s.length == STATE_LENGTH;
        position = new Vector2D(s[0],s[1]);
        velocity = new Vector2D(s[2],s[3]);
    }

    public int getStateLength() {
        return STATE_LENGTH;
    }

    public void setODEState(double time, ODEState state) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double[] getODEStateDerivativeArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
