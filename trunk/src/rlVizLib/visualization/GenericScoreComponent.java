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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Observation_action;
import org.rlcommunity.rlglue.codec.types.Reward_observation_action_terminal;

import rlVizLib.visualization.interfaces.GlueStateProvider;

public class GenericScoreComponent implements SelfUpdatingVizComponent, Observer {

    private VizComponentChangeListener theChangeListener = null;
    int episodeNumber = 0;
    int timeStep = 1;
    long totalSteps = 1;
    double lastReward = Double.NaN;

    public GenericScoreComponent(GlueStateProvider theGlueStateProvider) {
        theGlueStateProvider.getTheGlueState().addObserver(this);
    }

    public void render(Graphics2D g) {
        DecimalFormat myFormatter = new DecimalFormat("##.###");
        //This is some hacky stuff, someone better than me should clean it up
        Font f = new Font("Verdana", 0, 8);
        g.setFont(f);
        //SET COLOR
        g.setColor(Color.RED);
        //DRAW STRING
        AffineTransform saveAT = g.getTransform();
        g.scale(.005, .005);
//        TinyGlue theGlueState = theGlueStateProvider.getTheGlueState();

        //used for rounding
        String theRewardString;
        double preRound;
        preRound = lastReward;

        if (Double.isNaN(preRound)) {
            theRewardString = "None";
        } else {
            theRewardString = myFormatter.format(preRound);
        }
        g.drawString("E/S/T/R: " + episodeNumber + "/" + timeStep + "/" + totalSteps + "/" + theRewardString, 0.0f, 10.0f);

        g.setTransform(saveAT);
    }

    /**
     * We are an observer of events thrown by tiny glue.  Those events mean
     * something has changed.
     * @param o
     * @param theEvent
     */
    public void update(Observable o, Object theEvent) {
        if (theEvent instanceof Observation) {
            episodeNumber++;
            timeStep = 1;
            lastReward = Double.NaN;
        }
        if (theEvent instanceof Reward_observation_action_terminal) {
            Reward_observation_action_terminal ROAT = (Reward_observation_action_terminal) theEvent;
            lastReward = ROAT.r;
            timeStep++;
            totalSteps++;
        }
        if (theChangeListener != null) {
            theChangeListener.vizComponentChanged(this);
        }
    }

    public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener) {
        this.theChangeListener = theChangeListener;
    }
}
