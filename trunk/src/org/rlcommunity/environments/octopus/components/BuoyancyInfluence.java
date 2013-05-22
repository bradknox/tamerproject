package org.rlcommunity.environments.octopus.components;

/**
 * Implements buoyancy force for each node (which corresponds to the upward
 * force on a body immersed or partly immersed in a fluid)
 *
 */
public class BuoyancyInfluence implements Influence {
 
    public Vector2D getForce(Node target) {
        Vector2D position = target.getPosition();
        double surfaceDistance = Constants.get().getSurfaceLevel() - position.getY();
        if (surfaceDistance > 0) {
//            return new Vector2D(0, Constants.get().getBuoyancy() * surfaceDistance);
            return new Vector2D(0, Constants.get().getGravity()*target.getMass());
        }  else {
            return Vector2D.ZERO;
        }
    }
}