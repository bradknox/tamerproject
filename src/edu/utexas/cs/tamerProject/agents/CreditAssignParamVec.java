package edu.utexas.cs.tamerProject.agents;

import java.util.Arrays;

// A CreditAssignParamVec object is passed to the constructor of a CreditAssign object
public class CreditAssignParamVec{
	String distClass; // class of distribution over delay; can be immediate or uniform or ...
	double creditDelay; // delay before window starts; in seconds
	double windowSize; // window of credit assignment goes from creditDelay to (creditDelay + windowSize)
	//double[] distParams = new double[];
	int uniqueStart;
	boolean EXTRAPOLATE_FUTURE_REW;
	boolean delayWtedIndivRew = false; // this uses delay-weighted, individual reward (named in the original TAMER journal paper 
										// and first used in the K-CAP09 paper) instead of the current d-w, aggregate reward
	boolean noUpdateWhenNoRew = false; // this should only be used when delayWtedIndivRew is true
	
	public CreditAssignParamVec(String distClass, 
						double creditDelay, 
						double windowSize, 
						int uniqueStart, 
						boolean extrapolateFutureRew, 
						boolean delayWtedIndivRew,
						boolean noUpdateWhenNoRew){
		this.distClass = distClass;
		this.creditDelay = creditDelay; // amount of time that must pass after feedback to consider it for credit
		this.windowSize = windowSize;
		this.uniqueStart = uniqueStart;
		this.EXTRAPOLATE_FUTURE_REW = extrapolateFutureRew;
		this.delayWtedIndivRew = delayWtedIndivRew;
		this.noUpdateWhenNoRew = noUpdateWhenNoRew;
	}
	
	public CreditAssignParamVec(String distClass, 
						double creditDelay, 
						double windowSize, 
						boolean extrapolateFutureRew,
						boolean delayWtedIndivRew,
						boolean noUpdateWhenNoRew){
		this(distClass, creditDelay, windowSize, 0, extrapolateFutureRew, delayWtedIndivRew, noUpdateWhenNoRew);
	}
	
	public String toString(){
		String s = "";
		s += "distClass: " + this.distClass + ", ";
		s += "creditDelay: " + this.creditDelay + ", ";
		s += "windowSize: " + this.windowSize + ", ";
		s += "uniqueStart: " + this.uniqueStart + ", "; 
		s += "EXTRAPOLATE_FUTURE_REW: " + this.EXTRAPOLATE_FUTURE_REW + ", ";
		s += "delayWtedIndivRew: " + this.delayWtedIndivRew + ", ";
		s += "noUpdateWhenNoRew: " + this.noUpdateWhenNoRew;
		return s;
	}
}