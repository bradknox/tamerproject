package edu.utexas.cs.tamerProject.modeling.weka;



import weka.core.Instance;
import weka.core.FastVector;
import weka.core.Attribute;
import weka.core.Utils;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.WrapperSubsetEval;

import java.io.*;
import java.lang.Math;
import java.util.Date;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;

import edu.utexas.cs.tamerProject.modeling.ClassificationSample;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.weka.kNN.KNN;

public class WekaClassifier extends WekaModel {
	
	public static int numClasses = 2;


	public WekaClassifier(int numFeatures) {
		super(numFeatures);
	}
	public WekaClassifier(String modelName, int numFeatures){
		super(modelName, numFeatures);
	}
	

	protected void initData(int numFeatures){
		this.numAttributes = numFeatures + 1;
		lowImportancePrint("Instantiating batch model with " + numAttributes
                           + " attributes.");
        attrInfo = new FastVector();
        for (int i = 0; i < numFeatures; i++){
            attrInfo.addElement(new Attribute("" + (i+1)));
        }
        FastVector labelVec = new FastVector(WekaClassifier.numClasses);
        for (int i = 0; i < numClasses; i ++) {
        	labelVec.addElement(i + "");
        }
        attrInfo.addElement(new Attribute("label", labelVec));

        data = new Instances("Classifier instances", attrInfo, 0);
        data.setClassIndex(data.numAttributes() - 1);   // identify which variable is the dependent variable (aka label)        
        //System.out.println("The instances metainfo: " + data);
	}


	protected void setUpClassifiers(String modelName){
		this.modelName = modelName;
        try{
        	lowImportancePrint("Given model name: " + modelName);
        	if (modelName.equals("")) {
        		modelName = "weka.classifiers.functions.Logistic -R 1";
        	}
        	
        	
        	if (modelName.contains("Logistic")) {
	    		Logistic logRegr = new Logistic();
	    		this.classifiers.add(logRegr);
        	}
        	else{
	    		String[] tmpOptions;
	    		String classname;
	    		tmpOptions     = Utils.splitOptions(modelName);
	    		classname      = tmpOptions[0];
	    		tmpOptions[0]  = "";
	    		Classifier cls = (Classifier) Utils.forName(Classifier.class, classname, tmpOptions);
	    		this.classifiers.add(cls);
        	}



        	AttributeSelectedClassifier attrSelClassifier = new AttributeSelectedClassifier();
			//CfsSubsetEval eval = new CfsSubsetEval();
        	WrapperSubsetEval eval = new WrapperSubsetEval();
        	eval.setClassifier(this.classifiers.get(0)); // use base classifier for evaluation
			GreedyStepwise search = new GreedyStepwise();
			search.setSearchBackwards(true);
			attrSelClassifier.setClassifier(this.classifiers.get(0)); // also use base classifier for classification on reduced attribute set
			attrSelClassifier.setEvaluator(eval);
			attrSelClassifier.setSearch(search);
        	this.classifiers.add(attrSelClassifier);
        }
        catch (Exception e){
            System.out.println("Exception while initializing classifiers: " + e);
        }
        classifier = classifiers.get(0);
        //System.out.println("Classifier in use: " + classifier);
	}



	
	
    public void addInstance(ClassificationSample sample){
    	double weight = sample.weight;
    	//System.out.println("adding instance <"+ Arrays.toString(sample.feats) + ">;  " + sample.label);
    	if (this.numAttributes != sample.feats.length + 1) {
    		System.err.println("The number of attributes used to instantiate the model doesn't match the number in the sample to be added.");
    		System.err.println("Number from instantiation: " + this.numAttributes + ". Number in sample: "  + (sample.feats.length + 1));
    		System.exit(1);
    	}
    	Instance inst = new Instance(this.numAttributes);
    	for (int i = 0; i < sample.feats.length; i++) {
    		inst.setValue((Attribute)attrInfo.elementAt(i), sample.feats[i]);
    	}
    	inst.setDataset(data);
    	inst.setWeight(weight);
    	inst.setClassValue(sample.label);
        data.add(inst);
        System.out.println("Samples collected: " + data.numInstances());
        uniques.add(new Double(sample.unique));
    }
    

    public Instance makeInstance(Sample sample) {
        return makeInstance(sample.feats, sample.label + "", sample.weight);
    }
    public Instance makeInstance(double[] feats, String label, double weight) {
    	if (this.numAttributes != feats.length + 1) {
    		System.err.println("The number of attributes used to instantiate the model doesn't match the number in the sample to be added.");
    		System.err.println("Number from instantiation: " + this.numAttributes + ". Number in sample: "  + (feats.length + 1));
    		System.exit(1);
    	}
    	Instance inst = new Instance(this.numAttributes);
    	for (int i = 0; i < feats.length; i++) {
    		inst.setValue((Attribute)attrInfo.elementAt(i), feats[i]);
    	}
    	inst.setDataset(data);
    	inst.setWeight(weight);
    	inst.setClassValue(label);
    	return inst;
    }


    


	
	

    public static void main(String[] args) {
        WekaClassifier wbi = new WekaClassifier("Logistic", 2);
        
        // three samples for the model to train on
        double[] feats1 = {0.0, 1.0};
        wbi.addInstance(wbi.makeInstance(feats1, "1", 1.0), -1);
        double[] feats2 = {0.0, 0.0};
        wbi.addInstance(wbi.makeInstance(feats2, "1", 1.0), -1);
        double[] feats3 = {1.0, 1.0};
        wbi.addInstance(wbi.makeInstance(feats3, "0", 1.0), -1);
        wbi.buildModel();

            
        double[] testFeats1A = {0.0, 1.0};
        double classification = wbi.classifyInstance(testFeats1A);
        System.out.println("\nclassification: " + classification);
		double[] predDist = wbi.getClassDist(testFeats1A);
		System.out.println("prediction distribution for " + Arrays.toString(testFeats1A) 
													+ ": " + Arrays.toString(predDist));
        
        double[] testFeats2A = {1.0, 0.0};
        classification = wbi.classifyInstance(testFeats2A);
        System.out.println("\nclassification: " + classification);
        double[] predDist2 = wbi.getClassDist(testFeats2A);
		System.out.println("prediction distribution for " + Arrays.toString(testFeats2A) 
													+ ": " + Arrays.toString(predDist2));
 
        
        wbi.evaluateModel();
    }

}