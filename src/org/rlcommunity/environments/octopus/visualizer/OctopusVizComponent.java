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
package org.rlcommunity.environments.octopus.visualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.rlcommunity.environments.octopus.components.Target;
import org.rlcommunity.environments.octopus.components.Vector2D;
import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.VizComponentChangeListener;

/**
 *
 * @author Brian Tanner
 */
public class OctopusVizComponent implements SelfUpdatingVizComponent, Observer {

    OctopusVisualizer theVisualizer = null;
    
    /**Some of these should be removed **/
        private static final int REDRAW_DELAY = 30;
    private static final float DRAWING_SCALE = 30.0f;
    private static final Paint ARM_PAINT = Color.GREEN;
    private static final float FOOD_SIZE = 0.6f;
    private static final Shape FOOD_SHAPE =
            new Ellipse2D.Double(-FOOD_SIZE / 2, -FOOD_SIZE / 2, FOOD_SIZE, FOOD_SIZE);
    private static final Paint FOOD_PAINT = Color.ORANGE;
    private static final float TARGET_SIZE = 0.2f;
    private static final Shape TARGET_SHAPE =
            new Ellipse2D.Double(-TARGET_SIZE / 2, -TARGET_SIZE / 2, TARGET_SIZE, TARGET_SIZE);
    private static final Paint ELIGIBLE_TARGET_PAINT = Color.RED;
    private static final Paint INELIGIBLE_TARGET_PAINT = new Color(.8f, .8f, .8f, .7f);
    private static final float ARM_HUE = 0.3f;
    private static final Paint WATER_PAINT = new Color(0, 0.5f, 1.0f, 0.1f);

    public OctopusVizComponent(OctopusVisualizer theVisualizer) {
        this.theVisualizer = theVisualizer;
        theVisualizer.getTheGlueState().addObserver(this);
    }

    public void render(Graphics2D g2) {
        AffineTransform theScaleTransform = new AffineTransform();
        double scaleFactor = 1000.0d;
        theScaleTransform.scale(1.0d / scaleFactor, 1.0d / scaleFactor);
        AffineTransform origTransform = g2.getTransform();

        AffineTransform x = g2.getTransform();
        x.concatenate(theScaleTransform);
        g2.setTransform(x);


        List<List<Vector2D>> compartmentShapes = theVisualizer.getCompartmentShapes();

        int displayWidth = 1000;
        int displayHeight = 1000;


        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, displayWidth, displayHeight);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform oldTransform = g2.getTransform();
        g2.translate(displayWidth / 4, displayHeight / 2);
        g2.scale(DRAWING_SCALE, -DRAWING_SCALE);
        g2.setStroke(new BasicStroke(1.0f / DRAWING_SCALE));

        /* draw mouth */
//        if (env.getMouth() != null) {
//            g2.setPaint(Color.BLACK);
//            g2.fill(env.getMouth().getShape());
//        }

        /* draw arm */
        for (int i = 0, n = compartmentShapes.size(); i < n; i++) {
            List<Vector2D> thisShapeVector = compartmentShapes.get(i);
            
            
            float lum = 0.7f + 0.3f * (float) i / n;
            g2.setPaint(new Color(Color.HSBtoRGB(ARM_HUE, 1.0f, lum)));
            g2.fill(shapeFromVectorList(thisShapeVector));
        }

        AffineTransform baseTransform = g2.getTransform();

        /* draw food */
//        g2.setPaint(FOOD_PAINT);
//        for (Node n : env.getFood()) {
//            Vector2D pos = n.getPosition();
//            g2.translate(pos.getX(), pos.getY());
//            g2.fill(FOOD_SHAPE);
//            g2.setTransform(baseTransform);
//        }

        /* draw targets */
        for (Target t : theVisualizer.getTargets()) {
            Vector2D pos = t.getPosition();
            g2.translate(pos.getX(), pos.getY());
//            g2.setPaint(t.isHighlighted() ? ELIGIBLE_TARGET_PAINT : INELIGIBLE_TARGET_PAINT);
            g2.setPaint(ELIGIBLE_TARGET_PAINT);
            g2.fill(TARGET_SHAPE);
            g2.setTransform(baseTransform);
        }

        /* draw water level */
        g2.setPaint(WATER_PAINT);
        Shape waterShape = new Rectangle2D.Double(
                -100, theVisualizer.getSurfaceLevel() - 200,
                200, 200);
        g2.fill(waterShape);

        g2.setTransform(oldTransform);
        g2.setTransform(origTransform);
        
    }
    
        private static Shape shapeFromVectorList(List<Vector2D> theVector) {
        GeneralPath gp = new GeneralPath();
        if(theVector.size()<3){
            return gp;
        }

        gp.moveTo((float) theVector.get(0).getX(), (float) theVector.get(0).getY());
        for(int i=1;i<theVector.size();i++){
            gp.lineTo((float)theVector.get(i).getX(),(float)theVector.get(i).getY());
        }
        gp.closePath();
        return gp;
    }

        private VizComponentChangeListener theChangeListener=null;
        
    public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener) {
        this.theChangeListener=theChangeListener;
    }

    /**
     * This is probably getting called multiple times per step which is inefficient but not intolerable for now.
     * @param o
     * @param arg
     */
    public void update(Observable o, Object arg) {
        if(theChangeListener!=null){
            theChangeListener.vizComponentChanged(this);
        }
    }
        
        

}
