package edu.utexas.cs.tamerProject.agents;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import edu.utexas.cs.tamerProject.agents.CreditAssignParamVec;
import edu.utexas.cs.tamerProject.modeling.Sample;

//TODO - the class TimeStep has a redundant name, which is super dangerous. rename.

/**
 * CreditAssign implements TAMER's credit assignment technique, first described
 * in the Knox and Stone K-CAP09 paper but significantly revised since then. The
 * current algorithm is documented as part of an article in preparation; ask Brad
 * Knox for the section if it hasn't been published yet.
 * 
 * Credit assignment serves to create labels for state-action pair samples from
 * delayed reward. This class is fairly complex but quite important. I suggest 
 * either copying its use from TamerAgent (and HLearner, more directly) or first 
 * understanding it through our to-be-published documentation.
 * 
 * @author bradknox
 *
 */
public class CreditAssign{
	
	public class TimeStepForCred{
		public static final double UNASSIGNED_START_TIME = Double.NEGATIVE_INFINITY;
		public static final double UNASSIGNED_END_TIME = Double.POSITIVE_INFINITY;
		public double startTime = UNASSIGNED_START_TIME;
		public double endTime = UNASSIGNED_END_TIME;
		public double[] feats = null;
		public boolean throwOut = false; // if true, will not be used for learning
		public boolean setInStone = false;
		public double credUsedBeforeLastStep = 0;
		
		public String toString() {
			String s = "\n";
			s += "startTime: " + String.format("%f", startTime) + "\n";
			s += "endTime: " + String.format("%f", endTime) + "\n";
			s += "throwOut: " + throwOut + "\n";
			s += "setInStone: " + setInStone + "\n";
			s += "feats: " + Arrays.toString(feats) + "\n";
			return s;
		}
	}
	 
	public static final Random randGenerator = new Random();
	
	ArrayList<TimeStepForCred> timeStepsInWindow;
	ArrayList<Sample> activeSamples;
	int totalTimeSteps;
	final int UNIQUE_START;
	
	boolean inTrainSess = false;
	final double SAMPLE_CUMUL_CRED_MIN = 0.9; // If the training session is off for any part of first SAMPLE_CUMUL_CRED_MIN proportion of potential credit (or more), the  
								// sample isn't counted. I keep this less than 1 in case a long-tailed delay prob dist fcn is used. 
	boolean EXTRAPOLATE_FUTURE_REW = false;
	private double MIN_USED_CRED_FOR_EXTRAP = 0.5; // TODO set this externally
	public static final double APPROX_ONE = 0.99999;
	
	String distClass; // class of distribution over delay; can be previousStep, immediate, or uniform...; change to enum?
	boolean delayWtedIndivRew = false; // this uses delay-weighted, individual reward (named in the original TAMER journal paper 
										// and first used in the K-CAP09 paper) instead of the current d-w, aggregate reward
	boolean noUpdateWhenNoRew = false;
	
	double windowStart; // delay before window starts; in seconds
	double windowEnd; // window of credit assignment goes from creditDelay to (creditDelay + windowSize)
	
	// Note: start and end of window are in reverse time (looking backwards),
	// while start and end of time steps are in forward time.
	
	
	public CreditAssign(CreditAssignParamVec params){
		System.out.println("Creating CreditAssign object with params: " + params);
		this.distClass = params.distClass;
		this.windowStart = params.creditDelay;
		this.windowEnd = params.windowSize + this.windowStart;
		this.UNIQUE_START = params.uniqueStart;
		this.EXTRAPOLATE_FUTURE_REW = params.EXTRAPOLATE_FUTURE_REW;
		this.delayWtedIndivRew = params.delayWtedIndivRew;
		this.noUpdateWhenNoRew = params.noUpdateWhenNoRew;
		if (delayWtedIndivRew) {
			this.EXTRAPOLATE_FUTURE_REW = false;
			MIN_USED_CRED_FOR_EXTRAP = 0.0;
		}
//		else {noUpdateWhenNoRew = false;}
		
		totalTimeSteps = 0;
		clearHistory();
	}
	
	
	public void recordTimeStepEnd(double btwnStepTime){
		int lastTimeStepI = this.timeStepsInWindow.size() - 1;
		if (this.timeStepsInWindow.size() > 0)
			this.timeStepsInWindow.get(lastTimeStepI).endTime = btwnStepTime;
	}
	public void recordTimeStepStart(double[] feats, double btwnStepTime){
		// add new time step
		this.timeStepsInWindow.add(new TimeStepForCred());
		int lastTimeStepI = this.timeStepsInWindow.size() - 1;
		if (this.timeStepsInWindow.size() > 1 && 
				this.timeStepsInWindow.get(lastTimeStepI - 1).endTime == Double.POSITIVE_INFINITY) {
			System.err.println("\n\n\nTried to create a new time step in CreditAssign before ending the last. Exiting.");
			System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
			System.exit(1);
		}
		this.timeStepsInWindow.get(lastTimeStepI).feats = feats;
		this.timeStepsInWindow.get(lastTimeStepI).startTime = btwnStepTime;
		
		int newSampleUnique = delayWtedIndivRew ? -1 : this.totalTimeSteps + UNIQUE_START; 
		this.activeSamples.add(new Sample(feats, 1.0, newSampleUnique));
		this.totalTimeSteps++;
		//System.out.println("this.activeSamples.size(): " + this.activeSamples.size());
	}
	
	/** 
	 * @param stepIndex
	 * @return the number of active steps that occurred before the current step
	 * 
	 * For some step in this.timeStepsInWindow, indexed by stepIndex, this calculates how many
	 * steps back it is from the current time step. This method can alternatively be thought of
	 * as simply reversing the index order.
	 *  
	 * The "current" step should be the actual current one if this is called during a time step,
	 * and the just-finished step f this is called between steps (i.e., during agent_step()).
	 *  
	 * Quick math illustration: For a window of 3, the current step index is 2, so (3-2)-1
	 * makes 0 time steps before current.
	 **/
	private int getStepsBeforeCurrent(int stepIndex){
		return (this.timeStepsInWindow.size() - stepIndex) - 1;
	}
	
	
	
	
/////////////////////////////////////////////////////////////////////////	
	
	/**
	 * This method processes time steps to create samples, removes time steps that
	 * are beyond the credit assignment window, and return an array of samples
	 * to be added to a model update (e.g., H).
	 * 
	 * Essentially, this method's main parts are the calls of processTimeSteps() and
	 * removeFinishedTimeSteps() and then adding the created learning samples to
	 * an array to be returned.
	 *   
	 * @param currTime
	 * @param inTrainSess
	 * @return
	 */
	/*
	 * A bunch of code here only matters for the case when delayWtedIndivRew is true.
	 * Since, that case is for an out-dated credit assignment method, it can mostly
	 * be ignored.
	 */
	public Sample[] processSamplesAndRemoveFinished(double currTime, boolean inTrainSess) {
//		println("\n\n\n-------processSamplesAndRemoveFinished");
		
		// enforce calling this method at the right time
		if (GeneralAgent.duringStepTransition &&
						this.timeStepsInWindow.size() > 0 &&
						this.timeStepsInWindow.get(this.timeStepsInWindow.size() - 1).endTime
						== TimeStepForCred.UNASSIGNED_END_TIME) {
				System.err.println("Calling processSamplesAndRemoveFinished() with a step's start " +
						"recorded but the end unrecorded in CreditAssign. Be careful to call this " +
						"method before calling recordTimeStepStart().");
				System.err.println("Thread stack trace:\n" + Arrays.toString(Thread.currentThread().getStackTrace()));
				System.err.println("Killing agent process\n\n");
				System.exit(1);
		}


		
		ArrayList<Sample> activeCreditedSamples = this.processTimeSteps(currTime, inTrainSess);
		ArrayList<Sample> removedSamples = this.removeFinishedTimeSteps(currTime, inTrainSess);		
		
		//println("After processing, activeCreditedSamples: " + Arrays.toString(activeCreditedSamples.toArray()));
		//println("After processing, removedSamples: " + Arrays.toString(removedSamples.toArray()));
		//println("num activeCreditedSamples: " + activeCreditedSamples.size());
		//println("num removedSamples: " + removedSamples.size());
		
		if (noUpdateWhenNoRew) {
			if (delayWtedIndivRew && allSamplesHaveZeroRew()) {
				//System.out.println("no reward, no update.");
				return new Sample[0];
			}
		}
		
		ArrayList<Sample> samples = new ArrayList<Sample>();
		if (EXTRAPOLATE_FUTURE_REW || delayWtedIndivRew) {
			samples.addAll(activeCreditedSamples);
			if (delayWtedIndivRew) { // clone samples and reset reward and labels 
				for (int i = 0; i < this.activeSamples.size(); i++) {
					this.activeSamples.set(i, this.activeSamples.get(i).clone());
					this.activeSamples.get(i).unweightedRew = 0;
					this.activeSamples.get(i).label = 0;
					//System.out.println("replaced and reset a sample");
				}
			}
		}
		samples.addAll(removedSamples);
		//System.out.println("samples.size(): " + samples.size());
		if (delayWtedIndivRew) {
			removeSamplesWNoNewCred(samples); // remove all samples that received no credit last step
			setWtToCredLastStep(samples);
		}
		else {
			if (noUpdateWhenNoRew)
				removeSamplesWZeroRew(samples);
		}
		//System.out.println("num activeSamples: " + activeSamples.size());

		Sample[] samplesArray = new Sample[samples.size()];
		samples.toArray(samplesArray); // convert ArrayList to regular array
		return samplesArray;
	}

/////////////////////////////////////////////////////////////////////////	

	
	
	
	
	/**
	 * Iterate through time steps that are still eligible for credit (not beyond
	 * the credit assignment window) and create learning samples that reflect
	 * the current time and current credited reward totals per step.
	 * 
	 * @param currTime
	 * @param inTrainSess
	 * @return
	 */
	private ArrayList<Sample> processTimeSteps(double currTime, boolean inTrainSess) {
		//println("-------processTimeSteps\ncurrTime: " + String.format("%f", currTime));
		
		ArrayList<Sample> activeCreditedSamples = new ArrayList<Sample>();
		for (int i = 0; i < this.timeStepsInWindow.size(); i++) {
			Sample activeSample = this.activeSamples.get(i);
			TimeStepForCred step = this.timeStepsInWindow.get(i);
			//println("Time step before: " + this.timeStepsInWindow.get(i));
			//println("Sample before: " + this.activeSamples.get(i));
			if (this.timeStepsInWindow.get(i).setInStone) // don't change a step sample that is ineligible for further reward
				continue;
			double priorUsedCredit = this.activeSamples.get(i).usedCredit;
			
			/*
			 * Update sample for this time step
			 */
			activeSample.usedCredit = getCreditPastElig(i, getStepsBeforeCurrent(i), currTime);
			activeSample.label = activeSample.unweightedRew;
			if (EXTRAPOLATE_FUTURE_REW)
				activeSample.label /= activeSample.usedCredit; // extrapolate the final reward so that the label is more helpful immediately
			activeSample.creditUsedLastStep = activeSample.usedCredit - step.credUsedBeforeLastStep;
			
		
				
			/* If first credit opportunity is missed, throw out sample. Gets rid of 
			 * time steps created while not inTrainSess. (Time samples already 
			 * existing when session ends are dealt with in setInTrainSess().)
			 */
			if (!inTrainSess && activeSample.usedCredit > 0.0
						&& (priorUsedCredit == 0.0 || priorUsedCredit == Double.NEGATIVE_INFINITY)) {
				step.throwOut = true;
				//println("Marking a newly credited time step to be thrown out.");
			}
			if (!inTrainSess && isSampleFinished(i)
						&& (this.distClass == "previousStep" || this.distClass == "immediate")) { 
				step.throwOut = true;
				//println("Marking to throw out: " + this.activeSamples.get(i).toString());	
			}
			
			/*
			 * When appropriate, add unfinished samples (e.g. samples still eligible for credit) to
			 * a list for possible addition to the model. 
			 */
			if (this.distClass != "immediate" && this.distClass != "previousStep" 
					&& activeSample.usedCredit > MIN_USED_CRED_FOR_EXTRAP 
					&& activeSample.usedCredit < APPROX_ONE 
					&& !step.throwOut) {
				activeCreditedSamples.add(activeSample); // Note that activeSamples is a list of all non-removed samples. 
														 // activeCreditedSamples is merely the samples that might be 
														 // added to the model as intermediate, extrapolated samples. 
				//println("adding active sample: " + this.activeSamples.get(i).toString());
			}
			step.credUsedBeforeLastStep = activeSample.usedCredit; // set for the next time this is called
			//println("Sample after: " + activeSample);
			//println("Time step after: " + step + "\n---------------\n");
		}
		return activeCreditedSamples;
	}
	

	/**
	 * Determine whether time steps are no longer eligible for further credit.
	 * Any such step is "finished" and its sample is added to a list to be 
	 * returned and then added to the model.
	 * 
	 * @param currTime
	 * @param inTrainSess
	 * @return
	 */
	private ArrayList<Sample> removeFinishedTimeSteps(double currTime, boolean inTrainSess) {
		//println("-------removeFinishedTimeSteps\ncurrTime: " + String.format("%f", currTime));
		// remove any time steps completely beyond window
		ArrayList<Sample> removedSamples = new ArrayList<Sample>();
		int iRemovalOffset = this.distClass == "immediate" ? 0 : 1; // unless immediate, skip last one b/c it should be unfinished
		for (int i = 0; i < this.timeStepsInWindow.size() - iRemovalOffset; i++) { 
			//println("Remove sample? " + this.activeSamples.get(i));
			if (this.timeStepsInWindow.get(i).throwOut) {
				//println("Throwing out step \n" + this.timeStepsInWindow.get(i));
				removeSample(i);
				i--;
			}
			else if (isSampleFinished(i)) {
				//println("removing finished sample: " + this.activeSamples.get(i).toString());
				removedSamples.add(removeSample(i));
				i--;
			}
		}
		return removedSamples;
	}

	private boolean isSampleFinished(int sampleI){
		boolean finished = false;
		if (this.timeStepsInWindow.get(sampleI).setInStone)
			finished = true;
		else if (this.distClass == "previousStep" || this.distClass == "immediate") {
			int stepsBeforeCurrent = getStepsBeforeCurrent(sampleI); 
			int finishedBound = 0;
			// Intuition for logic below: a step transition (i.e., the period during an 
			//	agent_step() call) finishes a step's reward-gathering period but should
			//  not have added another step to the window. Therefore, the boundary moves
			//  one lower.
			if (this.distClass == "immediate")
				finishedBound = GeneralAgent.duringStepTransition ? 0 : 1;
			else if (this.distClass == "previousStep")
				finishedBound = GeneralAgent.duringStepTransition ? 1 : 2;
			//System.out.println("finishedBound: " + finishedBound);
			//System.out.println("stepsBeforeCurrent: " + stepsBeforeCurrent);
			if (stepsBeforeCurrent == finishedBound)
				finished = true;
		}
		else if (this.distClass == "uniform") {
			//double timeSinceEnd = currTime - this.timeStepsInWindow.get(i).endTime;
			if (this.activeSamples.get(sampleI).usedCredit <= 1 &&
					this.activeSamples.get(sampleI).usedCredit >= APPROX_ONE) {//(timeSinceEnd >= this.windowEnd) {
				finished = true;
			}
			//System.out.println("sample finished: " + finished + "; usedCredit: " 
			//				+ this.activeSamples.get(sampleI).usedCredit); 
		}

		return finished;
	}

	private Sample removeSample(int sampleI) {
		TimeStepForCred step = this.timeStepsInWindow.remove(sampleI);
		Sample sample = this.activeSamples.remove(sampleI);		
		sample.label = sample.unweightedRew / sample.usedCredit;
		return sample;
	}
	
	

	public void setInTrainSess(double currTime, boolean newInTrainSess) {
		if (this.inTrainSess && !newInTrainSess) {
			for (int i = 0; i < this.timeStepsInWindow.size(); i++) {
				double usedUpCredit = getCreditPastElig(i, getStepsBeforeCurrent(i), currTime);
				if (usedUpCredit < SAMPLE_CUMUL_CRED_MIN && usedUpCredit > 0.0) {
					this.timeStepsInWindow.get(i).throwOut = true; // throw the sample out
					println("Throwing out an already credited time step.");
				}
				else if (usedUpCredit >= SAMPLE_CUMUL_CRED_MIN){
					this.timeStepsInWindow.get(i).setInStone = true;
					this.activeSamples.get(i).usedCredit = usedUpCredit;
					// sample will be removed next step
				}
			}
		}
		//// Small fudge factor: if session started, credit missed before this instant and during the same 
		//// time step isn't counted for whether to throw the sample out.
		
		this.inTrainSess = newInTrainSess;
		println("\n\n\n\n\n\n\n\nIn training session: " + this.inTrainSess + "\n\n\n");
	}
	

	
	public void clearHistory(){
		this.timeStepsInWindow = new ArrayList<TimeStepForCred>();
		this.activeSamples = new ArrayList<Sample>();
	}


	public void processNewHReward(double hReward, double hRewTime){
		//println("-------processNewHRew\nhRewTime: " + String.format("%f", hRewTime) + "hReward: " + hReward);
		double totalRewardShare = 0.0; // The amount of credit-weighted reward spread across all time steps; 
									// should equal hReward at end...
		for (int i = 0; i < this.timeStepsInWindow.size(); i++) {
			double credit = getCredit(hRewTime, i, getStepsBeforeCurrent(i));
			//println("credit: " + credit);
			double rewardShare = hReward * credit;
			totalRewardShare += rewardShare;
			Sample sample = this.activeSamples.get(i);
			///println("Credit " + credit + " given to sample:\n" + sample);
			sample.unweightedRew += rewardShare;
			sample.usedCredit = Math.max(getCreditPastElig(i, getStepsBeforeCurrent(i), hRewTime), sample.usedCredit);			
			sample.label = sample.unweightedRew;
			if (EXTRAPOLATE_FUTURE_REW)
				sample.label /= sample.usedCredit; // extrapolate the final reward so that the label is more helpful immediately
			//println("this.activeSamples.get(i).unweightedRew: " + this.activeSamples.get(i).unweightedRew);
			//println("this.activeSamples.get(i).label: " + this.activeSamples.get(i).label);
		}
		//print("Total reward share: " + totalRewardShare + ". ");
		//println("Should be " + hReward + " if running on a real agent (with back-to-back time steps).");
	}
	

	
	private double getRelNearBound(int stepI, double currTime){
		if (this.timeStepsInWindow.get(stepI).endTime != Double.NEGATIVE_INFINITY)
			return currTime - this.timeStepsInWindow.get(stepI).endTime;
		else
			return 0.0;
	}
	
	private double getCredit(double hRewTime, int stepI, int stepsBeforeCurrent){
		//println("stepsBeforeCurrent: " + stepsBeforeCurrent);
		double credit = 0;
		if (this.distClass == "previousStep") {
			if (stepsBeforeCurrent == 1) { credit = 1; } // if called between steps, this is the time step before the one that just completed (and so this reward came before that completion)
			else { credit = 0; }
			return credit;
		}
		else if (this.distClass == "immediate") {
			if (stepsBeforeCurrent == 0) { credit = 1; }
			else { credit = 0; }
			return credit;
		}
		else if (this.distClass == "uniform") {
			double relativeFarBound = hRewTime - this.timeStepsInWindow.get(stepI).startTime;
			double relativeNearBound = getRelNearBound(stepI, hRewTime);
			if ((relativeFarBound > this.windowStart) && (relativeNearBound < this.windowEnd)) {
				if (relativeNearBound < this.windowStart)  relativeNearBound = this.windowStart;
				if (relativeFarBound > this.windowEnd)  relativeFarBound = this.windowEnd;
				// e.g. if windowStart is 0.2 sec and a time step started at 0.3 secs ago and ended 0.1 secs ago, 
				// for credit it would be considered to have ended at 0.2 secs ago (removing time that has no 
				// support in the pdf).
				credit = (relativeFarBound - relativeNearBound) /
								(this.windowEnd - this.windowStart);
			}
			else
				credit = 0;
			
		}
		else{
			System.err.println("Using an invalid distribution class for credit assignment!");
			System.exit(1);
		}
		if (credit < 0 || credit > 1) {
			println("bad credit: " + credit);
		}
		return credit;
	}
	
	
	public double getCreditPastElig(int sampleI, int stepsBeforeCurrent, double currTime){
		if (this.distClass == "previousStep") { //// I'm not entirely confident of what the implications are for previousStep and immediate, though I don't think this method will be used meaningfully for them.
			return isSampleFinished(sampleI) ? 1.0 : 0.0;
		}
		else if (this.distClass == "immediate") {
			return isSampleFinished(sampleI) ? 1.0 : 0.0;
		}
		else if (this.distClass == "uniform") {			
			/*
			 * Possible credit over time for uniform dist can be represented as a parallelogram, where the longer sides 
			 * are traversed with time and the orthogonal dimension is the credit received at a particular instant of time.
			 */
			TimeStepForCred step = this.timeStepsInWindow.get(sampleI);
			double uniformWdthOfSupport = this.windowEnd - this.windowStart;
			//println("uniformWdthOfSupport: " + uniformWdthOfSupport);
			//println("start time: " + String.format("%f", step.startTime));
			//println("end time: " + String.format("%f", step.endTime));
			double stepWidth = Math.min(step.endTime, currTime) - step.startTime; // When step isn't finished yet, assume it ends now (might make flawed estimates with temp effect if credit has already been received). 
			//println("stepWidth: " + stepWidth);

			// If stepWidth is 0, the steps are shorter than can be represented by current time-keeping. 
			// They can never receive credit, so they are automatically used up. I expect this to only 
			// be the case when not training.
			if (stepWidth == 0) {
				if (step.startTime >= currTime) // delay in calling this (observed from processNewHRew()) and a step has completed completely in the "future" (where the present is defined by reward time, not the real present); not a problem
					return 0;
				if (inTrainSess) {
					System.err.println("Steps of zero duration are being thrown out during training. Exiting from CreditAssign.");
					throw new RuntimeException("Steps of zero duration are being thrown out during training. Exiting from CreditAssign.");
				}
				return 1.0; 
			}
			
			double t = currTime - (step.startTime + this.windowStart); // time since crediting started
			//println("t (time since first credit): " + t);
			
			double pgramRectLength = Math.abs(uniformWdthOfSupport - stepWidth); // The length of the longer side of the parallelogram with the 
																				 // triangle sections removed (could be zero)
			//println("pgramRectLength: " + pgramRectLength);
			double pgramTriSide = Math.min(uniformWdthOfSupport, stepWidth); // The length of isosceles right triangle and also is the 
																			 // width of parallelogram in direction orthogonal to longest side
			//println("pgramTriSide: " + pgramTriSide);
			
			double pgramArea = pgramTriSide * (pgramTriSide + pgramRectLength); // total area of the parallelogram
			//println("pgramArea: " + pgramArea);
			
			double rampUpArea = Math.pow(Math.min(pgramTriSide, Math.max(0, t)), 2) / 2.0; // isosceles right triangle of max side length pgramTriSide
			//println("rampUpArea: "+ rampUpArea);
			double rectArea = Math.max(0, Math.min(pgramRectLength, t - pgramTriSide)) * pgramTriSide;
			//println("rectArea: " + rectArea);
			double rampDnArea = Math.max(0, Math.pow(Math.min(pgramTriSide, Math.max(0, t - (pgramTriSide + pgramRectLength)) ), 2) / 2.0); // isosceles right triangle of max side length pgramTriSide
			//println("rampDnArea: "+ rampDnArea);
			
			double pastArea = rampUpArea + rectArea + rampDnArea;
			double creditPastElig = (pgramArea == 0.0) ? 0.0 : Math.min(1.0 , pastArea / pgramArea);
			//println("credit in getCreditPastElig(): " + creditPastElig);
			return creditPastElig;
		}
		else{
			System.err.println("Using an invalid distribution class for credit assignment!");
			System.exit(1);
			return 0.0;
		}
	}
	

	private void removeSamplesWNoNewCred(ArrayList<Sample> samples){
		for (int i = 0; i < samples.size(); i++) {
			//System.out.print(i + ": " );
			if (samples.get(i).creditUsedLastStep == 0.0) {
				samples.remove(i);
				i--;
				//System.out.println("Removing sample with no new credit");
			}
			//else {
			//	System.out.println("Not removing sample with new credit of " + 
			//			samples.get(i).creditUsedLastStep);
			//}
		}
	}
	private void removeSamplesWZeroRew(ArrayList<Sample> samples){
		for (int i = 0; i < samples.size(); i++) {
			if (samples.get(i).label == 0) {
				samples.remove(i);
				i--;
				//System.out.println("Removing sample with zero reward");
			}
		}
	}
	private void setWtToCredLastStep(ArrayList<Sample> samples){
		for (int i = 0; i < samples.size(); i++) 
			samples.get(i).weight = samples.get(i).creditUsedLastStep;
	}
	private boolean allSamplesHaveZeroRew() {
		boolean allHaveZeroRew = true;
		for (int i = 0; i < activeSamples.size(); i++) {
			if (activeSamples.get(i).unweightedRew != 0.0)
				allHaveZeroRew = false;
		}
		return allHaveZeroRew;
	}
	
	public double drawDelay() {
		double delay = 0;
		if (this.distClass == "previousStep")
			delay = 0;
		else if (this.distClass == "uniform")
			delay =  this.windowStart + (randGenerator.nextDouble() * (this.windowEnd - this.windowStart));
		else {
			System.err.println("Using an invalid distribution class for credit assignment!");
			System.exit(1);
		}
		return delay;
	}
	
	
	
	

	public static void main(String[] args) {
		//// CREATE CreditAssignParamVec
		String distClass = "uniform";
		double creditDelay = 0.2;
		double windowSize = 0.6;
		boolean extrapolateFutureRewards = true;
		CreditAssignParamVec credAssignParams = new CreditAssignParamVec(distClass, creditDelay, 
															windowSize, extrapolateFutureRewards,
															false, true);
		CreditAssign credA = new CreditAssign(credAssignParams);
		credA.setInTrainSess(0.0, true);
		
		for (int i = 0; i < 10; i++) {
			credA.println("\n\n");
			double[] feats = {i, i + 10};
			credA.recordTimeStepEnd((0.2 * i));
			credA.recordTimeStepStart(feats, (0.2 * i));
			credA.processNewHReward((0.2 * i) == 1.8?1:0, (0.2 * i));
			Sample[] samplesForUpdate = credA.processSamplesAndRemoveFinished(0.2 * i, true);
			System.out.println("\nsamples for update: \n" + Arrays.toString(samplesForUpdate));
			System.out.println("\nactiveSamples: \n" + credA.activeSamples);
		}
		double[] feats2 = {10, 20};
		credA.processNewHReward(1.0, 1.9);
		credA.recordTimeStepEnd(2.0);
		credA.recordTimeStepStart(feats2, 2.0);
		credA.removeFinishedTimeSteps(2.0, true);
		credA.processNewHReward(1.0, 2.0);
		Sample[] creditedSamples2 = credA.processSamplesAndRemoveFinished(2.0, true);
		credA.println("\n\n\nCredited samples: " + Arrays.toString(creditedSamples2));
		credA.println("");
		credA.clearHistory(); 
		credA.processNewHReward(2.0, 2.0);
		Sample[] clearedCreditedSamples = credA.processSamplesAndRemoveFinished(2.0, true);
		credA.println("\n\n\nCredited samples after clearing: " + Arrays.toString(clearedCreditedSamples));
		
		
		// calculation of example in figure of journal paper
		System.out.println("\n\n\n-----calculation of example in figure of journal paper-----");
		credA.recordTimeStepStart(feats2, (3.25));
		credA.processNewHReward(2.0, 3.4);
		credA.recordTimeStepEnd(3.45);
		credA.recordTimeStepStart(feats2, 3.45);
		credA.processNewHReward(2.0, 3.85);
		// uncomment the next three lines for the intermediate label example
		creditedSamples2 = credA.processSamplesAndRemoveFinished(3.9, true);
		credA.println("activeSamples (where example label is): " + credA.activeSamples);
		credA.println("\n\n\nCredited samples: " + Arrays.toString(creditedSamples2));
		
		credA.processNewHReward(2.0, 4.1);
		
		// uncomment the next three lines for the final label example
		//creditedSamples2 = credA.processSamplesAndRemoveFinished(4.3, true);
		//credA.println("activeSamples: " + credA.activeSamples);
		//credA.println("credited samples (where example label is): " + Arrays.toString(creditedSamples2));
		
		
	}
	
	public void print(String s) {
		System.out.print(s);
	}
	public void println(String s) {
		System.out.println(s);
	}
}

