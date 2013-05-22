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

import java.util.Observable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Observer;

import org.rlcommunity.rlglue.codec.types.Reward_observation_action_terminal;

import rlVizLib.general.TinyGlue;
import rlVizLib.utilities.UtilityShop;
import rlVizLib.visualization.interfaces.AgentOnValueFunctionDataProvider;
import rlVizLib.visualization.interfaces.GlueStateProvider;

public class AgentOnValueFunctionVizComponent implements SelfUpdatingVizComponent, Observer {

    private VizComponentChangeListener theChangeListener;
    private AgentOnValueFunctionDataProvider dataProvider;
    private boolean enabled = true;

    public AgentOnValueFunctionVizComponent(AgentOnValueFunctionDataProvider dataProvider, TinyGlue theGlueState) {
        this.dataProvider = dataProvider;
        theGlueState.addObserver(this);
    }

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
        theChangeListener.vizComponentChanged(this);
    }

    private void enable() {
        enabled = true;
    }

    public void render(Graphics2D g) {
        if (!enabled) {
            Color myClearColor = new Color(0.0f, 0.0f, 0.0f, 0.0f);
            g.setColor(myClearColor);
            g.setBackground(myClearColor);
            g.clearRect(0, 0, 1, 1);
            return;
        }
        dataProvider.updateAgentState();
        g.setColor(Color.BLUE);

        double transX = UtilityShop.normalizeValue(dataProvider.getCurrentStateInDimension(0),
                dataProvider.getMinValueForDim(0),
                dataProvider.getMaxValueForDim(0));

        double transY = UtilityShop.normalizeValue(dataProvider.getCurrentStateInDimension(1),
                dataProvider.getMinValueForDim(1),
                dataProvider.getMaxValueForDim(1));


        Rectangle2D agentRect = new Rectangle2D.Double(transX-.01, transY-.01, .02, .02);
        g.fill(agentRect);
    }

    public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener) {
        this.theChangeListener = theChangeListener;
    }

    public void update(Observable o, Object theEvent) {
        if (theChangeListener != null) {
            theChangeListener.vizComponentChanged(this);
        }
    }
}
