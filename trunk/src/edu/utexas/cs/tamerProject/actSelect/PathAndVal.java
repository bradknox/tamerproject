package edu.utexas.cs.tamerProject.actSelect;

import java.util.ArrayList;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

public class PathAndVal {
	public ArrayList<Action> pathActs;
	public ArrayList<Observation> pathObs;
	public ArrayList<Double> discountedRews;
	public ArrayList<Double> rawRews;
	public double leafValue;
	public Action leafAction = null;
	
	
	public double getVal(){
		double val = leafValue;
		for (Double discRew: discountedRews){
			val += discRew.doubleValue();
		}
		return val;
	}
	public Action getFirstAct() {
		if (pathActs.size() != 0)
			return pathActs.get(0);
		else
			return leafAction;
	}
	
	/**
	 * This version of the constructor assumes a zero leaf value, which corresponds
	 * to terminal states.
	 */
	public PathAndVal(){
		this(0);
	}
	public PathAndVal(double leafValue){
		pathActs = new ArrayList<Action>();
		pathObs = new ArrayList<Observation>();
		discountedRews = new ArrayList<Double>();
		rawRews = new ArrayList<Double>();
		this.leafValue = leafValue;
	}
	public PathAndVal(double leafValue, Action leafAction){
		pathActs = new ArrayList<Action>();
		pathObs = new ArrayList<Observation>();
		discountedRews = new ArrayList<Double>();
		rawRews = new ArrayList<Double>();
		this.leafValue = leafValue;
		this.leafAction = leafAction;
	}
	public PathAndVal(ArrayList<Action> pathActs, ArrayList<Observation> pathObs, 
			ArrayList<Double> discountedRews, ArrayList<Double> rawRews, double leafValue){
		this.pathActs = pathActs;
		this.pathObs = pathObs;
		this.discountedRews = discountedRews;
		this.rawRews = rawRews;
		this.leafValue = leafValue;
	}
	
	public void addObsAndActBeforePath(Observation obs, Action act, double discRew, double rawRew){
		this.pathActs.add(0, act);
		this.pathObs.add(0, obs);
		this.discountedRews.add(0, Double.valueOf(discRew));
		this.rawRews.add(0, Double.valueOf(rawRew));
	}
	public void addObsAndActToPathEnd(Observation obs, Action act, double discRew, double rawRew){
		int pathSize = this.pathActs.size();
		this.pathActs.add(pathSize, act);
		this.pathObs.add(pathSize, obs);
		this.discountedRews.add(pathSize, Double.valueOf(discRew));
		this.rawRews.add(pathSize, Double.valueOf(rawRew));
	}
	
	/**
	 * output is 
	 * 
	 * pathValue; actions in path; (disc rew, undisc rew) for each action; value at leaf
	 */
	public String toString(){
		String s = this.getVal() + "; ";
		for (Action act: this.pathActs) {
			s+= "[";
			if (act.intArray.length > 0){	
				s += "[";
				for (int i = 0; i < act.intArray.length; i++){
					s += act.intArray[i];
					if (i < act.intArray.length - 1)
						s += " ";
				}
				s += "]";
			}
			if (act.doubleArray.length > 0){
				s += "[";
				for (int i = 0; i < act.doubleArray.length; i++){
					s += act.doubleArray[i];
					if (i < act.doubleArray.length - 1)
						s += " ";
				}
				s += "]";
			}
			if (act.charArray.length > 0){
				s += "[";
				for (int i = 0; i < act.charArray.length; i++){
					s += act.charArray[i];
					if (i < act.charArray.length - 1)
						s += " ";
				}
				s += "]";
			}
			s += "]";
		}
		for(int rewI = 0; rewI < this.discountedRews.size(); rewI++){
			s += "; (";
			s+= String.format("%.3g", this.discountedRews.get(rewI).doubleValue()) + ", ";
			s+= String.format("%.3g", this.rawRews.get(rewI).doubleValue()) + ")";
		}
		s += "(" + this.leafValue + ")"; 
		
		return s;
	}
}
