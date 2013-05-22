/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rlcommunity.rlviz.app.frames;

import org.rlcommunity.rlviz.app.visualizerLoadListener;

import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.interfaces.DynamicControlTarget;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 *
 * @author btanner
 */
public class VizFrameControlTarget extends JPanel implements DynamicControlTarget, visualizerLoadListener {

    Vector<Component> dynamicComponents = null;
    Component theFiller = null;
    Dimension defaultSize = null;

    public VizFrameControlTarget(Dimension theSize) {
        this.defaultSize = theSize;
        dynamicComponents = new Vector<Component>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        setPreferredSize(theSize);
        addFiller();

    }

    private void addFiller() {
        Dimension minSize = new Dimension(5, 5);
        Dimension prefSize = defaultSize;
        Dimension maxSize = new Dimension(defaultSize.width, Integer.MAX_VALUE);
        theFiller = new Box.Filler(minSize, prefSize, maxSize);
        add(theFiller);

    }

    private void removeFiller() {
        remove(theFiller);
    }

    public void removeControl(Component c) {

        removeFiller();

        removeControlsFromFrame();
        Vector<Component> updatedDynamicComponents = new Vector<Component>();
        for (Component component : dynamicComponents) {
            if (component != c) {
                updatedDynamicComponents.add(component);
            }
        }
        dynamicComponents = updatedDynamicComponents;
        addControlsToFrame();
        addFiller();
        getParent().validate();
        validate();
    }

    public void addControls(Vector<Component> newComponents) {
        removeFiller();

        removeControlsFromFrame();
        for (Component component : newComponents) {
            dynamicComponents.add(component);
        }
        addControlsToFrame();
        addFiller();
        getParent().validate();
        validate();
    }

    private void addControlsToFrame() {
        for (Component component : dynamicComponents) {
            add(component);
        }
    }

    private void removeControlsFromFrame() {
        for (Component component : dynamicComponents) {
            remove(component);
        }

    }

    public void clear() {
        //This is going to need to be refactored maybe to be environment and agent components, maybe not
        removeControlsFromFrame();
        dynamicComponents.removeAllElements();
        validate();
    }

    public void notifyVisualizerLoaded(AbstractVisualizer theNewVisualizer) {
        //nothing here
    }

    public void notifyVisualizerUnLoaded() {
        clear();
    }
}
