/*
Adapted by Brad Knox from RandomAgent.java by Brian Tanner

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
package edu.utexas.cs.tamerProject.agents.sarsaLambda;

import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndAct;


/**
 * This version of SarsaLambdaAgent ignores episode endings, acting as all of the 
 * episodes add together into one continuing learning experience.
 * 
 * Also, it's worth noting that the variable stepsThisEp is incremented for both agent_end() 
 * and agent_start(), which are being combined into one step. Therefore, the number of steps
 * only corresponds to a non-combined perspective. At the moment, this distinction makes no
 * difference in agent learning or behavior.
 * 
 * @author bradknox
 *
 */
public class SarsaLForceContAgent extends SarsaLambdaAgent{
    
	double endOfEpReward = 0;
	
	public void agent_init(String taskSpec) {
		super.agent_init(taskSpec);
		this.actSelector.getEnvTransModel().setForceCont(true);
	}
	
	public Action agent_start(Observation o, double time, Action predeterminedAct) {
    	/*
    	 * preserve lastAct, lastObs, and stepsThisEp
    	 */
		Observation lastObsBkp = this.lastObsAndAct.getObs();
    	Action lastActBkp = this.lastObsAndAct.getAct();
    	int stepsThisEpBkp = this.stepsThisEp;
    	
    	this.startHelper();
    	
    	/*
    	 * restore lastAct, lastObs, and stepsThisEp
    	 */
    	this.lastObsAndAct.setObs(lastObsBkp);
    	this.lastObsAndAct.setAct(lastActBkp);
    	this.stepsThisEp = stepsThisEpBkp;
    	
    	//System.out.println("stepsThisEp in agent_start(): " + this.stepsThisEp);
    	//if (this.lastObsAndAct.getObs() != null && this.lastObsAndAct.getAct() != null)
    	//	System.out.println("last obs and act at agent_start(): " 
    	//											+ Arrays.toString(this.lastObsAndAct.getObs().doubleArray) + ", "
		//											+ Arrays.toString(this.lastObsAndAct.getAct().intArray));
    	//else
    	//	System.out.println("last obs and act are null at agent_start()");
    	
        return agent_step(this.endOfEpReward, o, time, predeterminedAct); 
    }

    
    

    public void agent_end(double r, double time) {
    	this.stepStartTime = time; // this won't be used for an update, since there is no observation at this time
    	this.endHelper(r);
    	//System.out.println("stepsThisEp in agent_end(): " + this.stepsThisEp);
    	//System.out.println("\n\n Episode end\n");
    	
		/*
		 * DON'T PROCESS PREVIOUS TIME STEP (it will be done during agent_start() instead)
		 */
    	this.actSelector.anneal();
    	
    	this.endOfEpReward = r; // save reward at episode end so that it can be used for update at agent_start()
    }

}
