package edu.utexas.cs.tamerProject.envModels;

/**
 * This class takes in an instance of a SampleableEnvBase (e.g., LoopMaze) and 
 * creates sampleable reward and transition models for that environment. Such 
 * models are used when planning is required by the agent algorithm (e.g., for 
 * DPAgent). In general, it's better to use EnvWrapper when possible, rather 
 * than creating models that mimic the environment, as in the rewModels and 
 * transModels directories, which requires that any change to one is made in 
 * the other (bad coding that prior constraints forced upon me). Some 
 * environments have not yet been adapted to inherit SampleableEnvBase, but
 * such adaptation should be easy.
 * 
 * All environments used by this class should be a separate instance 
 * than that used by RLGlue. Specifically, this strategy prevents 
 * multi-threading problems and allows a potential random seed for the 
 * environment to create the same experience for two identical (non-random)
 * agents.
 * 
 */
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import rlVizLib.Environments.SampleableEnvBase;


import edu.utexas.cs.tamerProject.modeling.templates.ObsActModel;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndTerm;


public class EnvWrapper {
	public RewModelWrapped rewModel;
	public TransModelWrapped transModel;
	
	public EnvWrapper(SampleableEnvBase env){
		rewModel = new RewModelWrapped(env);
		transModel = new TransModelWrapped(env);
	}
}


class RewModelWrapped implements ObsActModel {
	SampleableEnvBase env;
	public RewModelWrapped(SampleableEnvBase env) {this.env = env;}
	
	public double predictLabel(Observation obs, Action act) {
		return env.sampleNextObs(obs, act).r;
	}
}


class TransModelWrapped extends EnvTransModel{
	SampleableEnvBase env;
	public TransModelWrapped(SampleableEnvBase env) {this.env = env;}
	
	@Override
	public ObsAndTerm sampleNextObsNoForceCont(Observation obs, Action act) {
		return ObsAndTerm.rotToObsAndTerm(env.sampleNextObs(obs, act));
	}

	@Override
	public ObsAndTerm getStartObs() {
		return ObsAndTerm.rotToObsAndTerm(env.sampleStartObs());
	}
	
	public boolean isObsLegal(Observation obs){
		return env.isObsLegal(obs);
	}
	public boolean isObsTerminal(Observation obs){
		return env.isObsTerminal(obs);
	}
}