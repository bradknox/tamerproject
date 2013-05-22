package org.rlcommunity.environments.keepAway.visualizer;

import rlVizLib.general.TinyGlue;
import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.GenericScoreComponent;
import rlVizLib.visualization.VizComponent;
import rlVizLib.visualization.interfaces.GlueStateProvider;

public class KeepAwayVisualizer extends AbstractVisualizer implements GlueStateProvider {
	
	TinyGlue theGlueState=null;
	
	public KeepAwayVisualizer(TinyGlue theGlueState){
		this.theGlueState=theGlueState;
		
		VizComponent keepAwayComponent=new KeepAwayVizComponent(this);
		VizComponent theField=new FieldComponent();
		VizComponent keepAwayPlayBackVizComponent=new KeepAwayPlayBackVizComponent(this);
		VizComponent theField2=new FieldComponent();

		super.addVizComponentAtPositionWithSize(theField, 0,0,1.0d,.5d);
		super.addVizComponentAtPositionWithSize(keepAwayComponent, 0,0,1.0d,.5d);

		super.addVizComponentAtPositionWithSize(theField2, 0,.5,1.0d,.5d);
		super.addVizComponentAtPositionWithSize(keepAwayPlayBackVizComponent, 0,.5,1.0d,.5d);
		super.addVizComponentAtPositionWithSize(new GenericScoreComponent(this), 0.0,0.0,1.0d,1.0d);
		
				
	}

	public TinyGlue getTheGlueState() {
		return theGlueState;
	}

}
