package org.rlcommunity.environments.octopus.components;

import org.rlcommunity.environments.octopus.config.*;
import org.rlcommunity.environments.octopus.odeframework.*;

import java.util.*;

/**
 * <p>A two-particle system that is constrained to rotate about its geometric
 * center.</p>
 */
public class ArmBase implements ODEEquationPart {
    
    private static final int STATE_LENGTH = 2;
    
    private RotationConstrainedNode upper, lower;
    
    private Vector2D center;
    private double radius;
    private double inertia;
    
    private double angularPosition, angularVelocity;
    
    private double posTorque, negTorque;
    
    private double assumedTime;
    
    public ArmBase(NodePairSpec pairSpec) {
        upper = new RotationConstrainedNode(pairSpec.getUpper());
        lower = new RotationConstrainedNode(pairSpec.getLower());
        
        center = upper.getPosition().add(lower.getPosition()).scale(0.5);
        
        Vector2D radVector = upper.getPosition().subtract(center);
        radius = radVector.norm();
        inertia = radius * radius * (upper.getMass() + lower.getMass());
        
        angularPosition = Math.atan2(radVector.getY(), radVector.getX());
        
        /* override the supplied initial velocities of the nodes; they might not
         * respect the constraint */
        upper.setVelocity(Vector2D.ZERO);
        lower.setVelocity(Vector2D.ZERO);        
        angularVelocity = 0;
        
        posTorque = 0.0;
        negTorque = 0.0;
    }
    
    public void setAction(double posTorque, double negTorque) {
        this.posTorque = posTorque;
        this.negTorque = negTorque;
    }
    
    public Node getUpperNode() {
        return upper;
    }
    
    public Node getLowerNode() {
        return lower;
    }
    
    public ODEState getCurrentODEState() {
        while(angularPosition>Math.PI)angularPosition-=2.0d*Math.PI;
        while(angularPosition<-Math.PI)angularPosition+=2.0d*Math.PI;
        return new ODEState(new double[] {angularPosition, angularVelocity});
    }
    
    public ODEState getODEStateDerivative() {
        double torque = Constants.get().getTorqueCoefficient()
            * (posTorque - negTorque);
        for (Node n: Arrays.asList(upper, lower)) {
            Vector2D radius = n.getPosition().subtract(center);
            torque += radius.crossMag(n.getNetForce());
        }
        double angularAccel = torque / inertia;
        return new ODEState(new double[] {angularVelocity, angularAccel});
    }
    
    public void assumeTimeAndState(double time, ODEState state) {
        this.assumedTime = time;
        
        double[] s = state.getArray();
        angularPosition = s[0];
        
        angularVelocity = s[1];

        Vector2D radVector = Vector2D.polar(radius, angularPosition);
        upper.setPosition(center.add(radVector));
        lower.setPosition(center.subtract(radVector));
        
        /* equivalent to standard "omega cross r" formula */
        upper.setVelocity(radVector.rotate90().scale(angularVelocity));
        lower.setVelocity(radVector.rotate270().scale(angularVelocity));
    }
    
    public int getStateLength() {
        return STATE_LENGTH;
    }
    
    private class RotationConstrainedNode extends Node {
        
        private final ODEState EMPTY_STATE = new ODEState(new double[0]);
        
        private RotationConstrainedNode(NodeSpec spec) {
            super(spec);
        }
        
        public void setPosition(Vector2D position) {
            this.position = position;
        }
        
        public void setVelocity(Vector2D velocity) {
            this.velocity = velocity;
        }
        
        public int getStateLength() {
            return 0;
        }
        
        public ODEState getCurrentODEState() {
            return EMPTY_STATE;
        }
        
        public ODEState getODEStateDerivative() {
            return EMPTY_STATE;
        }
        
        public void assumeTimeAndState(double time, ODEState state) {
            /* does nothing */
        }
    }

    public void setODEState(double time, ODEState state) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double[] getODEStateDerivativeArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}