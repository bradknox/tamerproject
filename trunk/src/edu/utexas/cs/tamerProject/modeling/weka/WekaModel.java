package edu.utexas.cs.tamerProject.modeling.weka;



import weka.core.Instance;
import weka.core.FastVector;
import weka.classifiers.Classifier;
import weka.core.converters.ArffSaver;
import weka.classifiers.Evaluation;

import java.io.*;
import java.lang.Math;
import java.util.Random;
import java.util.ArrayList;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.modeling.Sample;

/**
 * This abstract class is the parent of WekaClassifier and WekaRegressor, which interface with 
 * the Weka machine learning library. 
 * 
 * @author bradknox
 *
 */
public abstract class WekaModel {
    Instances data;
	ArrayList<Double> uniques = new ArrayList<Double>();
	transient Classifier classifier;
    transient ArrayList<Classifier> classifiers = new ArrayList<Classifier>(); // for comparing models
	String modelName;
	FastVector attrInfo;
	int numAttributes;
	boolean allowLowImportPrint = true;
	protected boolean builtOnce = false;
	public static boolean SUPPRESS_UNBUILT_MSG = false; 
    
	public WekaModel(int numFeatures) {
		initData(numFeatures);
		setUpClassifiers("");
	}
	
    public WekaModel(String modelName, int numFeatures){
		initData(numFeatures);
		setUpClassifiers(modelName);
    }

    public WekaModel(String modelName, int numFeatures, boolean allowLowImportPrint){
		this.allowLowImportPrint = allowLowImportPrint;
    	initData(numFeatures);
		setUpClassifiers(modelName);
    }
	
    protected abstract void initData(int numFeatures);
	protected abstract void setUpClassifiers(String modelName);
	
	
	
	public void clearSamplesAndReset(){
		classifiers = new ArrayList<Classifier>(); // for comparing models
		setUpClassifiers(this.modelName);
		initData(this.numAttributes);
	}

	
    
    public void addInstance(Instance inst, double unique){
        data.add(inst);
        lowImportancePrint("Samples collected: " + data.numInstances());
        uniques.add(new Double(unique));
    }
    
    public Instance makeUnlabledInstance(double[] unlabeledAttributes) {
        double[] attributes = new double[unlabeledAttributes.length + 1];
        for (int i = 0; i < unlabeledAttributes.length; i++) {
            attributes[i] = unlabeledAttributes[i];
        }
        attributes[unlabeledAttributes.length] = Instance.missingValue();
        Instance inst = new Instance(1.0, attributes);
        inst.setDataset(data);	
        return inst;
    }
    

    
    public double classifyInstance(double[] unlabeledAttributes) {
    	//System.out.println("classifying in model: " + this);
    	//System.out.println("Number of model instances: " + data.numInstances());
        Instance inst = makeUnlabledInstance(unlabeledAttributes);
        double classification = 0;
        if (classifier == null) {System.err.println("classifier is null");}
        try{ classification = classifier.classifyInstance(inst); }
        catch (Exception e){
        	if (data.numInstances() == 0) {return 0;}
            System.err.println("Exception while classifying instance: " + e);
            System.err.println("Cause: " + e.getCause());
			System.err.println("Has the classifier been built?");
			System.err.println("Number of model instances: " + data.numInstances());
            System.err.print("\nStack trace: ");
            e.printStackTrace();
        }
        return classification;
    }
    
    public double[] getClassDist(double[] unlabeledAttributes){
    	Instance inst = makeUnlabledInstance(unlabeledAttributes);
    	double[] dist = null;
        try {dist = classifier.distributionForInstance(inst);}
        catch (Exception e){
        	if (data.numInstances() == 0) {return null;}
        	if (!this.builtOnce && !SUPPRESS_UNBUILT_MSG) {
        		System.out.println("Exception while classifying instance. Classifier has not been built. Try calling buildModel().");
        	}
        	else{
	            System.err.println("Exception while classifying instance: " + e);
	            System.err.println("Cause: " + e.getCause());
				
				System.err.println("Number of model instances: " + data.numInstances());
	            System.err.print("\nStack trace: ");
	            System.err.println("Make sure that classifier supports distributionForInstance().");
	            e.printStackTrace();
        	}
        }
        return dist;
    }
    
    public void buildModel(){
        try {
            classifier.buildClassifier(data);
            //System.out.println("Classifier " + classifier.getClass().getName() + " built.");
            this.builtOnce = true;
        }
        catch (Exception e){
        	System.err.println("Exception while building classifier: " + e);
            System.err.println(".Classifier " + classifier.getClass().toString() + " will not be built.");
            System.err.println("Cause: " + e.getCause());
            System.err.println("\nStack trace: ");
            e.printStackTrace();
        }
    }
    
    
    
    
    
    public abstract Instance makeInstance(Sample sample);
    
	public void changeClassifier() {
		int currClassifierI = this.classifiers.indexOf(this.classifier);
		currClassifierI = (currClassifierI + 1) % this.classifiers.size();
		String oldClassifier = this.classifier.getClass().toString();
		this.classifier = this.classifiers.get(currClassifierI);
		System.err.println("Changed classifier from " + oldClassifier + " to " + this.classifier.getClass());
		buildModel();
		printAllInstances();
	}
	
	public Classifier getModel() {
		return this.classifier;
	}
	
	 
	public void loadDataAsArff(String envName, String timeStamp, String furtherID) {
		String dataDir = GeneralAgent.RLLIBRARY_PATH + "/tamerCommon/data/" + envName;
		System.out.println("GeneralAgent.RLLIBRARY_PATH: " + GeneralAgent.RLLIBRARY_PATH);
		String fileName = "";
		try {
			fileName = dataDir + "/" + timeStamp;
			fileName = furtherID.equals("") ? fileName : fileName + "-" + furtherID + ".arff";
			BufferedReader reader = new BufferedReader(
					new FileReader(fileName));
			this.data = new Instances(reader);
			reader.close();
			// setting class attribute
			this.data.setClassIndex(data.numAttributes() - 1);		 
			System.out.println("Weka model loaded from " + fileName);
		}
		 catch (IOException e) {
			 System.err.println("Exception while trying to load data from .arff file " + fileName + ":");
			 System.err.println(e);
			 e.printStackTrace();
			 System.exit(1);
		 }
		 while (uniques.size() < this.data.numInstances()) {
			 uniques.add(-1.0);
		 }
		 System.out.println("Instances added: " + this.data.numInstances());
		 System.out.println("to model: " + this);
	}
		
	public void saveDataAsArff(String envName, double timeStamp, String furtherID) {
		 ArffSaver saver = new ArffSaver();
		 saver.setInstances(this.data);
		 String dataDir = GeneralAgent.RLLIBRARY_PATH + "/tamerCommon/data/" + envName;
		 System.out.println("GeneralAgent.RLLIBRARY_PATH: " + GeneralAgent.RLLIBRARY_PATH);
		 try {
			 File directory = new File(dataDir);
			 directory.mkdir();
			 String fileName = dataDir + "/" + timeStamp; 
			 fileName = furtherID.equals("") ? fileName : fileName + "-" + furtherID + ".arff";
			 saver.setFile(new File(fileName));
			 saver.writeBatch();
			 System.out.println("Weka model saved to " + fileName);
		 }
		 catch (IOException e) {
			 System.err.println("Exception while trying to save data to .arff file:");
			 System.err.println(e);
			 e.printStackTrace();
		 }
	}	
	private void printAllInstances() {
		System.out.println(data.numInstances() + " instances:");
		for (int i = 0; i < data.numInstances() && i >= 0; i++){
			if (uniques.get(i) >= 0.0) {
				System.out.println(data.instance(i) + "\tunique: " + uniques.get(i)
							   + "\tweight: " + data.instance(i).weight());
			}
		}
	}
	public String dataToStr(){
		String dataStr = "[[";
		for (int i = 0; i < data.numInstances(); i++){
			if (i > 0) { dataStr += "],["; }
			dataStr += data.instance(i).toString();
			dataStr += "," + data.instance(i).weight();
		}
		dataStr += "]]";
		System.out.println("Created weka instances string: " + dataStr);
		return dataStr;
	}
	private void testAllClassifiers() {
		Classifier classifierInUse = this.classifier;
            System.out.println("\n\n\n\n\n\n\n\n------" + data.numInstances() + " samples gathered. Performing model evaluation.------\n\n\n\n\n");
            for (int i = 0; i < classifiers.size(); i++) {
                classifier = classifiers.get(i);
                System.out.println("\n\n\n\nTesting regression algorithm " + classifier.getClass());
                buildModel();
                evaluateModel();
            }
        classifier = classifierInUse;
	}
    public void evaluateModel(){
		try {
            Evaluation eval = new Evaluation(data);
            eval.evaluateModel(classifier, data);
            eval.crossValidateModel(classifier, data,
                        Math.min((data.numInstances() / 2), 10), new Random());
            System.out.println(eval.toSummaryString());
        }
        catch(Exception e) {
            System.err.println("Exception during model evaluation: " + e);
			}
    }
    
    protected void lowImportancePrint(String s) {
    	if (allowLowImportPrint)
    		System.out.println(s);
    }
    
    public boolean getBuiltOnce(){
    	return this.builtOnce;
    }
}