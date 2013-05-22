package edu.utexas.cs.tamerProject.envModels.rewModels;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.envModels.transModels.LoopMazeTransModel;
import edu.utexas.cs.tamerProject.modeling.templates.ObsActModel;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndTerm;

public class LoopMazeRewModel implements ObsActModel {

	LoopMazeTransModel transModel = new LoopMazeTransModel();
	
	/*
	 * The parameters below MUST match those in the environmental code.
	 */
	public double rewardPerStep = -1.0d;
    public double rewardAtGoal = 0.0d;

    
	public double predictLabel(Observation obs, Action act) {
		ObsAndTerm nextObsAndTerm = transModel.sampleNextObs(obs, act);
		return getReward(nextObsAndTerm.getTerm());
	}

    public double getReward(boolean terminal) {
		if (terminal) {
			return rewardAtGoal;
		} else {
			return rewardPerStep;
		}
    }

}
