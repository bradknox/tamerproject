package edu.utexas.cs.tamerProject.modeling;
	
import java.util.Arrays;

public class ClassificationSample{
	
	public double[] feats;
	public String label = "";
	public double weight = 1.0;
	public int unique = -1;
	
	public ClassificationSample(double[] feats, double weight){
		this.feats = feats;
		this.weight = weight;
	}
	
	public ClassificationSample(double[] feats, double weight, int unique){
		this(feats, weight);
		this.unique = unique;
	}
	
	public ClassificationSample(double[] feats, String label, double weight){
		this(feats, weight);
		this.label = label;
	}
	
	public ClassificationSample(double[] feats, String label, double weight, int unique){
		this(feats, weight, unique);
		this.label = label;
	}

	
//	
//	// attributes are simply the feats with the reward label appended. formatted for weka.
//	public double[] getAttributes(){
//		double[] attributes = Arrays.copyOf(this.feats, feats.length + 1);
//		attributes[attributes.length - 1] = this.label;
//		return attributes;
//	}
	
	public String toString(){
		String s = "\n";
		s += "feats: " + Arrays.toString(feats) + "\n";
		s += "label: " + label + "\n";
		s += "weight: " + weight + "\n";
		s += "unique: " + unique + "\n";
		return s;
	}
}


