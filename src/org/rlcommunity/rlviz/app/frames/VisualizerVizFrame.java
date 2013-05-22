/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rlcommunity.rlviz.app.frames;

import java.awt.Color;

import org.rlcommunity.rlviz.app.VisualizerPanel;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.rlcommunity.rlviz.app.visualizerLoadListener;

import rlVizLib.visualization.AbstractVisualizer;

/**
 *
 * @author btanner
 */
public abstract class VisualizerVizFrame extends GenericVizFrame implements visualizerLoadListener {

    protected VisualizerPanel theVizPanel = null;
    protected VizFrameControlTarget theDynamicControlTargetPanel = null;
    protected JPanel theCompositePanel = null;

    protected abstract void register();

    protected abstract String getWindowName();
    static public final int AgentVisualizerType = 0;
    static public final int EnvVisualizerType = 1;

    protected abstract int getVisualizerType();
    //Frame is told a size when it is created and if some visualizers override this
    //we might want to fall back on the default.

    public VisualizerVizFrame(String theName, Dimension theSize) {
        super(theName);
        setPreferredSize(theSize);

        theCompositePanel = new JPanel();
        theCompositePanel.setLayout(new BoxLayout(theCompositePanel, BoxLayout.X_AXIS));

        int eachHeight = theSize.height;
        Dimension halfSize = new Dimension(theSize.width / 2, eachHeight);
        Dimension thirdSize = new Dimension(theSize.width / 3, eachHeight);
        Dimension twoThirdSize = new Dimension(2 * theSize.width / 3, eachHeight);
        theCompositePanel.setPreferredSize(theSize);

        theDynamicControlTargetPanel = new VizFrameControlTarget(thirdSize);

        theVizPanel = new VisualizerPanel(twoThirdSize, getVisualizerType());
        theVizPanel.setPreferredSize(twoThirdSize);
        //Setup the border for the Visualizer
        //NOTE: WE DO THIS AGAIN in VisualizerPanel.notifyOfVisualizerLoaded()
        //Nov 2009: I'm commenting this out because we show the frame when we load
        //the visualizer now, so maybe we don't need the placeholder name.

//        TitledBorder vizPanelTitledBorder = null;
//        lowerEtchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
//        vizPanelTitledBorder = BorderFactory.createTitledBorder(lowerEtchedBorder, getWindowName());
//        theVizPanel.setBorder(vizPanelTitledBorder);

        Border lowerEtchedBorder = null;
        TitledBorder dynamicControlTargetTitledBorder = BorderFactory.createTitledBorder(lowerEtchedBorder, "Controls");
        theDynamicControlTargetPanel.setBorder(dynamicControlTargetTitledBorder);

        theCompositePanel.add(theDynamicControlTargetPanel);
        theCompositePanel.add(theVizPanel);
        //Register to be told about env/agent loads ad unloads
        register();
        getContentPane().add(theCompositePanel);
        pack();
        this.setBackground(Color.white);
    }

    public void notifyVisualizerLoaded(AbstractVisualizer theNewVisualizer) {
        if (theNewVisualizer != null) {
            theDynamicControlTargetPanel.setVisible(theNewVisualizer.wantsDynamicControls());
            theCompositePanel.revalidate();

            int numOverrides = 0;
            if (theNewVisualizer.getOverrideLocation() != null) {
                this.setLocation(theNewVisualizer.getOverrideLocation());
                numOverrides++;
            }
            if (theNewVisualizer.getOverrideSize() != null) {
                this.setSize(theNewVisualizer.getOverrideSize());
                numOverrides++;
            }
            if (numOverrides == 1) {
                System.err.println("If you are going to override the sizes, you should override the position too and vice versa.  I am overriding your overrides to defaults.");
            }
            if (numOverrides < 2) {
                if (getVisualizerType() == EnvVisualizerType) {
                    ((RLVizFrame) controlVizFrame).setEnvSizeAndLocation();
                } else {
                    //It's an agent viz.
                    ((RLVizFrame) controlVizFrame).setAgentSizeAndLocation();
                }

            }
        }
        super.setVisible(true);

    }

    public void notifyVisualizerUnLoaded() {
        super.setVisible(false);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            theVizPanel.startVisualizing();
            this.invalidate();
        }
        if (!b) {
            theVizPanel.stopVisualizing();
        }
        super.setVisible(b);

    }
}
