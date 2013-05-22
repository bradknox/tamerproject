package edu.utexas.cs.tamerProject.envModels;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndTerm;

/**
 * EnvModel is an abstract class for a transition model. Example uses are for
 * planning or tree search.
 * 
 * @author bradknox
 *
 */
public abstract class EnvTransModel {
	private boolean forceContinuous = false;
	
	public void setForceCont(boolean cont){
		this.forceContinuous = cont;
		System.out.println("Forcing continuous domain in environment transition model.");
	}
	public boolean getForceCont() {return this.forceContinuous;}
	
	public ObsAndTerm sampleNextObs(Observation obs, Action act){
		/*
		 * Get next observation and whether terminal
		 */
		ObsAndTerm nextObsAndTerm = sampleNextObsNoForceCont(obs, act);
		
		/*
		 * If next state is term and forcing continuous, then get start instead
		 */
		if (nextObsAndTerm.getTerm() && forceContinuous) {
			nextObsAndTerm = getStartObs();
		}
		return nextObsAndTerm;
	}
	public boolean isObsLegal(Observation obs){
		return true;
	}
	public boolean isObsTerminal(Observation obs){
		return true;
	}
	
	public abstract ObsAndTerm sampleNextObsNoForceCont(Observation obs, Action act);
	public abstract ObsAndTerm getStartObs(); // corresponds to environment's env_start() method
}
