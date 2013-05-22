package org.rlcommunity.environments.keepAway.visualizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import rlVizLib.visualization.VizComponent;

public class FieldComponent implements VizComponent{
boolean drawnEver=false;

	public void render(Graphics2D g) {
		
		Rectangle2D theField=new Rectangle2D.Double(0,0,1,1);
		g.setColor(Color.green);
		g.fill(theField);
		
	}

	public boolean update() {
		if(!drawnEver){
			drawnEver=true;
			return true;
		}
		return false;
	}
	

}
