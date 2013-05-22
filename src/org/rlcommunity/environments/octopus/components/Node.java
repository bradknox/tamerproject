package org.rlcommunity.environments.octopus.components;

import java.util.*;

import org.rlcommunity.environments.octopus.config.*;
import org.rlcommunity.environments.octopus.odeframework.*;

/**
 * <p>A particle (point mass) with a mass, position, and velocity, to which
 * forces can be applied. Particles represented by Node instances may be subject
 * to kinematic constraints, so their motion is not necessarily determined
 * directly by the forces acting on them. Concrete Node subclasses can
 * represent particles with specific constraint configurations, and will
 * implement the {@link ODEEquationPart} abstract methods to provide appropriate
 * equations of motion.</p>
 */
public abstract class Node implements ODEEquationPart {
    
    private List<Influence> influences;
    private double mass;
    
    /** This Node's current position. Updated by subclasses. */
    protected Vector2D position;
    
    /** This Node's current velocity. Updated by subclasses. */
    protected Vector2D velocity;
    
    public Node(NodeSpec spec) {
        this(spec.getMass(),
                Vector2D.fromDuple(spec.getPosition()),
                Vector2D.fromDuple(spec.getVelocity()));
    }
    
    public Node(double mass, Vector2D initialPosition, Vector2D initialVelocity) {
        this.mass = mass;
        this.position = initialPosition;
        this.velocity = initialVelocity;
        influences = new ArrayList<Influence>();
    }
    
    public double getMass() {
        return mass;
    }
    
    public Vector2D getPosition() {
        return position;
    }
    
    public Vector2D getVelocity() {
        return velocity;
    }
    
    public void addInfluence(Influence inf) {
        influences.add(inf);
    }
    
    public Vector2D getNetForce() {
        Vector2D netForce = Vector2D.ZERO;
        for (Influence i : influences) {
            netForce = netForce.add(i.getForce(this));
        }
        return netForce;
    }
}
