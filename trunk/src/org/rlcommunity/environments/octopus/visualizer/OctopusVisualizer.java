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


import java.util.List;
import java.util.Set;
import org.rlcommunity.environments.octopus.components.Target;
import org.rlcommunity.environments.octopus.components.Vector2D;
import org.rlcommunity.environments.octopus.messages.OctopusStateRequest;
import org.rlcommunity.environments.octopus.messages.OctopusStateResponse;
import org.rlcommunity.environments.octopus.messages.OctopusCoreDataRequest;
import org.rlcommunity.environments.octopus.messages.OctopusCoreDataResponse;
import rlVizLib.general.TinyGlue;
import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.GenericScoreComponent;
import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.interfaces.DynamicControlTarget;
import rlVizLib.visualization.interfaces.GlueStateProvider;

/**
 *
 * @author btanner
 */
public class OctopusVisualizer extends AbstractVisualizer implements GlueStateProvider{
    private TinyGlue glueState;
    private DynamicControlTarget theControlTarget;
    
    Set<Target> theTargets=null;
    Double theSurfaceLevel=null;

    
      public OctopusVisualizer(TinyGlue glueState, DynamicControlTarget theControlTarget) {
        super();

        this.glueState = glueState;
        this.theControlTarget = theControlTarget;
        setupVizComponents();
        addDesiredExtras();


    }

    protected void setupVizComponents() {
        SelfUpdatingVizComponent scoreComponent = new GenericScoreComponent(this);
        super.addVizComponentAtPositionWithSize(new OctopusVizComponent(this),0, 0, 1.0, 1.0);
        super.addVizComponentAtPositionWithSize(scoreComponent, 0, 0, 1.0, 1.0);
    }

    protected void addDesiredExtras() {
        addPreferenceComponents();
    }

    public void addPreferenceComponents() {

    }

    public TinyGlue getTheGlueState() {
        return glueState;
    }

    List<List<Vector2D>> getCompartmentShapes() {
        OctopusStateResponse theResponse=OctopusStateRequest.Execute();
        return theResponse.getCompartmentShapes();
    }

    
    private void requestCoreVisualizerData(){
            OctopusCoreDataResponse theResponse=OctopusCoreDataRequest.Execute();
            theTargets=theResponse.getTargets();
            theSurfaceLevel=theResponse.getSurfaceLevel();
        
    }
    
    private void checkCoreVisualizerData(){
        if(theTargets==null || theSurfaceLevel==null){
            requestCoreVisualizerData();
        }
    }
    double getSurfaceLevel() {
        checkCoreVisualizerData();
        return theSurfaceLevel;
    }
    
    

    Set<Target> getTargets() {
        checkCoreVisualizerData();
        return theTargets;
    }
    
    @Override
    public String getName(){
        return "Experimental Octopus .1";
    }

    
}
