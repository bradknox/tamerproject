package edu.utexas.cs.tamerProject.utils.encapsulation;

import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

public class ObsAndTerm {

	private Observation o = null;
	private boolean terminal = false;
	
	public ObsAndTerm(){
		//System.err.println("New ObsAndTerm" + Arrays.toString(Thread.currentThread().getStackTrace()));	
	}
	public ObsAndTerm(Observation o, boolean term){
		this.o = o;
		this.terminal = term;
		//System.err.println("New ObsAndTerm" + Arrays.toString(Thread.currentThread().getStackTrace()));	
	}
	
	public void setObs(Observation o){
		//System.err.println("Observation changed" + Arrays.toString(Thread.currentThread().getStackTrace()));	
		//System.err.println("Old obs: " + (obsIsNull() ? "null" : this.o.doubleArray[0]));
		this.o = o;
		//System.err.println("New obs: " + (obsIsNull() ? "null" : this.o.doubleArray[0]));
	}
	public Observation getObs(){return this.o;}
	
	public void setTerm(boolean term) {	this.terminal = term; }
	public boolean getTerm() { return this.terminal; }
	
	public boolean obsIsNull(){ return this.o == null; }
	
	public static ObsAndTerm rotToObsAndTerm(Reward_observation_terminal rot) {
		return new ObsAndTerm(rot.o, rot.terminal == 1);
	}
	
	public String toString(){ return this.o + ", " + this.terminal; }
	
}
