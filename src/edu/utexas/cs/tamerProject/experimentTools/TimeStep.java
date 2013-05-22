package edu.utexas.cs.tamerProject.experimentTools;

import java.util.ArrayList;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.agents.tamer.HRew;

public class TimeStep{
	public Observation o;
	public Action a;
	public double rew; // MDP reward
	public ArrayList<HRew> hRewList;
	public double timeStamp;
	public boolean startOfEp = false;
	public boolean endOfEp = false;
	public boolean training = true;
	// boolean demo = false;
	// boolean beforeUserAction = false;
	
	public String toString() {
		return RecordHandler.stepToStr(this);
	}
}