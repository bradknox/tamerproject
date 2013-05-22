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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.rlcommunity.rlviz.app.frames.VisualizerVizFrame;

import rlVizLib.messaging.agent.AgentGraphicRequest;
import rlVizLib.messaging.environment.EnvGraphicRequest;
import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.VisualizerPanelInterface;

public class VisualizerPanel extends JPanel implements ComponentListener, VisualizerPanelInterface, visualizerLoadListener {

    private static final long serialVersionUID = 1L;
    public static boolean vizLoaded = false;
    AbstractVisualizer theVisualizer = null;
    //Going to use this timer so that all of the cascase resizing that happens is on a delay so that window resizing is more responsive
    Timer resizeChildrenTimer = null;
    BufferedImage defaultImage = null;
    
    final int VisualizerType;

    public VisualizerPanel(Dimension initialSize, int VisualizerType) {
        super();
        this.VisualizerType=VisualizerType;
        this.setSize((int) initialSize.getWidth(), (int) initialSize.getHeight());
        this.setBackground(Color.white);
        addComponentListener(this);

    }

    
    private Image getDefaultImageTransformed(){
        int imageW=defaultImage.getWidth();
        int imageH=defaultImage.getHeight();
        
        int panelWidth=getWidth();
        int panelHeight=getHeight();
        double widthRatio=(double)panelWidth/(double)imageW;
        double heightRatio=(double)panelHeight/(double)imageH;
        
        double theRatio=widthRatio;
        
        if(heightRatio<widthRatio){
            theRatio=heightRatio;
        }
        return defaultImage.getScaledInstance((int)(imageW*theRatio), (int)(imageH*theRatio), Image.SCALE_SMOOTH);
    }
            
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;
        if (theVisualizer != null) {
            if (RLGlueLogic.getGlobalGlueLogic().getGlueState().getTimeStep() == 0) {
                if (defaultImage != null) {
                    Image newImage=getDefaultImageTransformed();
                    double yOffset=getHeight()-newImage.getHeight(null);
                    double xOffset=.5*(getWidth()-newImage.getWidth(null));
                    g2.drawImage(newImage,AffineTransform.getTranslateInstance(xOffset, yOffset),null);

                }
            }
         else {
            g2.drawImage(theVisualizer.getLatestImage(), 0, 0, this);
        }
        }

    }

    public void componentHidden(ComponentEvent arg0) {
        // TODO Auto-generated method stub
    }

    public void componentMoved(ComponentEvent arg0) {
        // TODO Auto-generated method stub
    }

    public void componentResized(ComponentEvent arg0) {
        resizeChildrenTimer = new Timer();
        resizeChildrenTimer.schedule(new TimerTask() {
            public void run() {
                tellChildrenToResize();
            }
        }, 100);

    }

    public void tellChildrenToResize() {
        if (theVisualizer != null) {
            theVisualizer.notifyPanelSizeChange();
        }
    }

    public void componentShown(ComponentEvent arg0) {
        // TODO Auto-generated method stub
    }

    public void startVisualizing() {
        if (theVisualizer != null) {
            theVisualizer.startVisualizing();
        }
    }

    public void stopVisualizing() {
        if (theVisualizer != null) {
            theVisualizer.stopVisualizing();
        }
    }

    public void receiveNotificationVizChanged() {
        this.repaint();
    }

    public void notifyVisualizerLoaded(AbstractVisualizer theNewVisualizer) {
        if (this.theVisualizer != null) {
            this.theVisualizer.stopVisualizing();
        }
        if (theNewVisualizer != null) {
            this.theVisualizer = theNewVisualizer;

            this.setNamedBorder();
            theVisualizer.setParentPanel(this);
            theVisualizer.notifyPanelSizeChange();
            
            //Try to get an image
            if (VisualizerType == VisualizerVizFrame.AgentVisualizerType) {
                AgentGraphicRequest.Response graphicResponse = AgentGraphicRequest.Execute();
                defaultImage = graphicResponse.getImage();
            }
            if(VisualizerType==VisualizerVizFrame.EnvVisualizerType){
            EnvGraphicRequest.Response graphicResponse = EnvGraphicRequest.Execute();
            defaultImage = graphicResponse.getImage();
            }
            vizLoaded = true;

            //This is the biggest hack of them all.  Something was happening where
            //without a resize I was getting ghosting of old visualizers that I
            //could not get rid of.  Resizing fixed it.

            Component theParent=getParent();
            while(true){
                if(theParent instanceof JFrame){
                    break;
                }
                theParent=theParent.getParent();
            }
            JFrame theWindow=(JFrame)theParent;
            Dimension currentSize=theWindow.getSize();
            Dimension newSize=new Dimension(currentSize.width+1,currentSize.height+1);
            theWindow.setSize(newSize);
        } else {
            System.err.println("Failed To Load a Visualizer.");
        }
    }

    public void notifyVisualizerUnLoaded() {
        theVisualizer = null;
    }

    private void setNamedBorder() {
        TitledBorder titled = null;
        Border loweredetched = null;
        loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        titled = BorderFactory.createTitledBorder(loweredetched, theVisualizer.getName());
        setBorder(titled);
    }
}
