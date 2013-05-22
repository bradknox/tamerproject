/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rlcommunity.environments.continuousgridworld.visualizer;

import java.awt.geom.Rectangle2D;
import java.util.Vector;
import rlVizLib.visualization.interfaces.AgentOnValueFunctionDataProvider;
import rlVizLib.visualization.interfaces.GlueStateProvider;
import rlVizLib.visualization.interfaces.ValueFunctionDataProvider;

/**
 *
 * @author btanner
 */
public interface GridWorldVisualizerInterface extends AgentOnValueFunctionDataProvider, GlueStateProvider, ValueFunctionDataProvider {

    Vector<Rectangle2D> getBarrierRegions();

    Vector<Double> getPenalties();

    Vector<Rectangle2D> getResetRegions();

    Vector<Rectangle2D> getRewardRegions();

    Vector<Double> getTheRewards();

    Rectangle2D getWorldRect();

    void updateAgentState();

}
