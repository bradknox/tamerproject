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
package org.rlcommunity.rlviz.app.frames;

import java.awt.Color;
import org.rlcommunity.rlviz.app.VisualizerPanel;
import org.rlcommunity.rlviz.app.RLControlPanel;
import org.rlcommunity.rlviz.app.RLGlueLogic;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import org.rlcommunity.rlviz.settings.RLVizSettings;

public class RLVizFrame extends GenericVizFrame {

    //Components
    VisualizerPanel ePanel = null;
    VisualizerPanel aPanel = null;
    RLGlueLogic theGlueConnection = null;
    static String programName = "RLVizApp";
    private static final long serialVersionUID = 1L;
    private final boolean useEnvVisualizer;
    private final boolean useAgentVisualizer;

    public RLVizFrame() {
        this(true, false);
    }

    public void setEnvSizeAndLocation() {
        int panelWidth = 800;
        int panelHeight = 800;

        if (useEnvVisualizer && useAgentVisualizer) {
            panelHeight /= 2;
        }

        int startX = this.getWidth() + 30;

        if (useEnvVisualizer) {
            envVizFrame.setSize(new Dimension(panelWidth, panelHeight));
            envVizFrame.setLocation(startX, 10);
        }
    }

    public void setAgentSizeAndLocation() {
        int panelWidth = 800;
        int panelHeight = 800;
        if (useEnvVisualizer && useAgentVisualizer) {
            panelHeight /= 2;
        }

        int startX = this.getWidth() + 30;

        if (useAgentVisualizer) {
            agentVizFrame.setSize(new Dimension(panelWidth, panelHeight));

            if (!useEnvVisualizer) {
                agentVizFrame.setLocation(startX, 10);
            } else {
                agentVizFrame.setLocation(startX, panelHeight + 30);
            }
        }

    }

    public RLVizFrame(boolean useEnvVisualizer, boolean useAgentVisualizer) {
        super();
        this.useEnvVisualizer = useEnvVisualizer;
        this.useAgentVisualizer = useAgentVisualizer;

        theGlueConnection = RLGlueLogic.getGlobalGlueLogic();
        setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));



        final RLControlPanel controlPanel = new RLControlPanel(theGlueConnection);
        getContentPane().add(controlPanel);
        pack();
        int panelWidth = 800;
        int panelHeight = 800;
        if (useEnvVisualizer && useAgentVisualizer) {
            panelHeight /= 2;
        }
        if (useEnvVisualizer) {
            envVizFrame = new EnvVisualizerFrame(new Dimension(panelWidth, panelHeight));
        }
        if (useAgentVisualizer) {
            agentVizFrame = new AgentVisualizerFrame(new Dimension(panelWidth, panelHeight));
        }

        setEnvSizeAndLocation();
        setAgentSizeAndLocation();

        setFrames(this, envVizFrame, agentVizFrame);
        makeMenus();

        if (envVizFrame != null) {
            envVizFrame.setFrames(this, envVizFrame, agentVizFrame);
            envVizFrame.makeMenus();
            envVizFrame.setVisible(false);
        }
        if (agentVizFrame != null) {
            agentVizFrame.setFrames(this, envVizFrame, agentVizFrame);
            agentVizFrame.makeMenus();
            agentVizFrame.setVisible(false);
        }

//        if (!useAgentVisualizer && !useEnvVisualizer) {
        setLocation(10, 10);
//        } else {
//            setLocation(panelWidth + 20, 10);
//        }





        pack();
        setVisible(true);
        this.setBackground(Color.white);

        //Make sure we exit if they close the window
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle(programName);


        if (RLVizSettings.getBooleanSetting("autoload")) {
            new Thread(new Runnable() {

                public void run() {
                    controlPanel.doLoad();
                }
            }).start();
        }
    }
}
