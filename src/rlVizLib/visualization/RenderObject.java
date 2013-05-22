/*
 * Copyright 2008 Brian Tanner
 * http://bt-recordbook.googlecode.com/
 * brian@tannerpages.com
 * http://brian.tannerpages.com
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package rlVizLib.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 *  This is a component of a final image.
 * @author Brian Tanner
 */
public abstract class RenderObject implements Runnable{

    private Dimension mySize;
    private BufferedImage prodImage = null;
    private ImageAggregator theBoss;
    private AffineTransform theScaleTransform = null;
    private BufferedImage workImage = null;

    public RenderObject(Dimension mySize, ImageAggregator theBoss) {
        this.mySize=mySize;
        this.theBoss=theBoss;
        resizeImages();
    }

    public final BufferedImage getProductionImage() {
        return prodImage;
    }

    public abstract void kill();

    public void receiveSizeChange(Dimension newPanelSize) {
        mySize = newPanelSize;
        resizeImages();
    }
    
    
    protected void redrawImages() {
                Graphics2D g = workImage.createGraphics();

                //Set the scaling transform
                AffineTransform currentTransform = g.getTransform();
                currentTransform.concatenate(theScaleTransform);
                g.setTransform(currentTransform);

                //Clear the screen to transparent
                Color myClearColor = new Color(0.0f, 0.0f, 0.0f, 0.0f);
                g.setColor(myClearColor);
                g.setBackground(myClearColor);
                g.clearRect(0, 0, 1, 1);

                getVizComponent().render(g);

                BufferedImage tmpImage = prodImage;
                prodImage = workImage;
                workImage = tmpImage;

                theBoss.receiveNotification();
    }

    abstract BasicVizComponent getVizComponent();
    abstract void initiateForcedRedraw();

   
    private void resizeImages() {
        workImage = new BufferedImage((int) mySize.getWidth(), (int) mySize.getHeight(), BufferedImage.TYPE_INT_ARGB);
        //Set the transform on the image so we can draw everything in [0,1]
        theScaleTransform = new AffineTransform();
        theScaleTransform.scale(mySize.getWidth(), mySize.getHeight());

        prodImage = new BufferedImage((int) mySize.getWidth(), (int) mySize.getHeight(), BufferedImage.TYPE_INT_ARGB);
        initiateForcedRedraw();
    }
}
