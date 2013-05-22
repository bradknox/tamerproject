/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.utexas.cs.tamerProject.environments.loopmaze;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 *
 * @author btanner
 */
public class Zone {
    private final Line2D centerLine;
    private final double zoneRadius;


    public Zone(double x1,double y1,double x2,double y2, double zoneRadius){
        Point2D start=new Point2D.Double(x1,y1);
        Point2D end=new Point2D.Double(x2,y2);
        centerLine=new Line2D.Double(start, end);
        this.zoneRadius=zoneRadius;
    }

    public double getReward(int[] agentPosition){
        double distance=centerLine.ptSegDist(agentPosition[0], agentPosition[1]);
        if(distance<zoneRadius){
            return -400.0d * (zoneRadius - distance);
        }
        return 0.0d;
    }


}
