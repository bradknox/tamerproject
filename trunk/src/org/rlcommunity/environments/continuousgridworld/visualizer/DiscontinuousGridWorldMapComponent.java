package org.rlcommunity.environments.continuousgridworld.visualizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import org.rlcommunity.rlglue.codec.types.Observation;
import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.VizComponentChangeListener;

public class DiscontinuousGridWorldMapComponent implements SelfUpdatingVizComponent, Observer {

    GridWorldVisualizerInterface CGWViz;
    private VizComponentChangeListener theChangeListener;

    public DiscontinuousGridWorldMapComponent(GridWorldVisualizerInterface CGWViz) {
        this.CGWViz = CGWViz;
        CGWViz.getTheGlueState().addObserver(this);
    }

    public void render(Graphics2D g) {
        Rectangle2D theWorldRect = CGWViz.getWorldRect();

        double width = theWorldRect.getWidth();
        double height = theWorldRect.getHeight();
        
        AffineTransform theScaleTransform = new AffineTransform();
        theScaleTransform.scale(1.0d / width, 1.0d / height);
        AffineTransform x = g.getTransform();
        x.concatenate(theScaleTransform);
        g.setTransform(x);


        int numX = 20;
        int numY = 20;

        Vector<Observation> queries = new Vector<Observation>(numX*numY);

        for (int xi = 0; xi < numX; xi++) {
            for (int yi = 0; yi < numY; yi++) {
                Observation queryObs = new Observation(0, 2);
                queryObs.doubleArray[0] = ((double)xi) / numX * width;
                queryObs.doubleArray[1] = ((double)yi) / numY * height;

                queries.add(queryObs);
            }
        }

        // Find out the mapping between real space and observation space
        Vector<Observation> mappedQueries = CGWViz.getQueryObservations(queries);

        int idx = 0;

        for (int xi = 0; xi < numX; xi++) {
            for (int yi = 0; yi < numY; yi++, idx++) {
                double px = ((double)xi)/numX * width;
                double py = ((double)yi)/numY * height;
                double w = width/numX;
                double h = height/numY;
                
                double[] observation = mappedQueries.get(idx).doubleArray;

                Rectangle2D realRect = new Rectangle2D.Double(px, py, w, h);
//                System.out.println (px+" "+py+" maps to "+observation[0]+" "+observation[1]);
                Rectangle2D fillRect = 
                        new Rectangle2D.Double(observation[0], observation[1], w, h);
                boolean done = false;
                
                Vector<Rectangle2D> barrierRegions = CGWViz.getBarrierRegions();
                Vector<Double> thePenalties = CGWViz.getPenalties();

                for (int i = 0; i < barrierRegions.size(); i++) {
                    Rectangle2D thisRect = barrierRegions.get(i);
                    double thisPenalty = thePenalties.get(i);
                    if (thisRect.intersects(realRect)) {
                        Color theColor = new Color((float) thisPenalty, 0, 0);

                        g.setColor(theColor);
                        g.fill(fillRect);

                        done = true;
                        break;
                    }
                }

                if (done)
                    continue;

                // Test whether this observation falls in the reset region
                Vector<Rectangle2D> resetRegions = CGWViz.getResetRegions();

                for (Rectangle2D rect : resetRegions) {
                    if (rect.intersects(realRect)) {
                        g.setColor(Color.blue);
                        g.fill(fillRect);

                        done = true;
                        break;
                    }
                }
            }
        }
    }

    public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener) {
        this.theChangeListener = theChangeListener;
    }

    public void update(Observable o, Object arg) {
        if (theChangeListener != null) {
            theChangeListener.vizComponentChanged(this);
        }
    }
}
