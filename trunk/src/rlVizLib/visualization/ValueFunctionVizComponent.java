/*
Copyright 2007 Brian Tanner
brian@tannerpages.com
http://brian.tannerpages.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package rlVizLib.visualization;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.rlcommunity.rlglue.codec.types.Observation;

import rlVizLib.general.TinyGlue;
import rlVizLib.utilities.UtilityShop;
import rlVizLib.visualization.interfaces.DynamicControlTarget;
import rlVizLib.visualization.interfaces.ValueFunctionDataProvider;

/**
 *
 * @author btanner
 */
public class ValueFunctionVizComponent implements SelfUpdatingVizComponent, ChangeListener, ActionListener, Observer {

    long lastQueryTime = 0;
    Vector<Double> theValues = null;
    double bestV;
    double worstV;
    int VFRows;
    int VFCols;
    double rowGridSize;
    double colGridSize;
    double xQueryIncrement;
    double yQueryIncrement;
    ValueFunctionDataProvider dataProvider;
    double currentValueFunctionResolution;
    double newValueFunctionResolution;
    Vector<Observation> theQueryObservations = null;
    DynamicControlTarget theControlTarget = null;
    JSlider numColsOrRowsForValueFunction = null;
    boolean valueFunctionShowing = true;
    JButton refreshButton = new JButton("Update Value Function");
    Vector<Component> myUIComponents = new Vector<Component>();
    Vector<Component> AllMyUIComponents = new Vector<Component>();
    private TinyGlue theGlueState = null;
    JLabel vfPrefsLabel = null;
    JCheckBox autoUpdateValueFunction = null;
    private boolean enabled = true;

    public void setEnabled(boolean newEnableValue) {
        if (newEnableValue == false && this.enabled) {
            disable();
        }
        if (newEnableValue == true && !this.enabled) {
            enable();
        }
    }

    private void disable() {
        enabled = false;
        if (vfPrefsLabel != null) {
            vfPrefsLabel.setText("Value Function Not Supported");
        }
        for (Component thisComponent : AllMyUIComponents) {
            thisComponent.setEnabled(false);
        }
        theChangeListener.vizComponentChanged(this);

    }

    private void enable() {
        enabled = true;
    }

    public ValueFunctionVizComponent(ValueFunctionDataProvider theDataProvider, DynamicControlTarget theControlTarget, TinyGlue theGlueState) {
        super();
        this.theGlueState = theGlueState;
        currentValueFunctionResolution = 10.0;
        this.theControlTarget = theControlTarget;


        this.dataProvider = theDataProvider;

        bestV = Double.MIN_VALUE;
        worstV = Double.MAX_VALUE;

        theQueryObservations = null;

        //Setup the slider
        numColsOrRowsForValueFunction = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);
        numColsOrRowsForValueFunction.setPreferredSize(new Dimension(150, 50));
        numColsOrRowsForValueFunction.setSize(new Dimension(150, 50));

        setValueFunctionResolution(numColsOrRowsForValueFunction.getValue());
        JPanel tinyPanel = new JPanel();
        tinyPanel.setPreferredSize(new Dimension(150, 50));
        tinyPanel.add(numColsOrRowsForValueFunction);

        numColsOrRowsForValueFunction.addChangeListener(this);

        if (theControlTarget != null) {

            vfPrefsLabel = new JLabel("Value Function Preferences");
            JLabel autoUpdateLabel = new JLabel("Auto-update");
            vfPrefsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            autoUpdateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            autoUpdateValueFunction = new JCheckBox();
            autoUpdateValueFunction.setSelected(valueFunctionShowing);
            refreshButton.setEnabled(!valueFunctionShowing);
            refreshButton.addActionListener(this);

            JPanel autoUpdateGridPanel = new JPanel();
            autoUpdateGridPanel.add(autoUpdateLabel);
            autoUpdateGridPanel.add(autoUpdateValueFunction);

            JLabel valueFunctionResolutionLabel = new JLabel("Resolution for Value Function (right is finer)");
            valueFunctionResolutionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


            autoUpdateValueFunction.addChangeListener(this);

            myUIComponents.add(vfPrefsLabel);
            myUIComponents.add(valueFunctionResolutionLabel);
            myUIComponents.add(tinyPanel);
            myUIComponents.add(autoUpdateGridPanel);
            myUIComponents.add(refreshButton);
            
            AllMyUIComponents.addAll(myUIComponents);
            AllMyUIComponents.add(numColsOrRowsForValueFunction);
            AllMyUIComponents.add(autoUpdateValueFunction);
            AllMyUIComponents.add(autoUpdateLabel);

            theControlTarget.addControls(myUIComponents);
        }
        theGlueState.addObserver(this);
    }

    public int getIndexForRow(int row, int col) {
        return row + col * VFRows;
    }

    Vector<Observation> getQueryStates() {
        Vector<Observation> thePoints = new Vector<Observation>();

        int y = 0;
        int x = 0;

        for (y = 0; y < VFRows; y++) {
            for (x = 0; x < VFCols; x++) {

                //Query the value function in the agent
                double positionVal = dataProvider.getMinValueForDim(0) + x * xQueryIncrement;
                double velocityVal = dataProvider.getMinValueForDim(1) + y * yQueryIncrement;

                Observation thisState = new Observation(0, 2);
                thisState.doubleArray[0] = positionVal;
                thisState.doubleArray[1] = velocityVal;
                thePoints.add(thisState);
            }
        }
        return thePoints;
    }

    public void render(Graphics2D g) {
//This actually calls for data, so we want it in the render thread where it won't slow anyting else down
        if (!enabled) {
            Color myClearColor = new Color(0.0f, 0.0f, 0.0f, 0.0f);
            g.setColor(myClearColor);
            g.setBackground(myClearColor);
            g.clearRect(0, 0, 1, 1);
            return;
        }
        update();
        double y = 0;
        double x = 0;

        double thisBest = Double.MIN_VALUE;
        double thisWorst = Double.MAX_VALUE;

        if (theValues == null) {
            return;
        }

        int linearIndex = 0;

        for (y = 0; y < VFRows; y++) {
            for (x = 0; x < VFCols; x++) {
                Rectangle2D valueRect = new Rectangle2D.Double(x * rowGridSize, y * colGridSize, rowGridSize, colGridSize);
                double V = 0.0f;

                V = theValues.get(linearIndex);


                if (Double.isInfinite(V) || Double.isNaN(V)) {
                    System.out.println("The value at linear index: " + linearIndex + " + is " + V + "+ (size is " + theValues.size());
                }

                if (V < thisWorst) {
                    thisWorst = V;
                }
                if (V > thisBest) {
                    thisBest = V;
                }
                float greenValue = (float) UtilityShop.normalizeValue(V, worstV, bestV);

                if (greenValue < 0) {
                    greenValue = 0;
                }
                if (greenValue > 1) {
                    greenValue = 1;
                }
                Color theColor = new Color(0, greenValue, 0);

                g.setColor(theColor);
                g.fill(valueRect);

                linearIndex++;
            }
        }

        worstV = thisWorst;
        bestV = thisBest;
    }

    public void update() {
        if (newValueFunctionResolution != currentValueFunctionResolution || theQueryObservations == null) {

            currentValueFunctionResolution = newValueFunctionResolution;

            VFRows = (int) currentValueFunctionResolution;
            VFCols = (int) currentValueFunctionResolution;

            //The range of the position and velocity

            double xRangeSize = dataProvider.getMaxValueForDim(0) - dataProvider.getMinValueForDim(0);
            double yRangeSize = dataProvider.getMaxValueForDim(1) - dataProvider.getMinValueForDim(1);
            //QueryIncrements are the number that the query variables will change from cell to cell
            xQueryIncrement = xRangeSize / VFCols;
            yQueryIncrement = yRangeSize / VFRows;

            rowGridSize = 1.0d / VFRows;
            colGridSize = 1.0d / VFCols;

            Vector<Observation> theQueryStates = getQueryStates();
            theQueryObservations = dataProvider.getQueryObservations(theQueryStates);
        }

        theValues = dataProvider.queryAgentValues(theQueryObservations);
    }

    public double getValueFunctionResolution() {
        return currentValueFunctionResolution;
    }

    public void setValueFunctionResolution(int theValue) {
        newValueFunctionResolution = theValue;
    }

    public void stateChanged(ChangeEvent sliderChangeEvent) {
        if (sliderChangeEvent.getSource() instanceof JSlider) {
            JSlider source = (JSlider) sliderChangeEvent.getSource();
            int theValue = source.getValue();
            setValueFunctionResolution(theValue);
            update(null, "Slider Changed");
        }
        if (sliderChangeEvent.getSource() instanceof JCheckBox) {
            JCheckBox source = (JCheckBox) sliderChangeEvent.getSource();
            valueFunctionShowing = source.isSelected();
            refreshButton.setEnabled(!valueFunctionShowing);
            update(null, "Checkbox Changed");
        }
    }
    private VizComponentChangeListener theChangeListener = null;

    public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener) {
        this.theChangeListener = theChangeListener;
    }

    /**
     * This is because we are an observer of changes sent out by the TinyGlue.
     * @param o
     * @param arg
     */
    public void update(Observable o, Object theEvent) {


        if (theChangeListener != null && valueFunctionShowing && enabled) {
            theChangeListener.vizComponentChanged(this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        //This only happens when the button is pressed.
        if (theChangeListener != null) {
            theChangeListener.vizComponentChanged(this);
        }
    }
}