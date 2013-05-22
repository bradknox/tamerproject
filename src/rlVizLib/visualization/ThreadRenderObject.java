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

/**
 * This used to be the workhorse of redrawing images, now that functionality 
 * has moved up to RenderObject.  This class now gets to focus on sleeping
 * and polling :)
 * @author btanner
 */
public class ThreadRenderObject extends RenderObject {

    private PollingVizComponent theComponent = null;
    volatile boolean shouldDie = false;
    int defaultSleepTime = 50;
    private volatile boolean forced=false;

    public ThreadRenderObject(Dimension currentVisualizerPanelSize, PollingVizComponent theComponent, ImageAggregator theBoss) {
        super(currentVisualizerPanelSize, theBoss);
        this.theComponent = theComponent;
    }

    public void kill() {
        shouldDie = true;
    }

    public void run() {

        while (!shouldDie) {
            if (theComponent.update()||forced) {
                forced=false;
                redrawImages();
                try {
                    Thread.sleep(defaultSleepTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }//end of the while loop
        //Now that we've died, can reset the shouldDie flag so that we can easily be restarted
        shouldDie = false;
    }

    @Override
    BasicVizComponent getVizComponent() {
        return theComponent;
    }

    @Override
    void initiateForcedRedraw() {
        forced=true;
    }
}
