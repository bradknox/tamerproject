package edu.utexas.cs.tamerProject.agents.dynamicProgramming;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;

/**
 * This version of DPAgent ignores episode endings, acting as all of the episodes add 
 * together into one continuing learning experience.
 * 
 * Also, it's worth noting that the variable stepsThisEp is incremented for both agent_end() 
 * and agent_start(), which are being combined into one step. Therefore, the number of steps
 * only corresponds to a non-combined perspective. At the moment, this distinction makes no
 * difference in agent learning or behavior.
 * 
 * @author bradknox
 *
 */
public class DPForceContAgent extends DPAgent {

	double endOfEpReward = 0;
    
	public void agent_init(String taskSpec) {
		super.agent_init(taskSpec);
		this.envTransModel.setForceCont(true);
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
    	    	
        return agent_step(this.endOfEpReward, o, time, predeterminedAct);
    }

    
    

    public void agent_end(double r, double time) {
    	this.stepStartTime = time;
    	this.endHelper(r);
    	//System.out.println("stepsThisEp in agent_end(): " + this.stepsThisEp);
    	//System.out.println("\n\n Episode end\n");
    	
		/*
		 * DON'T PROCESS PREVIOUS TIME STEP (it will be done during agent_start() instead)
		 */
    	if (this.actSelector != null)
    		this.actSelector.anneal(); 
    	
    	this.endOfEpReward = r; // save reward at episode end so that it can be used for update at agent_start()
    }

	
	
}
