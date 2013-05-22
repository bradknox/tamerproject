/*
Copyright 2007 Brian Tanner
http://rl-library.googlecode.com/
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
package org.rlcommunity.environments.tetris.visualizer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Component;
import java.util.Vector;
import java.lang.Integer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.rlcommunity.environments.tetris.messages.TetrisStateRequest;
import org.rlcommunity.environments.tetris.messages.TetrisStateResponse;
import org.rlcommunity.rlglue.codec.types.Observation;

import rlVizLib.general.TinyGlue;
import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.interfaces.DynamicControlTarget;

public class TetrisVisualizer extends AbstractVisualizer {

    private TetrisStateResponse currentState = null;
    private TinyGlue theGlueState = null;
    private int lastUpdateTimeStep = -1;
    javax.swing.JCheckBox printGridCheckBox = null;

    public boolean printGrid() {
        if (printGridCheckBox != null) {
            return printGridCheckBox.isSelected();
        }
        return false;
    }
    
    DynamicControlTarget theControlTarget = null;
    SelfUpdatingVizComponent theBlockVisualizer;
    SelfUpdatingVizComponent theTetrlaisScoreViz;

    public TetrisVisualizer(TinyGlue theGlueState, DynamicControlTarget theControlTarget) {
        super();

        this.theGlueState = theGlueState;
        this.theControlTarget = theControlTarget;
        theBlockVisualizer = new TetrisBlocksComponent(this);
        theTetrlaisScoreViz = new TetrisScoreComponent(this);

        addVizComponentAtPositionWithSize(theBlockVisualizer, 0, .1, 1.0, .87);
        addVizComponentAtPositionWithSize(theTetrlaisScoreViz, 0, 0, 1.0, 0.3);

        addDesiredExtras();
    }
//Override this if you don't want some extras (like check boxes)
    protected void addDesiredExtras() {
        //addPreferenceComponents();
    }

    public void checkCoreData() {
        if (currentState == null) {
            currentState = TetrisStateRequest.Execute();
        }
    }

    protected void addPreferenceComponents() {
        //Setup the slider
        printGridCheckBox = new JCheckBox();
        if (theControlTarget != null) {
            Vector<Component> newComponents = new Vector<Component>();
            JLabel tetrisPrefsLabel = new JLabel("Tetris Visualizer Preferences: ");
            JLabel printGridLabel = new JLabel("Draw Grid");
            tetrisPrefsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            printGridLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel gridPrefPanel = new JPanel();
            gridPrefPanel.add(printGridLabel);
            gridPrefPanel.add(printGridCheckBox);


            newComponents.add(tetrisPrefsLabel);
            newComponents.add(gridPrefPanel);

            theControlTarget.addControls(newComponents);
        }

    }

    public void updateAgentState(boolean force) {
//        //Only do this if we're on a new time step
        int currentTimeStep = theGlueState.getTotalSteps();


//        if (currentState == null || currentTimeStep != lastUpdateTimeStep || force) {
//            System.out.println("\t\tRequesting tetris state");
           currentState = TetrisStateRequest.Execute();
//            System.out.println("\t\tGot tetris state");
           lastUpdateTimeStep = currentTimeStep;
//        }
    }

    public int getWidth() {
        checkCoreData();
        return currentState.getWidth();
    }

    public int getHeight() {
        checkCoreData();
        return currentState.getHeight();
    }

    public double getScore() {
        return theGlueState.getReturnThisEpisode();
    }

    public int[] getWorld() {
        checkCoreData();
        return currentState.getWorld();
    }
    
    public int[] getPreviousBlock(){
        checkCoreData();
        return currentState.getPreviousBlock();
    }

    /* public int[] getSecToLastBlock(){
        checkCoreData();
        return currentState.getSecToLastBlock();
    }*/

    public int getEpisodeNumber() {
        return theGlueState.getEpisodeNumber();
    }

    public int getTimeStep() {
        return theGlueState.getTimeStep();
    }

    public int getTotalSteps() {
        return theGlueState.getTotalSteps();
    }

    public int getCurrentPiece() {
        checkCoreData();
        return currentState.getCurrentPiece();
    }

    public TinyGlue getGlueState() {
        return theGlueState;
    }

    public void drawObs(Observation tetrisObs) {
        System.out.println("STEP: " + theGlueState.getTotalSteps());
        int index = 0;
        for (int i = 0; i < currentState.getHeight(); i++) {
            for (int j = 0; j < currentState.getWidth(); j++) {
                index = i * currentState.getWidth() + j;
                System.out.print(tetrisObs.intArray[index]);
            }
            System.out.print("\n");
        }

    }
    
    public String message(String msg){
        String[] parsedMsg = msg.split(":");
        if (parsedMsg.length >= 2) {  
            if (parsedMsg[0].equals("SHOWREINF")) {
                showFeedback(Integer.parseInt(parsedMsg[1]));
                return "REINFSHOWN";
            }
        }
        return "";
    }

    public void showFeedback(int feedback){
        ((TetrisBlocksComponent)theBlockVisualizer).setCurrFeedback(feedback);
        ((TetrisScoreComponent)theTetrlaisScoreViz).refresh();
        try{
            //do what you want to do before sleeping
            Thread.currentThread().sleep(100);//sleep for 100 ms
            //do what you want to do after sleeping
        }
        catch(InterruptedException e){
            //If this thread was intrrupted by another thread
            System.out.println("Interrupted");
        }
        ((TetrisScoreComponent)theTetrlaisScoreViz).refresh();
    }

    public String getName() {
        return "Tetris 1.1 (DEV)";
    }
    
    /* @Override
    public String getName(){return "";} */
    
    @Override
    public Point getOverrideLocation(){
        return new Point(800,0);
    }

    @Override
    public Dimension getOverrideSize(){
        return new Dimension(500,800);
    }
    
    // uncomment to make the control box disappear
    public boolean wantsDynamicControls(){
        return false;
    }
}
