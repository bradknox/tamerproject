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
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.imageio.ImageIO;
import org.rlcommunity.rlglue.codec.types.Observation;
import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.VizComponentChangeListener;

public class HotPlateVizComponent implements SelfUpdatingVizComponent, Observer {

    private HotPlateVisualizer theVizualizer = null;
    private Vector<Rectangle2D> safeZones=new Vector<Rectangle2D>();
    private VizComponentChangeListener theChangeListener;

    private Image hotplateImage=null;



    public HotPlateVizComponent(HotPlateVisualizer theVizualizer) {
        this.theVizualizer = theVizualizer;
        URL hotplateImageUrl = HotPlateVizComponent.class.getResource("/images/hotplate.jpg");
            try {
            hotplateImage = ImageIO.read(hotplateImageUrl);
        } catch (Exception ex) {
            System.err.println("ERROR: Problem getting hot plate image.");
        }

        theVizualizer.getTheGlueState().addObserver(this);


    }

    private void updateData(){
            safeZones=new Vector<Rectangle2D>();
            boolean signaledVersion=theVizualizer.isSignaled();
            int[] safeZoneID=theVizualizer.getSafeZone();
            //Assume 2D for now.

               if (!signaledVersion) {
                    safeZones.add(new Rectangle2D.Double(0,0,.05,.05));
                    safeZones.add(new Rectangle2D.Double(.95,0,.05,.05));
                    safeZones.add(new Rectangle2D.Double(0,.95,.05,.05));
                    safeZones.add(new Rectangle2D.Double(.95,.95,.05,.05));
               }else{

                    double xStart=0.0d;
                    if(safeZoneID[0]==1)
                        xStart=.95d;
                    double yStart=0.0d;
                    if(safeZoneID[1]==1)
                        yStart=.95d;
                    safeZones.add(new Rectangle2D.Double(xStart,yStart,.05,.05));
               }
    }
    public void render(Graphics2D g) {
        //Draw the background
        g.drawImage(hotplateImage, 0,0,1,1, null);

        //Draw the safezones
        g.setColor(Color.white);
        for (Rectangle2D thisSafeZone : safeZones) {
            g.fill(thisSafeZone);
        }


    }

    public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener) {
        this.theChangeListener = theChangeListener;
    }

    public void update(Observable o, Object arg) {
        //Only update on RL_start (glue produces an observation)
        if (arg instanceof Observation) {
            updateData();
            theChangeListener.vizComponentChanged(this);
        }

    }
}
