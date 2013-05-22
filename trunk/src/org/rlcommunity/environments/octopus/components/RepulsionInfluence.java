package org.rlcommunity.environments.octopus.components;

/**
 * Represents a repulsive field from a single node.
 */
public class RepulsionInfluence implements Influence {
    
    private Node source;
    
    public RepulsionInfluence(Node source) {
        this.source = source;
    }
    
    public Vector2D getForce(Node target) {
        Vector2D displacement = target.getPosition().subtract(source.getPosition());
        double distance = displacement.norm();
        if (distance < Constants.get().getRepulsionThreshold()) {
            double forceMag = Constants.get().getRepulsionConstant()
                / Math.pow(distance, Constants.get().getRepulsionPower());
            return displacement.scaleTo(forceMag);
        } else {
            return Vector2D.ZERO;
        }
        
    }
}