package edu.utexas.cs.tamerProject.modeling;
	
import java.util.Arrays;

public class Sample implements Cloneable{
//	protected boolean REGRESSION_SAMPLE = true; // TODO remove all refs to this variable
	
	public double[] feats;
	public double label = 0;
	public double unweightedRew = 0;
	public double weight = 1;
	public double creditUsedLastStep = 0;
	public double usedCredit = 0;

	
	public int unique = -1;
	////public boolean newSample = True;
	
	public Sample(double[] feats, double weight){
		this.feats = feats;
		this.weight = weight;
	}
	
	public Sample(double[] feats, double weight, int unique){
		this(feats, weight);
		this.unique = unique;
	}
	
	public Sample(double[] feats, double label, double weight){
		this(feats, weight);
		this.label = label;
	}
	
	public Sample(double[] feats, double label, double weight, int unique){
		this(feats, weight, unique);
		this.label = label;
	}

//	public boolean isRegressionSample() {return REGRESSION_SAMPLE;}
//	protected void setIsRegressionSample(boolean isRegressionSample){this.REGRESSION_SAMPLE = isRegressionSample;}
	
	
	// attributes are simply the feats with the reward label appended. formatted for weka.
	public double[] getAttributes(){
		double[] attributes = Arrays.copyOf(this.feats, feats.length + 1);
		attributes[attributes.length - 1] = this.label;
		return attributes;
	}
	
	public String toString(){
		String s = "\n";
		s += "feats: " + Arrays.toString(feats) + "\n";
		s += "label: " + label + "\n";
		s += "unweighted reward: " + unweightedRew + "\n";
		s += "weight: " + weight + "\n";
		s += "creditUsedLastStep: " + creditUsedLastStep + "\n";
		s += "usedCredit: " + usedCredit + "\n";
		s += "unique: " + unique + "\n";
		return s;
	}
	
	public Sample clone() {
		Sample cloneSample = null;
		
		try { 
			cloneSample = (Sample)super.clone();
			cloneSample.feats = feats.clone();
		} 
		catch (CloneNotSupportedException e) {e.printStackTrace();}
		
		return cloneSample;
	}
}


