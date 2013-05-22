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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Observable;

public abstract class AbstractVisualizer extends Observable implements ImageAggregator {

    private BufferedImage productionEnvImage = null;
    private BufferedImage bufferEnvImage = null;
    private VisualizerPanelInterface parentPanel = null;
    private Vector<RenderObject> theRenderObjects = new Vector<RenderObject>();
    private Vector<Thread> theThreads = new Vector<Thread>();
    private Vector<Point2D> positions = new Vector<Point2D>();
    private Vector<Point2D> sizes = new Vector<Point2D>();
    volatile boolean currentlyRunning = false;

	public boolean verbose = false;

    public void setParentPanel(VisualizerPanelInterface parentPanel) {
		System.out.println("Setting parentPanel to " + parentPanel.getClass().getSimpleName());
        this.parentPanel = parentPanel;
    }

    public String message(String theStringMessage){
        return "By default: "+this.getClass().getName()+" does not respond to messages.  The concrete class below does not override the message method.";
    }

    public void notifyPanelSizeChange() {
        resizeImages();
    }

	public Vector<RenderObject> getRenderObjects() {
		return theRenderObjects;
	}

    /**
     * This method was added for Brad Knox on Nov 2009 so that he could make a
     * visualizer that specified a custom size.  This is dangerous because if you
     * override the size or location of the visualizer frame, you have to do it
     * in both the agent and environment, otherwise they might overlap or look
     * otherwise dumb.  You've been warned.
     * @return
     */
    public Dimension getOverrideSize(){
        return null;
    }

    /**
     * This is also for Brad Knox, Nov 2009.  If you override this and return
     * false then there will be no genericControlTarget for you to put your
     * controls on.  You'll get the space back for visualization.
     * @return
     */
    public boolean wantsDynamicControls(){
        return true;
    }

    /**
     * This method was added for Brad Knox on Nov 2009 so that he could make a
     * visualizer that specified a custom location.  This is dangerous because if you
     * override the size or location of the visualizer frame, you have to do it
     * in both the agent and environment, otherwise they might overlap or look
     * otherwise dumb.  You've been warned.
     * @return
     */
    public Point getOverrideLocation(){
        return null;
    }

    private synchronized void resizeImages() {
   		Dimension currentVisualizerPanelSize = parentPanel.getSize();

		// BK: create correctly sized images but don't redraw yet	   
		productionEnvImage = new BufferedImage((int) currentVisualizerPanelSize.getWidth(), (int) currentVisualizerPanelSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
        bufferEnvImage = new BufferedImage((int) currentVisualizerPanelSize.getWidth(), (int) currentVisualizerPanelSize.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < theRenderObjects.size(); i++) {
            theRenderObjects.get(i).receiveSizeChange(makeSizeForVizComponent(i));
        }

    }

    public AbstractVisualizer() {
    }

    public BufferedImage getLatestImage() {
        return productionEnvImage;
    }


    /*
     * This method updates each BasicVizComponent (each within a RenderObject)
     * and then redraws productionEnvImage with the updated component images.
     */
    public void forceRedraw() {
        for (int i = 0; i < theRenderObjects.size(); i++) {
            theRenderObjects.get(i).initiateForcedRedraw();
        }
    }


    public synchronized void redrawCurrentImage() {
		if (verbose){
			System.out.println("redrawCurrentImage called");
			System.out.flush();
		}
        synchronized (redrawLock) {
            scheduled = false;
            drawing = true;
        }
		if (verbose){
			System.out.println("redraw lock taken");
			System.out.flush();
		}

        Graphics2D G = bufferEnvImage.createGraphics();
        Color myClearColor = new Color(0.0f, 0.0f, 0.0f, 0.0f);
        G.setColor(myClearColor);
        G.setBackground(myClearColor);
        G.clearRect(0, 0, bufferEnvImage.getWidth(), bufferEnvImage.getHeight());


		/*
		 * Cycle through SelfUpdatingVizComponents (e.g., CarOnMountainComponent for mountain car) and 
		 * call getProductionImage(), which calls render()(?).
		 */
        for (int i = 0; i < theRenderObjects.size(); i++) {
            RenderObject thisRunner = theRenderObjects.get(i);
            Dimension position = makeLocationForVizComponent(i);
            G.drawImage(thisRunner.getProductionImage(), position.width, position.height, null);
        }

		// Make generated image official as productionEnvImage
        BufferedImage tmpImage = productionEnvImage;
        productionEnvImage = bufferEnvImage;
        bufferEnvImage = tmpImage;

        synchronized (redrawLock) {
            lastDrawTime = System.currentTimeMillis();
            drawing = false;
        }

		if (verbose){
			System.out.println("redraw finished. parentPanel: " + parentPanel.getClass().getSimpleName());
			System.out.flush();
		}
        parentPanel.receiveNotificationVizChanged();
		super.notifyObservers(productionEnvImage);
    }
    public long lastDrawTime = 0;
    volatile boolean scheduled = false;
    boolean drawing = false;
    Object redrawLock = new Object();
    volatile Timer redrawTimer = new Timer();

    public void receiveNotification() {
        final int timeBetweenDraw = 25;
        //Either draw now or schedule a drawing

        synchronized (redrawLock) {
            long now = System.currentTimeMillis();
            //If we're planning on drawing but haven't started, relax
            if (scheduled) {
                return;
            }
                scheduled = true;
            //If we're not scheduled but currently drawing, schedule at the interval
            if (drawing) {
                redrawTimer.schedule(new TimerTask() {

                    public void run() {
				    	Thread.currentThread().setName("RedrawThread-AlreadyDrawing-AbstractVisualizer-Timer");
                        redrawCurrentImage();
                    }
                }, timeBetweenDraw);

                return;
            }
            //Ok so in this case, we're !scheduled && !drawing
            //Schedule a drawing in the shorter term
            long timeSinceDraw = now - lastDrawTime;
            long timeTillDraw = timeBetweenDraw - timeSinceDraw;
            if (timeTillDraw < 0) {
                timeTillDraw = 0;
            }

            redrawTimer.schedule(new TimerTask() {

                public void run() {
                	Thread.currentThread().setName("RedrawThread-NotDrawing-AbstractVisualizer-Timer");
                    redrawCurrentImage();
                }
            }, timeTillDraw);
        }
    }

    public void startVisualizing() {
        synchronized (startStopSynchObject) {
            if (currentlyRunning || currentlyStarting) {
                return;
            }
            currentlyStarting = true;
        }
        for (RenderObject thisRunner : theRenderObjects) {
            Thread theThread = new Thread(thisRunner);
            theThreads.add(theThread);
            theThread.start();
        }

        synchronized (startStopSynchObject) {
            currentlyRunning = true;
            currentlyStarting = false;
        }

    }
    volatile boolean currentlyStopping = false;
    volatile boolean currentlyStarting = false;
    Object startStopSynchObject = new Object();

    public void stopVisualizing() {

        synchronized (startStopSynchObject) {
            if (!currentlyRunning || currentlyStopping) {
                return;
            }
            currentlyStopping = true;

            if (currentlyStarting) {
                System.err.println("Got through all of the stopping conditions, we're going to stop... but its currently starting....ahhh");
            }
        }

        // tell them all to die
        for (RenderObject thisRunner : theRenderObjects) {
            thisRunner.kill();
        }

        // wait for them all to be done
        for (Thread thisThread : theThreads) {
            try {
                thisThread.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        theThreads.removeAllElements();

        synchronized (startStopSynchObject) {
            currentlyStopping = false;
            currentlyRunning = false;
        }
    }

    private Dimension makeSizeForVizComponent(int i) {
        Dimension currentVisualizerPanelSize = parentPanel.getSize();



        double width = currentVisualizerPanelSize.getWidth() - 10;
        double height = currentVisualizerPanelSize.getHeight() - 20;

        double scaledWidth = width * sizes.get(i).getX();
        double scaledHeight = height * sizes.get(i).getY();

        Dimension d = new Dimension();
        d.setSize(scaledWidth, scaledHeight);

        return d;
    }

    private Dimension makeLocationForVizComponent(int i) {
        Dimension currentVisualizerPanelSize = parentPanel.getSize();

        double width = currentVisualizerPanelSize.getWidth() - 10;
        double height = currentVisualizerPanelSize.getHeight() - 20;

        double startX = 5 + width * positions.get(i).getX();
        double startY = 16 + height * positions.get(i).getY();

        Dimension d = new Dimension();
        d.setSize(startX, startY);

        return d;
    }

    //All of these should be between 0 and 1
    public void addVizComponentAtPositionWithSize(PollingVizComponent newComponent, double xPos, double yPos, double width, double height) {
        theRenderObjects.add(new ThreadRenderObject(new Dimension(200, 200), newComponent, this));
        positions.add(new Point2D.Double(xPos, yPos));
        sizes.add(new Point2D.Double(width, height));
    }
    //All of these should be between 0 and 1
    public void addVizComponentAtPositionWithSize(SelfUpdatingVizComponent newComponent, double xPos, double yPos, double width, double height) {
        theRenderObjects.add(new SelfUpdatingRenderObject(new Dimension(200, 200), newComponent, this));
        positions.add(new Point2D.Double(xPos, yPos));
        sizes.add(new Point2D.Double(width, height));
    }

    public boolean isCurrentlyRunning() {
        return currentlyRunning;
    }

    public String getName() {
        return "Name not implemented";
    }
}
