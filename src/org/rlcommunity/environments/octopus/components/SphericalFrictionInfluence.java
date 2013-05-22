package org.rlcommunity.environments.octopus.components;


/**
 * Implements viscous fluid friction for each node
 *
 */
public class SphericalFrictionInfluence implements Influence {
    
    
    /**
     * @see Influence#getForce(Node)
     */
    public Vector2D getForce(Node target) {
        /* We assume the friction constant is positive (otherwise positive
         * feedback occurs.) */
        double speed = target.getVelocity().norm();
        return target.getVelocity().scaleTo( -speed * speed * Constants.get().getFrictionPerpendicular());
    }

}