package edu.utexas.cs.tamerProject.modeling.templates;

import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;

import java.lang.Math;
import java.util.Arrays;

public abstract class IncModel extends RegressionModel{

	protected double stepSize = 1.0;
	
	public void addInstance(Sample sample){
		this.addInstance(sample, 0.0);
	}
	public abstract void addInstance(Sample sample, double predictionAugmentation);
	
}