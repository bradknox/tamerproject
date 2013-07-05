package edu.utexas.cs.tamerProject.agents.tamerrl;

import java.util.ArrayList;
import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.params.Params;
import edu.utexas.cs.tamerProject.agents.CreditAssign;
import edu.utexas.cs.tamerProject.agents.CreditAssignParamVec;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;


/**
 * HInfluence calculates a parameter (beta in the ICML-11 workshop paper) that determines
 * the relative influence of the model of human reward for TAMER+RL. 
 * <p>
 * This class implements both simple annealing, starting at COMP_PARAM, and the more complex
 * eligibility module, which outputs a parameter in range [0, COMB_PARAM].
 * 
 * See TamerRLAgent for an example of how to use this class.
 */
public class HInfluence {
	
	private String INFLUENCE_METHOD; // annealedParam or eligTrace
	public double COMB_PARAM = 1.0;
	private double STEP_DECAY_FACTOR;
	private double EP_DECAY_FACTOR;
	private String TRACE_STYLE = "accumulating"; // replacing or accumulating
	private double ACCUM_FACTOR = 0.0; // in range (0,1]
	
	public FeatGenerator featGen;
	boolean stateOnly = false;
	Action stateOnlyAction = new Action();
//	private String featInput;
	public CreditAssign credA;
	
	public double[] traces;
	public double[] lastStepTraces;
	private double[] minFeats;
	private double[] maxFeats;
	
	public HInfluence(String influenceMethod, double combParam, String envName, 
								Params tamerParams, GeneralAgent agent, boolean stateOnly) {
		this.INFLUENCE_METHOD = influenceMethod;
		this.COMB_PARAM = combParam;
		Params params = Params.getHInflParams(envName, influenceMethod.equals("eligTrace"));
		if (agent.getClass().toString().contains("TamerRLAgent") && ((TamerRLAgent)agent).USING_PY_MC_MODEL)
			params.setPyMCParams(this.getClass().toString(), influenceMethod.equals("eligTrace"));
		this.STEP_DECAY_FACTOR = Double.valueOf(params.hInflParams.get("stepDecayFactor")).doubleValue();
		this.EP_DECAY_FACTOR = Double.valueOf(params.hInflParams.get("epDecayFactor")).doubleValue();
		if (this.INFLUENCE_METHOD.equals("annealedParam")){
			this.traces = new double[1];
			this.setTracesToMax();
		}
		else if (this.INFLUENCE_METHOD.equals("eligTrace")) {
			//// set featGen
			this.ACCUM_FACTOR = Double.valueOf(params.hInflParams.get("accumFactor")).doubleValue();
			this.TRACE_STYLE = params.hInflParams.get("traceStyle");
			this.featGen = agent.getFeatGen(params);
			//// set CreditAssign
			CreditAssignParamVec credAssignParams = new CreditAssignParamVec(tamerParams.distClass, 
																	tamerParams.creditDelay, 
																	tamerParams.windowSize,
																	tamerParams.extrapolateFutureRew,
																	tamerParams.delayWtedIndivRew,
																	tamerParams.noUpdateWhenNoRew); // extrapolation matches TAMER b/c then newly credited samples are used here in sync with when they are added to H-hat
			this.credA = new CreditAssign(credAssignParams);
			this.stateOnly = stateOnly;
			if (this.stateOnly) {
				this.maxFeats = this.featGen.getMaxPossSFeats();
				this.minFeats = this.featGen.getMinPossSFeats();
				this.stateOnlyAction.intArray = new int[1];
				this.stateOnlyAction.doubleArray = new double[0];
			}
			else {
				this.maxFeats = this.featGen.getMaxPossFeats();
				this.minFeats = this.featGen.getMinPossFeats();
			}
			this.traces = new double[maxFeats.length];
			this.lastStepTraces = new double[maxFeats.length];
		} 
		
	}	

	public void setAccumFactor(double val) {
		this.ACCUM_FACTOR = val;
	}
	
	public void setTracesToMax() {
		for (int i = 0; i < this.traces.length; i++) {
			this.traces[i] = 1.0;
		}
	}
	
	public void setTraceStyle(String traceStyle) {
		this.TRACE_STYLE = traceStyle;
	}
	
	public void setStepDecayFactor(double stepDecayFactor)
	{
		this.STEP_DECAY_FACTOR = stepDecayFactor;
	}
	public void setEpDecayFactor(double epDecayFactor)
	{
		this.EP_DECAY_FACTOR = epDecayFactor;
	}
	
	private double[] linearNormFeats(double[] feats) {
		double[] normFeats = new double[feats.length];
//		System.out.println("feats.lengths: " + feats.length);
		for (int i = 0; i < feats.length; i++) {
			normFeats[i] = (feats[i] - this.minFeats[i]) / (this.maxFeats[i] - this.minFeats[i]);
			if ((maxFeats[i] - minFeats[i] == 0)) {
				normFeats[i] = 0.0;
//				System.out.println("feats[" + i + "]: " + feats[i]);
//				System.out.println("(maxFeats[i] - minFeats[i]): " + (maxFeats[i] - minFeats[i]));
			}
			if (normFeats[i] < 0){
				System.out.println("components of negative normed feats -- feats[i]: " + feats[i] 
				                    + ", minFeats[i]: " + minFeats[i] + ", maxFeats[i]: " + maxFeats[i]);
			}
		}
		return normFeats;
	}

	
	public void episodeEndUpdate() {
		this.lastStepTraces = Arrays.copyOf(this.traces, this.traces.length);
		if (this.EP_DECAY_FACTOR != 1.0)
			this.epDecayEligTraces();
	}
	
	public double getHInfluence (Observation o, Action a) {
		//System.out.println("action in getHInfluence(o, a): " + Arrays.toString(a.intArray));
		return getHInfluence(o, a, false);
	}
	public double getHInfluence (Observation o, Action a, boolean lastStepInf) {
		double[] tracesForInf = this.traces;
		if (lastStepInf) tracesForInf = this.lastStepTraces;
		if (this.INFLUENCE_METHOD.equals("annealedParam")){
			return (tracesForInf[0]) * this.COMB_PARAM;
		}
		return this.getHInfluence(this.getFeats(o, a), lastStepInf);
	}
	
	public double getHInfluence (int[] intVars1, double[] doubleVars1, char[] charVars1,
			int[] intVars2, double[] doubleVars2) {
		if (this.INFLUENCE_METHOD.equals("annealedParam")){
			return (this.traces[0]) * this.COMB_PARAM;
		}
		return this.getHInfluence(this.getFeats(intVars1, doubleVars1, charVars1, intVars2, doubleVars2));
	}
	public double getHInfluence(double[] feats) {
		return getHInfluence(feats, false);
	}
	
	public double getHInfluence(double[] feats, boolean lastStepInf) {
		double[] tracesForInf = this.traces;
		if (lastStepInf) tracesForInf = this.lastStepTraces;
		if (this.INFLUENCE_METHOD.equals("annealedParam")){
			return (tracesForInf[0]) * this.COMB_PARAM;
		}
		else if (this.INFLUENCE_METHOD.equals("eligTrace")) {

			double eligAndFeatDotPr = 0;
			double normFeatsL1Norm = 0;
			double[] normFeats = this.linearNormFeats(feats);
			for (int i = 0; i < normFeats.length; i++) {
				//// if feature values need normalization, it could be applied here
				eligAndFeatDotPr += normFeats[i] * Math.min(1.0, tracesForInf[i]);
				normFeatsL1Norm += normFeats[i];
				if (tracesForInf[i] < 0) {
					System.out.println("\nfound a trace below 0!!!");
					System.out.println("normFeats[i]: " + normFeats[i]);
					System.out.println("tracesForInf[i]: " + tracesForInf[i]);
				}
				
//				System.out.print("norm feat: " + normFeats[i]);
//				System.out.println("normFeatsL1Norm: " + normFeatsL1Norm);

			}
//			System.out.println("normFeatsL1Norm: " + normFeatsL1Norm);
			double hInflWt = (this.COMB_PARAM * (eligAndFeatDotPr / normFeatsL1Norm));
//			System.out.println("hInflWt: " + hInflWt);
			return hInflWt;
		}
		else {
			System.err.println("Influence method " + this.INFLUENCE_METHOD + " not supported. Exiting.");
			System.exit(1);
			return -1;
		}
	}
	
	public void stepUpdate(boolean inTrainSess, double stepStartTime){
		this.lastStepTraces = Arrays.copyOf(this.traces, this.traces.length);
		//// decay
		if (this.STEP_DECAY_FACTOR != 1.0)
			this.stepDecayEligTraces();
		if (this.INFLUENCE_METHOD.equals("eligTrace")) {
			//// for each step in window, grow
//			System.out.print("\n\nHInfluence ");
			Sample[] samples = this.credA.processSamplesAndRemoveFinished(stepStartTime, inTrainSess);
			if (inTrainSess){
				double totalWt = 0;
				for (Sample sample: samples) {
					this.growEligTraces(sample.feats, sample.creditUsedLastStep);
					totalWt += sample.creditUsedLastStep;
				}
//				System.out.println("Total weight from this step: " + totalWt);
			}
		}
	}
	
	
	private double[] getFeats(Observation o, Action a) {
		if (this.stateOnly) {
			//a = stateOnlyAction;
			return this.featGen.getSFeats(o);
		}
		else	
			return this.featGen.getFeats(o, a);
	}
	private double[] getFeats(int[] intVars1, double[] doubleVars1, char[] charVars1,
								int[] intVars2, double[] doubleVars2){
		Observation o = new Observation();
		o.intArray = intVars1;
		o.doubleArray = doubleVars1;
		o.charArray = charVars1;
		Action a = new Action();
		a.intArray = intVars2;
		a.doubleArray = doubleVars2;
		if (this.stateOnly) 
			return this.featGen.getSFeats(o);
		else	
			return this.featGen.getFeats(o, a);
	}
	
//	public void recordTimeStep(Observation o, Action a, double stepStartTime) {
//		if (this.INFLUENCE_METHOD.equals("eligTrace")) {
//			if (this.stateOnly)
//				a = this.stateOnlyAction;
//			double[] normFeats = this.linearNormFeats(this.getFeats(o, a));
//			this.credA.recordTimeStepEnd(stepStartTime);
//			this.credA.recordTimeStepStart(normFeats, stepStartTime);
//		}		
//	}
	
	// each time step will have a endTime, which is the next step's startTime
	public void recordTimeStepStart(Observation o, Action a, double startTime){
		if (this.INFLUENCE_METHOD.equals("eligTrace")) {
			if (this.stateOnly)
				a = this.stateOnlyAction;
			double[] normFeats = this.linearNormFeats(this.getFeats(o, a));
		
		//		System.out.println("number of normFeats: " + normFeats.length);
		//		System.out.println("feats before normalization: " + Arrays.toString(this.featGen.getFeats(o.intArray, o.doubleArray, 
		//				a.intArray, a.doubleArray)));
			this.credA.recordTimeStepStart(normFeats, startTime);
		}
	}
	
	// each time step will have a endTime, which is the next step's startTime
	public void recordTimeStepEnd(double endTime){
		if (this.INFLUENCE_METHOD.equals("eligTrace"))
			this.credA.recordTimeStepEnd(endTime);
	}
	
	public void stepDecayEligTraces() {
//		System.out.println("STEP_DECAY_FACTOR: " + this.STEP_DECAY_FACTOR);
		for (int i = 0; i < this.traces.length; i++){
			this.traces[i] *= this.STEP_DECAY_FACTOR;
		}
	}
	public void epDecayEligTraces() {
//		System.out.println("EP_DECAY_FACTOR: " + this.EP_DECAY_FACTOR);
		for (int i = 0; i < this.traces.length; i++){
			this.traces[i] *= this.EP_DECAY_FACTOR;
		}
	}

	
	// assumes 0 <= x <= 1 for each element x of normFeats
	private void growEligTraces(double[] normFeats, double weight){
//		System.out.println("\n\n\nweight: " + weight);
		System.out.print("g");
		System.out.flush();
//		System.out.println("this.ACCUM_FACTOR: " + this.ACCUM_FACTOR);
//		System.out.println("numFeats: " + normFeats.length);
//		System.out.println("\n\nnormalized feats: " + Arrays.toString(normFeats));
//		System.out.println("\n\ntraces: " + Arrays.toString(this.traces));
		for (int i = 0; i < normFeats.length; i++){
			
			//// update traces with normFeats
			if (this.TRACE_STYLE.equals("replacing")){ // not sure how to use weight for replacing; leaving it out might be best
				this.traces[i] = Math.max(normFeats[i], this.traces[i]);
			}
			else if (this.TRACE_STYLE.equals("accumulating")){
				this.traces[i] = Math.min( ((weight * normFeats[i] * this.ACCUM_FACTOR) + this.traces[i]), 1.0);
//				if (this.traces[i] < 0) {
//					System.out.println("components for sub-zero trace -- weight: " + weight 
//								+ "normFeats[i]: " + normFeats[i] + "this.ACCUM_FACTOR: " + this.ACCUM_FACTOR);
//				}
			}
			else {
				System.err.println("Trace style " + this.TRACE_STYLE + "  is not supported in " 
										+ this.getClass() + ". Exiting.");
				System.exit(0);
			}
		}		
	}	
	
}