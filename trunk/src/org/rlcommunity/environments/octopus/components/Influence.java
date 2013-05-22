package org.rlcommunity.environments.octopus.components;

/**
 * Models forces acting on nodes. Each node has a certain number of 
 * influences dictating it's dynamic behaviour.
 */
public interface Influence {
	/**
	 * Returns the force due to an influence on a particular node.
	 * @param target The node on which we want to get the forcu due to this influence.
	 * @return the force due to this influence on the target node.
	 */
    public Vector2D getForce(Node target);
}
