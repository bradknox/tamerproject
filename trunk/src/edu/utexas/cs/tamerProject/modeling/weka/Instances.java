package edu.utexas.cs.tamerProject.modeling.weka;

import java.io.IOException;
import java.io.Reader;
import java.util.Random;

import weka.core.FastVector;

public class Instances extends weka.core.Instances {
	static final long serialVersionUID = -194123450742748L;
	  public Instances(/*@non_null@*/Reader reader) throws IOException {
		  super(reader);
	  }
	  @Deprecated public Instances(/*@non_null@*/Reader reader, int capacity)
	    throws IOException {
		  super(reader, capacity);
	  }
	  public Instances(/*@non_null@*/Instances dataset) {
		  super(dataset);
	  }
	  public Instances(/*@non_null@*/Instances dataset, int capacity) {
		  super(dataset, capacity);
	  }
	  public Instances(/*@non_null@*/Instances source, int first, int toCopy) {
		  super(source, first, toCopy);
	  }
	  public Instances(/*@non_null@*/String name,
				   /*@non_null@*/FastVector attInfo, int capacity) {
		  super(name, attInfo, capacity);
	  }
	  
	  
	  public Instances resampleWithWeights(Random random) {

		    double [] weights = new double[numInstances()];
		    for (int i = 0; i < weights.length; i++) {
		      weights[i] = instance(i).weight();
		      System.out.println("weights[i]: " + weights[i]);
		    }
		    return (Instances)resampleWithWeights(random, weights);
	  }
}
