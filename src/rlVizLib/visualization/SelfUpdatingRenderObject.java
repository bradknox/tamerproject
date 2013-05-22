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

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This used to be the workhorse of redrawing images, now that functionality 
 * has moved up to RenderObject.  This class now gets to focus on sleeping
 * and polling :)
 * @author btanner
 */
public class SelfUpdatingRenderObject extends RenderObject implements VizComponentChangeListener {

    volatile boolean shouldDie = false;
    private SelfUpdatingVizComponent theComponent = null;

    public SelfUpdatingRenderObject(Dimension currentVisualizerPanelSize, SelfUpdatingVizComponent theComponent, ImageAggregator theBoss) {
        super(currentVisualizerPanelSize, theBoss);
        this.theComponent = theComponent;
        theComponent.setVizComponentChangeListener(this);
    }

    public void kill() {
        shouldDie = true;
        synchronized (theComponent) {
            theComponent.notify();
        }
    }

    public synchronized void vizComponentChanged(BasicVizComponent theComponent) {
        synchronized (theComponent) {
            theComponent.notify();
        }
    }

    public void run() {

        while (!shouldDie) {
            try {
				/*
				 * Draw every 60 seconds if no updates are coming (usually via vizComponentChanged()).
				 */
                synchronized (theComponent) {
                    theComponent.wait(60000);
                }
                redrawImages();

            } catch (InterruptedException ex) {
                Logger.getLogger(SelfUpdatingRenderObject.class.getName()).log(Level.SEVERE, null, ex);
            }
        }//end of the while loop
        //Now that we've died, can reset the shouldDie flag so that we can easily be restarted
        shouldDie = false;
    }

    @Override
    public BasicVizComponent getVizComponent() {
        return theComponent;
    }

    @Override
    void initiateForcedRedraw() {
        if (theComponent != null) {
            synchronized (theComponent) {
                theComponent.notify();
            }
        }

    }
}
