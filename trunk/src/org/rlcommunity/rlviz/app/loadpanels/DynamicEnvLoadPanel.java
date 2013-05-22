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


package org.rlcommunity.rlviz.app.loadpanels;

import org.rlcommunity.rlviz.app.RLGlueLogic;

import rlVizLib.general.ParameterHolder;
import rlVizLib.messaging.environmentShell.EnvShellTaskSpecRequest;
import rlVizLib.messaging.environmentShell.EnvShellTaskSpecResponse;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;

public class DynamicEnvLoadPanel extends DynamicLoadPanel implements EnvLoadPanelInterface {

	public DynamicEnvLoadPanel(RLGlueLogic theGlueConnection){
		super(theGlueConnection,"No Envs Available");
	}
	
	public void updateList(){
		theNames=theGlueConnection.getEnvNameList();
		theParams=theGlueConnection.getEnvParamList();
                
		
		super.refreshList(theNames, "No Envs Available");
		
	}

	@Override
	public boolean load(String thisName, ParameterHolder thisP) {
            boolean loadCheck = theGlueConnection.loadEnvironment(thisName,thisP);
            if(loadCheck){
                theGlueConnection.loadEnvironmentVisualizer();
                return true;
            }
            return false;
	}

	@Override
	public String getStringType() {
		return "Environment";
	}

    public TaskSpecPayload getTaskSpecPayload() {
        if (currentLoadedIndex != -1 && !theNames.isEmpty()) {
            String thisName = theNames.get(currentLoadedIndex);
            updateParamsFromPanel();
            ParameterHolder thisP = theParams.get(currentLoadedIndex);
            
            EnvShellTaskSpecResponse theTSPResponse=EnvShellTaskSpecRequest.Execute(thisName, thisP);
            return theTSPResponse.getTaskSpecPayload();
                    
        }else{
            System.err.println("getTaskSpecPayload was called on the EnvDynamicLoad Panel but there are none of what you tried to load or we couldn't set the index right");
        }
        return null;
    }

    
}
