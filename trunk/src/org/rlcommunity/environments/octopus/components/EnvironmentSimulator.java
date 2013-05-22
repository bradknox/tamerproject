package org.rlcommunity.environments.octopus.components;

import org.rlcommunity.environments.octopus.odeframework.*;
import java.util.*;
import java.awt.Shape;

public class EnvironmentSimulator  {
    
    private Arm arm;
    private Set<Food> food;
    
    private Set<Node> nodes;
        
    private ODEEquationPart rootEquationPart;
    private ODEEquation equation;
    
    public EnvironmentSimulator(Arm arm, Set<Food> food) {
        super();
        this.arm = arm;
        this.food = new LinkedHashSet<Food>(food);
        
        nodes = new LinkedHashSet<Node>();
        nodes.addAll(arm.getNodes());
        nodes.addAll(food);
        
        Influence gravity = new GravityInfluence();
        Influence buoyancy = new BuoyancyInfluence();
        Influence friction = new SphericalFrictionInfluence();
                
        for (Node n: nodes) {
            n.addInfluence(gravity);
            n.addInfluence(buoyancy);
        }

        /* Spherical friction and repulsion apply to food only. */
        for (Node f: food) {
            f.addInfluence(friction);
            /* apply a repulsion force from every other node */
            for (Node n: nodes) {
                Influence repulsion = new RepulsionInfluence(n);
                if (n != f) {
                    f.addInfluence(repulsion);
                }
            }
        }
        
        List<ODEEquationPart> parts = new ArrayList<ODEEquationPart>();
        parts.add(arm.asEquationPart());
        for (Node n: food) {
            parts.add(n);
        }
        
        rootEquationPart = new ODEEquationPartAggregate(parts) {
            public void assumeTimeAndState(double t, ODEState s) {
                super.assumeTimeAndState(t, s);
                EnvironmentSimulator.this.arm.updateInfluences();
            }
        };
        
        equation = new PartBasedEquation(rootEquationPart);
    }
    
    /* convenience methods for setting/getting state */
    
    public void setODEState(ODEState s) {
        /* the time here is irrelevant */
        rootEquationPart.assumeTimeAndState(0.0, s);
    }
    
    public ODEState getODEState() {
        return rootEquationPart.getCurrentODEState();
    }
    
    
    /**
     * <p>Returns an ODEEquation view of this EnvironmentSimulator.</p>
     */
    public ODEEquation asEquation() {
        return equation;
    }
}
