package org.rlcommunity.environments.octopus.components;



/**
 * Implements gravitational force for each node
 * The gravitational force is applied to nodes for the moment, but later
 * on it will also be applied to piece of food.
 *
 */
public class GravityInfluence implements Influence {
    
    public Vector2D getForce(Node target) {
        return new Vector2D(0, -Constants.get().getGravity() * target.getMass());
    }
}