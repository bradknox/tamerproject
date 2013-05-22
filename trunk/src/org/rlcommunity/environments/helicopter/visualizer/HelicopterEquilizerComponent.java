/* RLViz Helicopter Domain Visualizer  for RL - Competition 
* Copyright (C) 2007, Brian Tanner
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */
package org.rlcommunity.environments.helicopter.visualizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import java.util.Observable;
import java.util.Observer;
import rlVizLib.utilities.UtilityShop;
import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.VizComponentChangeListener;

public class HelicopterEquilizerComponent implements SelfUpdatingVizComponent, Observer {
	private HelicopterVisualizer heliVis= null;
	
     	int lastUpdateTimeStep=-1;

	public HelicopterEquilizerComponent(HelicopterVisualizer helicopterVisualizer) {
		heliVis = helicopterVisualizer;
                helicopterVisualizer.getGlueState().addObserver(this);
	}

	public void render(Graphics2D g) {
		double[] state = heliVis.getState();
                if(state==null){
                    return;
                }
	    //SET COLOR
            g.setColor(Color.WHITE);
            g.fillRect(0,0,1,1);

	    g.setColor(Color.BLACK);
	    //DRAW 12 Lines with blue ball equalizers.
            
            int bottomOfLines=5;
            int topOfLines=95;
            double lineRangeSize=topOfLines-bottomOfLines;
	    AffineTransform saveAT = g.getTransform();
		g.scale(.01, .01);
		double min =0.0d;
		double max = 0.0d;
		for(int i=0; i<12; i++){
			g.setColor(Color.BLACK);
			g.drawLine(i*7+6, bottomOfLines, i*7+6, topOfLines);
			g.setColor(Color.BLUE);
			min = heliVis.getMinAt(i);
			int transY = (int)(UtilityShop.normalizeValue( state[i], heliVis.getMinAt(i),heliVis.getMaxAt(i))*(lineRangeSize) + (float)bottomOfLines);
			if(transY > topOfLines){
                            transY = topOfLines;
                            g.setColor(Color.RED);
                        }
                        if(transY < bottomOfLines){
                            transY = bottomOfLines;
                            g.setColor(Color.RED);
                        }
                        g.drawRect(i*7+5, transY, 2, 1);
		}
	    g.setTransform(saveAT);
	}

    /**
     * This is the object (a renderObject) that should be told when this component needs to be drawn again.
     */
    private VizComponentChangeListener theChangeListener;

    public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener) {
        this.theChangeListener = theChangeListener;
    }

    /**
     * This will be called when TinyGlue steps.
     * @param o
     * @param arg
     */
    public void update(Observable o, Object arg) {
        if (theChangeListener != null) {
            theChangeListener.vizComponentChanged(this);
        }
    }

}
