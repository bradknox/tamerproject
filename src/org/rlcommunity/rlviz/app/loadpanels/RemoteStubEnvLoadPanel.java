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


package org.rlcommunity.rlviz.app.loadpanels;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.rlcommunity.rlviz.app.RLGlueLogic;

import rlVizLib.messaging.environmentShell.TaskSpecPayload;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class RemoteStubEnvLoadPanel implements EnvLoadPanelInterface {

    JPanel thePanel = null;
    RLGlueLogic theGlueConnection = null;

    public RemoteStubEnvLoadPanel(RLGlueLogic theGlueConnection) {
        this.theGlueConnection = theGlueConnection;
        thePanel = new JPanel();
        //
                //Setup the border for the publicPanel 
                //
        TitledBorder titled = null;
        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        titled = BorderFactory.createTitledBorder(loweredetched, "Dynamic Environment Loading Disabled");
        titled.setTitleJustification(TitledBorder.CENTER);
        thePanel.setBorder(titled);
        thePanel.add(new JLabel("Please run a separate environment process"));

    }

    public JPanel getPanel() {
        return thePanel;
    }

    public boolean load() {
        //This does nothing because this panel is a stub
        theGlueConnection.loadEnvironmentVisualizer();
        return true; // might need to change later if it causes bugs
    }

    public void setEnabled(boolean b) {
        //This is important
        thePanel.setEnabled(b);
    }

    public void updateList() {
    //This does nothing because this panel is a stub
    }

    public boolean canLoad() {
        return true;
    }

    public TaskSpecPayload getTaskSpecPayload() {
        return TaskSpecPayload.makeUnsupportedPayload();
    }
}
