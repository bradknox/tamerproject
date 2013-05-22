package edu.utexas.cs.tamerProject.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.envModels.EnvTransModel;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.ObsActModel;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.utils.encapsulation.IndexAndVal;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndAct;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndTerm;

public abstract class TreeSearchNode {
	protected TreeSearchNode parent;
	protected Observation obs;
	protected boolean terminal;
	protected TreeMap<ObsAndAct, TreeSearchNode> childrenByObsAndAct;
	
	int totalVisitsHere = 0;
		
	protected static Action[] possActions = null;
	protected static double discountFactor = -1;
	protected static Random random = new Random();

	EnvTransModel transModel;
	ObsActModel rewModel;
	public RegressionModel qFunction;
	FeatGenerator featGen;
	
	public abstract double createTreePathSample(int depthToGo);
	public static void setPossibleActions(Action[] possActions) {TreeSearchNode.possActions = possActions;}
	public static void setDiscountFactor(double discountFactor) { TreeSearchNode.discountFactor = discountFactor;}
	//public void setRewModel(ObsActModel rewModel) {this.rewModel = rewModel;}
}
