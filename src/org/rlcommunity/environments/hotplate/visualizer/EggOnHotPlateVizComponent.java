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
package org.rlcommunity.environments.hotplate.visualizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import javax.imageio.ImageIO;
import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.VizComponentChangeListener;

public class EggOnHotPlateVizComponent implements SelfUpdatingVizComponent, Observer {

    private HotPlateVisualizer mcv = null;
    private VizComponentChangeListener theChangeListener;
    private Image eggImage = null;

    public EggOnHotPlateVizComponent(HotPlateVisualizer mc) {
        this.mcv = mc;
        mc.getTheGlueState().addObserver(this);
        URL hotplateImageUrl = HotPlateVizComponent.class.getResource("/images/egg.png");
            try {
            eggImage = ImageIO.read(hotplateImageUrl);
            eggImage=eggImage.getScaledInstance(50,50,Image.SCALE_SMOOTH);
        } catch (Exception ex) {
            System.err.println("ERROR: Problem getting egg image.");
        }

    }

    public void render(Graphics2D g) {
        AffineTransform saveAT = g.getTransform();
        g.scale(.005, .005);

        g.setColor(Color.blue);

        double transX=mcv.getCurrentStateInDimension(0)*200.0d;
        double transY=mcv.getCurrentStateInDimension(1)*200.0d;
//        Rectangle2D theAgentRectangle=new Rectangle2D.Double(agentX-.025d, agentY-.025d, .05, .05);
  //      g.drawImage(eggImage, 0,0,1,1, null);

//            g.fill(theAgentRectangle);
        AffineTransform theTransform = AffineTransform.getTranslateInstance(transX - eggImage.getWidth(null) / 2.0d, transY - eggImage.getHeight(null) / 2.0d);
        //theTransform.concatenate(AffineTransform.getRotateInstance(theta, whichImageToDraw.getWidth(null) / 2, whichImageToDraw.getHeight(null) / 2));
        g.drawImage(eggImage, theTransform, null);

        g.setTransform(saveAT);

    }

    public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener) {
        this.theChangeListener = theChangeListener;
    }

    public void update(Observable o, Object arg) {
        if (theChangeListener != null) {
            theChangeListener.vizComponentChanged(this);
            mcv.updateAgentState(true);
        }
    }
}
