package org.rlcommunity.environments.octopus.components;

import org.rlcommunity.environments.octopus.config.*;
import java.awt.geom.*;

/**
 * <p>A piece of food.</p>
 */
public class Food extends UnconstrainedNode {
    
    private double value;
    
    public Food(FoodSpec spec) {
        super(spec);
        value = spec.getReward();
    }
    
    public void warp() {
        double coord = (0.5 + Math.random()/2.0) * Double.MAX_VALUE;
        position = new Vector2D(coord, coord);
    }
    
    public double getValue() {
        return value;
    }
}
