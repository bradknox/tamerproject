/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rlcommunity.rlviz.app.frames;

import org.rlcommunity.rlviz.app.RLGlueLogic;
import java.awt.Dimension;
import org.rlcommunity.rlviz.app.visualizerLoadListener;

import rlVizLib.visualization.AbstractVisualizer;

/**
 *
 * @author btanner
 */
public class EnvVisualizerFrame extends VisualizerVizFrame{
    private static final long serialVersionUID = 1L;

    public EnvVisualizerFrame(Dimension theSize){
        super("Environment Visualizer",theSize);
    }

    @Override
    protected void register() {
       RLGlueLogic.getGlobalGlueLogic().addEnvVisualizerLoadListener(this);
       RLGlueLogic.getGlobalGlueLogic().setEnvironmentVisualizerControlTarget(super.theDynamicControlTargetPanel);
       RLGlueLogic.getGlobalGlueLogic().addEnvVisualizerLoadListener(super.theDynamicControlTargetPanel);
       RLGlueLogic.getGlobalGlueLogic().addEnvVisualizerLoadListener(super.theVizPanel);
    }

    @Override
    protected String getWindowName() {
        return "Environment Visualizer";
    }

    @Override
    protected int getVisualizerType() {
        return VisualizerVizFrame.EnvVisualizerType;
    }
}
