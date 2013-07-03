package edu.utexas.cs.tamerProject.agents;

import java.util.ArrayList;

import edu.utexas.cs.tamerProject.agents.tamer.HRew;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;


/**
 * HLearner interfaces between an agent, a CreditAssign object, and a RegressionModel 
 * object to learn the human reward model.
 * 
 * @author bradknox
 *
 */

public class HLearner {
	
	/** Written by Brad Knox
	 *
	 *
	 **/

	private RegressionModel model = null; 
	public CreditAssign credA = null;
	//


	public HLearner(RegressionModel model, CreditAssignParamVec credAssignParams){
		this.model = model;
		this.credA = new CreditAssign(credAssignParams);
	}

	public RegressionModel getModel(){
		return this.model;
	}
	
	public void reset(){
		this.model.clearSamplesAndReset();
		this.clearHistory();
	}

	// each time step will have a endTime, which is the next step's startTime
	public void recordTimeStep(double[] feats, double startTime){
		this.recordTimeStepEnd(startTime);
		this.recordTimeStepStart(feats, startTime);
	}
	
	// each time step will have a endTime, which is the next step's startTime
	public void recordTimeStepStart(double[] feats, double startTime){
		this.credA.recordTimeStepStart(feats, startTime);
	}
	
	// each time step will have a endTime, which is the next step's startTime
	public void recordTimeStepEnd(double endTime){
		this.credA.recordTimeStepEnd(endTime);
	}
	
	public Sample[] processSamples(double currTime, boolean inTrainSess) {
		//System.out.print("\n\nHLearner processSamples()");
		Sample[] samples = this.credA.processSamplesAndRemoveFinished(currTime, inTrainSess);
		//if (samples.length > 0)
		//	System.out.println("Adding " + samples.length + " samples.");
		if (samples.length > 0)
			addSamplesAndBuild(samples);
		return samples;
	}
	
	public void clearHistory(){
		this.credA.clearHistory();
	}


	public void processHRew(ArrayList<HRew> hRewThisStep){
		//System.out.println("Updating with human reward. Size: " + hRewThisStep.size());
		//Sample[] newSamples = new Sample[0];
		for (HRew hRew: hRewThisStep) {
			this.credA.processNewHReward(hRew.val, hRew.time);
		}
	}

	// NOTE NOTE NOTE
	private void addSamplesAndBuild(Sample[] samples) {
		this.model.addInstancesWReplacement(samples);
		this.model.buildModel();
	}

	

}