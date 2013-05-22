package org.rlcommunity.environments.octopus.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Models an octopus muscle ( as a link between two vertices).
 *
 */
abstract class MuscleInfluence implements Influence {
    
    protected Node n1,n2;
    protected double width;
    protected double initialLength;
    protected double action;
    protected Map<Node, Vector2D> forces;
    
    protected double ActiveConstant;
    protected double PassiveConstant;
    protected double DampingConstant;
    
    public MuscleInfluence(Node n1, Node n2, double width) {
        this.n1 = n1;
        this.n2 = n2;
        this.width = width;
        this.action = 0;
        this.initialLength = n1.getPosition().subtract(n2.getPosition()).norm();
        this.forces = new HashMap<Node, Vector2D>();
        
        // The constants are not set here, override this class to set the constants to the proper value
        this.ActiveConstant = 0;
        this.PassiveConstant = 0;
        this.DampingConstant = 0;
    }
    
    public void update() {
        Vector2D displacement = n2.getPosition().subtract(n1.getPosition());
        Vector2D center = n1.getPosition().addScaled(displacement, 0.5);
        Vector2D velocity = n1.getVelocity().subtract(n2.getVelocity());
        
        double projectedVelocity = velocity.dot(displacement.normalize());
        // Follows the linear muscle model
        double normalizedLength = displacement.norm()/initialLength;
        double forceMag = 0;
        if(normalizedLength > Constants.get().getMuscleNormalizedMinLength()) {
            forceMag = (ActiveConstant * action + PassiveConstant) * (normalizedLength - Constants.get().getMuscleNormalizedMinLength());
        }
        forceMag += projectedVelocity * DampingConstant;
        for (Node n: Arrays.asList(n1, n2)) {
            forces.put(n, center.subtract(n.getPosition()).scaleTo(forceMag));
        }
    }
    
    public void setAction(double action) {
        /* clamp action to [0, 1] */
        action = Math.max(action, 0.0);
        action = Math.min(action, 1.0);
        this.action = action;
    }
    
    public Vector2D getForce(Node target) {
        return forces.containsKey(target) ?
            forces.get(target) :
            Vector2D.ZERO;
    }
}