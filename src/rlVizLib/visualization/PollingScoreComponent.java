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

import rlVizLib.general.TinyGlue;
import rlVizLib.visualization.interfaces.GlueStateProvider;

/**
 * @deprecated This was the old Score Component, it has been renamed because I want to 
 * make the update to the new one as transparent as possible.  The new one is self-updating.
 * @author btanner
 */

public class PollingScoreComponent implements PollingVizComponent{
        private GlueStateProvider theGlueStateProvider = null;
        private int lastUpdateTimeStep=-1;
        
        public PollingScoreComponent(GlueStateProvider theVis){
                this.theGlueStateProvider = theVis;
        }

        public void render(Graphics2D g) {
            DecimalFormat myFormatter = new DecimalFormat("##.###");
                //This is some hacky stuff, someone better than me should clean it up
                Font f = new Font("Verdana",0,8);     
                g.setFont(f);
            //SET COLOR
            g.setColor(Color.RED);
            //DRAW STRING
            AffineTransform saveAT = g.getTransform();
            g.scale(.005, .005);
            TinyGlue theGlueState=theGlueStateProvider.getTheGlueState();
            
            //used for rounding
            String theRewardString;
            double preRound;
                preRound = theGlueState.getLastReward();

            if(Double.isNaN(preRound)){
                theRewardString = "None";
            }
            else
                theRewardString = myFormatter.format(preRound);

            g.drawString("E/S/T/R: " +theGlueState.getEpisodeNumber()+"/"+theGlueState.getTimeStep()+"/"+theGlueState.getTotalSteps()+"/"+theRewardString,0.0f, 10.0f);
            g.setTransform(saveAT);
        }

        public boolean update() {
                //Only draw if we're on a new time step
                int currentTimeStep=theGlueStateProvider.getTheGlueState().getTotalSteps();
                if(currentTimeStep!=lastUpdateTimeStep){
                        lastUpdateTimeStep=currentTimeStep;
                        return true;
                }
                return false;
        }
        
        
}



