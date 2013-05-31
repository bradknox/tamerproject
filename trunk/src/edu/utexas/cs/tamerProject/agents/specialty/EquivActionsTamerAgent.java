package edu.utexas.cs.tamerProject.agents.specialty;

import java.util.HashMap;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;




public class EquivActionsTamerAgent extends TamerAgent {

	
		
	    public Action agent_stepWEquiv(double r, Observation o, double startTime, Action predeterminedAct, 
	    		Action tieBreakAction, HashMap<Action, Action> actionMap) {
	    	//System.out.println("\n-----------------EquivActionsTamerAgent step---------------\n");
	    	//System.out.println("Training? " + this.inTrainSess);
	    	//System.out.println("Tamer obs: " + Arrays.toString(o.intArray));
	    	this.checkObs(o);

	    	this.stepStartTime = startTime;
			this.stepStartHelper(r, o); // this.stepStartTime (set in stepStartHelper()) ends last step and starts new step
			//System.out.println("TAMER this.stepStartTime: " + String.format("%f", this.stepStartTime));
	    	this.hLearner.recordTimeStepEnd(this.stepStartTime);
			
	    	/*
	    	 * PROCESS PREVIOUS TIME STEP
	    	 */
			//if (this.stepsThisEp > 1)
				//System.out.println("Predicted human reward for last step in TAMER: " + this.getValForLastStep());
			processPrevTimeStep(this.stepStartTime);
			this.hLearner.processSamples(this.stepStartTime, inTrainSess);
			
			/*
			 *  GET ACTION
			 */
			Action act = predeterminedAct;
			if (this.currObsAndAct.actIsNull()) {
				act = this.actSelector.selectAction(o, tieBreakAction);
			}


			/*
			 * CHANGE ACTION TO DOMINANT EQUIVALENT ACTION	
			 * 
			 * Will Action.equals() work?
			 */
			if (actionMap.containsKey(act)) {
				act = actionMap.get(act);
			}
			this.currObsAndAct.setAct(act);
			
			this.lastStepStartTime = this.stepStartTime;

			this.stepEndHelper(r, o);
			if (this.isTopLevelAgent) // If not top level, TamerAgent's chosen action might not be the actual action. This must be called by the primary class.
				this.hLearner.recordTimeStepStart(this.featGen.getFeats(o, this.currObsAndAct.getAct()), this.stepStartTime);
			
			return this.currObsAndAct.getAct();
	    }


	
}
