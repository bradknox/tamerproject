package org.rlcommunity.environments.octopus.components;


/**
 * A friction class representing a cylinder or other object which has
 * a different friction coefficient in the tangential and in the
 * perpendicular direction.
 * 
 */
public class AxialFrictionInfluence implements Influence {
    private Node target;
    private Node axialNode;
    
    /**
     * The friction influence will only influence the target, the axial
     * node serves only to give the orientation of the segment that
     * gives the tangential direction for friction computation.
     * @param target The node on which the friction will be computed.
     * @param axialNode A node that serves only to determine the orientation of the axis.
     */
    public AxialFrictionInfluence(Node target, Node axialNode) {
        this.target = target;
        this.axialNode = axialNode;
    }
    
    public Vector2D getForce(Node target) {
        if(target == this.target) {
            Vector2D axis = target.getPosition().subtract(axialNode.getPosition()).normalize();
            // We project the speed in the perpendicular and tangential 
            // direction and apply the different coefficients to each
            Vector2D velocity = target.getVelocity();
            double tanSpeed = velocity.dot(axis);
            Vector2D tangential = axis.scaleTo(tanSpeed);
            Vector2D perpendicular = velocity.subtract(tangential);
            double perSpeed = perpendicular.norm();
            return tangential.scaleTo(-tanSpeed*tanSpeed*Constants.get().getFrictionTangential())
                .add(perpendicular.scaleTo(-perSpeed*perSpeed*Constants.get().getFrictionPerpendicular()));
        } else {
            return Vector2D.ZERO;
        }
    }

}
