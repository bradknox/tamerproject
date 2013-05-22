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
package org.rlcommunity.rlviz.app;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.rlcommunity.rlviz.app.loadpanels.DynamicAgentLoadPanel;
import org.rlcommunity.rlviz.app.loadpanels.DynamicEnvLoadPanel;
import org.rlcommunity.rlviz.app.loadpanels.RemoteStubAgentLoadPanel;
import org.rlcommunity.rlviz.app.loadpanels.RemoteStubEnvLoadPanel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.rlcommunity.rlviz.app.loadpanels.AgentLoadPanelInterface;
import org.rlcommunity.rlviz.app.loadpanels.EnvLoadPanelInterface;
import org.rlcommunity.rlviz.settings.RLVizSettings;

import rlVizLib.messaging.agentShell.TaskSpecResponsePayload;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;

public class RLControlPanel extends JPanel implements ActionListener, ChangeListener {

    /**
     * 
     */
    RLGlueLogic theGlueConnection = null;
    private static final long serialVersionUID = 1L;
    JButton bLoad = null;
    JButton bUnLoad = null;
    JButton bStart = null;
    JButton bStop = null;
    JButton bStep = null;
    JButton bCompatibility;
    JSlider sleepTimeBetweenSteps = null;
    JLabel simSpeedLabel = new JLabel("Simulation Speed (left is faster)");
    EnvLoadPanelInterface envLoadPanel = null;
    AgentLoadPanelInterface agentLoadPanel = null;
//	JPanel LoaderPanel=null;

    public RLControlPanel(RLGlueLogic theGlueConnection) {
        super();
        this.theGlueConnection = theGlueConnection;


        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        if (RLVizSettings.getBooleanSetting("list-environments")) {
            envLoadPanel = new DynamicEnvLoadPanel(theGlueConnection);
        } else {
            envLoadPanel = new RemoteStubEnvLoadPanel(theGlueConnection);
        }
        if (RLVizSettings.getBooleanSetting("list-agents")) {
            agentLoadPanel = new DynamicAgentLoadPanel(theGlueConnection);
        } else {
            agentLoadPanel = new RemoteStubAgentLoadPanel(theGlueConnection);
        }
        bLoad = new JButton("Load Experiment");
        bLoad.addActionListener(this);

        bUnLoad = new JButton("UnLoad Experiment");
        bUnLoad.addActionListener(this);

        bStart = new JButton("Start");
        bStart.addActionListener(this);

        bStop = new JButton("Stop");
        bStop.addActionListener(this);

        bStep = new JButton("Step");
        bStep.addActionListener(this);

        JPanel experimentalControlsPanel = new JPanel();
        experimentalControlsPanel.setLayout(new BoxLayout(experimentalControlsPanel, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(bLoad);
        buttonPanel.add(bUnLoad);
        buttonPanel.add(bStart);
        buttonPanel.add(bStop);
        buttonPanel.add(bStep);

        //
        //Setup the border for the buttons and speed slider
        //
        TitledBorder titled = null;
        Border loweredetched = null;
        loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        titled = BorderFactory.createTitledBorder(loweredetched, "Experimental Controls");
        experimentalControlsPanel.setBorder(titled);

        experimentalControlsPanel.add(buttonPanel);

        sleepTimeBetweenSteps = new JSlider(JSlider.HORIZONTAL, 1, 500, RLVizSettings.getIntSetting("default-speed"));
        sleepTimeBetweenSteps.addChangeListener(this);


        //Turn on labels at major tick marks.
        sleepTimeBetweenSteps.setMajorTickSpacing(100);
        sleepTimeBetweenSteps.setMinorTickSpacing(25);
        sleepTimeBetweenSteps.setPaintTicks(true);
        sleepTimeBetweenSteps.setPaintLabels(true);

        simSpeedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        experimentalControlsPanel.add(simSpeedLabel);
        experimentalControlsPanel.add(sleepTimeBetweenSteps);

        add(experimentalControlsPanel);
        add(Box.createRigidArea(new java.awt.Dimension(0, 10)));

        JPanel LoaderPanel = null;

        LoaderPanel = new JPanel();

        LoaderPanel.setLayout(new BoxLayout(LoaderPanel, BoxLayout.X_AXIS));

        LoaderPanel.add(envLoadPanel.getPanel());
        LoaderPanel.add(agentLoadPanel.getPanel());
        JPanel LoaderSuperPanel = new JPanel();
        LoaderSuperPanel.setLayout(new BoxLayout(LoaderSuperPanel, BoxLayout.Y_AXIS));

        bCompatibility = new JButton("Check Compatibility");
        bCompatibility.setEnabled(false);
        bCompatibility.addActionListener(this);
        bCompatibility.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        LoaderSuperPanel.add(bCompatibility);
        //Only make the compatibility button if we are dynamically loading agents and envs
        if (RLVizSettings.getBooleanSetting("list-environments") || RLVizSettings.getBooleanSetting("list-agents")) {
            bCompatibility.setEnabled(true);
        }

        LoaderSuperPanel.add(LoaderPanel);
        //
        //Setup the border for the loader panels
        //
        titled = BorderFactory.createTitledBorder(loweredetched, "Dynamic Loading");
        titled.setTitleJustification(TitledBorder.CENTER);

        LoaderSuperPanel.setBorder(titled);


        add(LoaderSuperPanel);


        envLoadPanel.updateList();
        agentLoadPanel.updateList();

        setDefaultEnabling();
    }

    public void actionPerformed(ActionEvent theEvent) {
        if (theEvent.getSource() == bLoad) {
            handleLoadClick();
        }
        if (theEvent.getSource() == bUnLoad) {
            handleUnLoadClick();
        }
        if (theEvent.getSource() == bStart) {
            handleStartClick();
        }
        if (theEvent.getSource() == bStop) {
            handleStopClick();
        }
        if (theEvent.getSource() == bStep) {
            handleStepClick();
        }
        if (theEvent.getSource() == bCompatibility) {
            handleCompatibilityClick();
        }
    }

    private void showMessage(boolean success, String theMessage) {
        if (success) {
            JOptionPane.showMessageDialog(null, theMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, theMessage, "Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Ugly hack method, should return false only if compatibility check 
     * was available and false.
     * @return
     */
    private boolean checkCompatibility() {
        TaskSpecPayload theTSP = envLoadPanel.getTaskSpecPayload();
        //If its not supported, return true cause we can't tell
        if (!theTSP.getSupported()) {
            return true;
        }
        //If there is an error with env, return false
        if (theTSP.getErrorStatus()) {
            return false;
        }

        TaskSpecResponsePayload theTSRP = agentLoadPanel.getTaskSpecPayloadResponse(theTSP);
        //if the agent is not supported, return true cause we can't tell
        if (!theTSRP.getSupported()) {
            return true;
        }
        return (!theTSRP.getErrorStatus());
    }

    private void handleCompatibilityClick() {
        TaskSpecPayload theTSP = envLoadPanel.getTaskSpecPayload();
        if (!theTSP.getSupported()) {
            showMessage(false, "Could not do compatibility because environment or related mechanism does not support it.");
            return;
        }
        if (theTSP.getSupported() && theTSP.getErrorStatus()) {
            showMessage(false, "Could not do compatibility check because of environment\n" + theTSP.getErrorMessage());
            return;
        }

        TaskSpecResponsePayload theTSRP = agentLoadPanel.getTaskSpecPayloadResponse(theTSP);
        if (!theTSRP.getSupported()) {
            showMessage(false, "Could not do compatibility check because the agent or related mechanism does not support it.");
            return;
        }
        if (theTSRP.getSupported() && !theTSRP.getErrorStatus()) {
            showMessage(true, "This agent is compatible with this environment.");
            return;
        } else {
            showMessage(false, "This agent is not compatible with this environment : " + theTSRP.getErrorMessage());
            return;
        }
    }

//These are a couple of API methods to let other invoke commands on the control panel
//It's a bit hacky.


    public void doLoad(){
        handleLoadClick();
    }
    public void doUnload(){
        handleUnLoadClick();
    }

    /**
     *
     * @param shouldEnable
     */
    private void setSpeedEnabling(boolean shouldEnable){
        sleepTimeBetweenSteps.setEnabled(shouldEnable && !RLVizSettings.getBooleanSetting("speed-frozen"));
        simSpeedLabel.setEnabled(sleepTimeBetweenSteps.isEnabled());
    }

    private void setUnloadEnabling(boolean shouldEnable){
        bUnLoad.setEnabled(shouldEnable && !RLVizSettings.getBooleanSetting("unload-disabled"));
    }

    private void setDefaultEnabling() {
        envLoadPanel.setEnabled(true);
        agentLoadPanel.setEnabled(true);

        bLoad.setEnabled(envLoadPanel.canLoad() && agentLoadPanel.canLoad());
        if (bCompatibility == null) {
            System.out.println("bCompat is null.");
        }
        bCompatibility.setEnabled(bLoad.isEnabled());
        setUnloadEnabling(false);
        bStart.setEnabled(false);
        bStop.setEnabled(false);
        bStep.setEnabled(false);
        setSpeedEnabling(true);
    }

    void handleUnLoadClick() {
        envLoadPanel.setEnabled(true);
        agentLoadPanel.setEnabled(true);


        bLoad.setEnabled(true);
        bCompatibility.setEnabled(true);
        setUnloadEnabling(false);
        bStart.setEnabled(false);
        bStop.setEnabled(false);
        bStep.setEnabled(false);
        setSpeedEnabling(false);

        theGlueConnection.unloadExperiment();
    }


    private void handleLoadClick() {
        bLoad.setEnabled(false);
        boolean compatible = checkCompatibility();
        if (!compatible) {
            JOptionPane.showMessageDialog(null, "This agent and environment are not compatible.\nClick \"Check Compatibility\" for more info.", "Failed", JOptionPane.ERROR_MESSAGE);
            bLoad.setEnabled(true);
            return;
        }
        //This takes care of sending the messages to the shells to load things
        //Or in the case where connections are remote, this will do nothing basically.
        theGlueConnection.startNewExperiment();

        boolean envLoadedOk = envLoadPanel.load();
        boolean agentLoadedOk = agentLoadPanel.load();//agent


        if (!envLoadedOk || !agentLoadedOk) {
            if (!envLoadedOk) {
                System.err.println("Environment didn't load properly\n");
            }
            if (!agentLoadedOk) {
                System.err.println("Agent didn't load properly\n");
            }
            return;
        }

        theGlueConnection.startVisualizers();
        //Not sure why this is necessary but it seems to be.
        theGlueConnection.RL_init();

        envLoadPanel.setEnabled(false);
        bCompatibility.setEnabled(false);
        agentLoadPanel.setEnabled(false);



        bLoad.setEnabled(false);
        setUnloadEnabling(true);
        bStart.setEnabled(true);
        bStop.setEnabled(false);
        bStep.setEnabled(true);
        setSpeedEnabling(true);
    }

    private void handleStartClick() {
        bLoad.setEnabled(false);
        bCompatibility.setEnabled(false);
        setUnloadEnabling(false);
        bStart.setEnabled(false);
        bStop.setEnabled(true);
        bStep.setEnabled(false);
        setSpeedEnabling(true);


        int stepDelay = sleepTimeBetweenSteps.getValue();
        theGlueConnection.setNewStepDelay(stepDelay);
        theGlueConnection.start();
    }

    private void handleStepClick() {
        envLoadPanel.setEnabled(false);
        agentLoadPanel.setEnabled(false);

        bLoad.setEnabled(false);
        bCompatibility.setEnabled(false);
        setUnloadEnabling(true);
        bStart.setEnabled(true);
        bStop.setEnabled(false);
        bStep.setEnabled(true);
        setSpeedEnabling(true);

        theGlueConnection.step();
    }

    private void handleStopClick() {
        envLoadPanel.setEnabled(false);
        agentLoadPanel.setEnabled(false);

        bLoad.setEnabled(false);
        bCompatibility.setEnabled(false);

        setUnloadEnabling(true);
        bStart.setEnabled(true);
        bStop.setEnabled(false);
        bStep.setEnabled(true);
        setSpeedEnabling(true);

        theGlueConnection.stop();

    }

    public void stateChanged(ChangeEvent sliderChangeEvent) {
        JSlider source = (JSlider) sliderChangeEvent.getSource();
        int theValue = source.getValue();

        if (source == sleepTimeBetweenSteps) {
            if (!source.getValueIsAdjusting()) {
                theGlueConnection.setNewStepDelay(theValue);
            }
        }
    }
}
