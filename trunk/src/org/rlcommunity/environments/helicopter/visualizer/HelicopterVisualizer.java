/* RLViz Helicopter Domain Visualizer  for RL - Competition 
* Copyright (C) 2007, Brian Tanner
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */
package org.rlcommunity.environments.helicopter.visualizer;

import org.rlcommunity.environments.helicopter.messages.HelicopterRangeRequest;
import org.rlcommunity.environments.helicopter.messages.HelicopterRangeResponse;
import java.awt.Component;
import java.util.Vector;
import javax.swing.JLabel;
import rlVizLib.visualization.interfaces.GlueStateProvider;
import rlVizLib.general.TinyGlue;
import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.GenericScoreComponent;
import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.interfaces.DynamicControlTarget;

public class HelicopterVisualizer extends AbstractVisualizer implements GlueStateProvider {

    private HelicopterRangeResponse currentRange = null;
    private TinyGlue theGlueState = null;
    private DynamicControlTarget theControlTarget = null;

    
    public HelicopterVisualizer(TinyGlue theGlueState, DynamicControlTarget theControlTarget) {
        super();
        this.theGlueState = theGlueState;
        this.theControlTarget = theControlTarget;
        SelfUpdatingVizComponent theCounterViz = new GenericScoreComponent(this);
        SelfUpdatingVizComponent theEquilizerViz = new HelicopterEquilizerComponent(this);

        addVizComponentAtPositionWithSize(theEquilizerViz, 0, 0, 1.0, 1.0);
        addVizComponentAtPositionWithSize(theCounterViz, 0, 0, 1.0, 0.5);
        updateParamRanges();
        addDesiredExtras();

    }

    protected void addDesiredExtras() {
        addPreferenceComponents();
    }

    public void addPreferenceComponents() {
        if (theControlTarget != null) {
            Vector<Component> newComponents = new Vector<Component>();
            JLabel HeliControlLabel = new JLabel("Helicopter Controls");
            HeliControlLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            newComponents.add(HeliControlLabel);

            theControlTarget.addControls(newComponents);
        }
    }


    public boolean updateParamRanges() {
        if (currentRange == null) {
            currentRange = HelicopterRangeRequest.Execute();
            return true;
        }
        return false;
    }

    // getters here

    public double[] getState() {
        if(theGlueState.getLastObservation()==null){
            return null;
        }
        return theGlueState.getLastObservation().doubleArray;
    }

    public double getMinAt(int i) {
        return currentRange.getMinAt(i);
    }

    public double getMaxAt(int i) {
        return currentRange.getMaxAt(i);
    }

    public double[] getMins() {
        return currentRange.getMins();
    }

    public double[] getMaxs() {
        return currentRange.getMaxs();
    }

    public TinyGlue getTheGlueState() {
        return theGlueState;
    }

    @Override
    public String getName() {
        return "Helicopter Hovering 1.1 (DEV)";
    }

    TinyGlue getGlueState() {
        return theGlueState;
    }

}