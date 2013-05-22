package org.rlcommunity.environments.skeleton.visualizer;


import rlVizLib.general.TinyGlue;
import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.VizComponent;
import org.rlcommunity.rlglue.codec.types.Observation;
import rlVizLib.visualization.GenericScoreComponent;
import rlVizLib.visualization.interfaces.GlueStateProvider;

public class SkeletonVisualizer extends AbstractVisualizer implements GlueStateProvider {

    private TinyGlue theGlueState = null;

    /**
     * Creates a new Cart Pile Visualizer
     * @param theGlueState Global glue state object
     */
    public SkeletonVisualizer(TinyGlue theGlueState) {
        super();
        this.theGlueState = theGlueState;

        GenericScoreComponent scoreComponent = new GenericScoreComponent(this);
        super.addVizComponentAtPositionWithSize(scoreComponent, 0, 0, 1.0, 1.0);
    }


    public TinyGlue getTheGlueState() {
        return theGlueState;
    }

    @Override
    public String getName() {
        return "Skeleton 1.0";
    }
}
