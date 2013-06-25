package edu.utexas.cs.tamerProject.modeling.weka;



import weka.core.Instance;
import weka.core.FastVector;
import weka.core.Attribute;
import weka.core.Utils;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.M5P;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.LWL;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.functions.SMOreg;
//import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.RegressionByDiscretization;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.WrapperSubsetEval;

import java.lang.Math;
import java.util.Arrays;
import java.io.*;
import java.lang.Double;


import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.weka.kNN.KNN;

public class WekaRegressor extends WekaModel implements Externalizable{ //Serializable{
    private static final long serialVersionUID = 248L; 
    
    /*
     *  This method scares me. Later changes to this class's code could make the copy 
     *  incorrect and cause problems that are hard to track down.
     *  
     */
    public WekaRegressor deepCopy() {
    	WekaRegressor copy = new WekaRegressor(modelName, this.numAttributes - 1, false); 
    	// numAttributes, attrInfo, classifier, classifiers, modelName, and initialization of data and uniques covered by constructor
    	
    	// need: add data and uniques, 		
    	for (Double unique : this.uniques) {
    		copy.uniques.add(new Double(unique.doubleValue()));
    	}
    	for (int i = 0; i < this.data.numInstances(); i++) {
    		copy.data.add(new Instance(this.data.instance(i)));
    	}
    	
    	copy.buildModel(); // If source model hasn't built, this violates the strict idea of a deep copy, but that should be fine.
    	
    	return copy;
    }
    
    
	public WekaRegressor(int numFeatures) {
		super(numFeatures);
	}
	public WekaRegressor(String modelName, int numFeatures){
		super(modelName, numFeatures);
	}

	public WekaRegressor(String modelName, int numFeatures, boolean allowLowImportPrint){
		super(modelName, numFeatures, allowLowImportPrint);
	}

	protected void initData(int numFeatures){
		this.numAttributes = numFeatures + 1;
		lowImportancePrint("Instantiating batch model with " + numAttributes
                           + " attributes.");
		lowImportancePrint("Class: " + this);
        this.attrInfo = new FastVector();
        for (int i = 0; i < numFeatures; i++){
            this.attrInfo.addElement(new Attribute("" + (i+1)));
        }
        this.attrInfo.addElement(new Attribute("Label"));
        this.data = new Instances("Regressor instances", attrInfo, 0);
        this.data.setClassIndex(data.numAttributes() - 1);   // identify which variable is the dependent variable (aka label)        
//        System.out.println("The instances metainfo: " + data);
	}


	protected void setUpClassifiers(String modelName){
		this.modelName = modelName;
        try{
        	lowImportancePrint("Given model name: " + modelName);
        	if (modelName.equals("") || modelName.equals("IBk"))
        		this.classifiers.add(new IBk()); // kNN; works very well on MC and CP; computationally fast
        	else if (modelName.equals("M5P"))
        		this.classifiers.add(new M5P()); // performs well on Tetris
        	else if (modelName.equals("KDTree"))
        		this.classifiers.add(new KNN("KDTree"));
        	else if (modelName.equals("BallTree"))
        		this.classifiers.add(new KNN("BallTree"));
        	else if (modelName.equals("CoverTree"))
        		this.classifiers.add(new KNN("CoverTree"));
        	else if (modelName.equals("LinearNNSearch"))
        		this.classifiers.add(new KNN("LinearNNSearch"));
        	else if (modelName.equals("LinearRegression")) { 
        		String[] linRegrOpts = {"-R", "3000"};
        		LinearRegression linRegrModel = new LinearRegression();
        		linRegrModel.setOptions(linRegrOpts);
        		this.classifiers.add(linRegrModel);
        	}
        	else { // e.g., modelName = "weka.classifiers.functions.Logistic -R 1"; use this for more direct control of weka options
        		String[] tmpOptions;
        		String classname;
        		tmpOptions     = Utils.splitOptions(modelName);
        		classname      = tmpOptions[0];
        		tmpOptions[0]  = "";
        		Classifier cls = (Classifier) Utils.forName(Classifier.class, classname, tmpOptions);
        		this.classifiers.add(cls);
        	}
        	
            //String[] optsIBk = {"-X", "-I"};
            //String[] optsIBk = {"-F", "-I"}; //{"-F"}; 
            //(classifiers.get(classifiers.size() - 1)).setOptions(optsIBk);
            
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
        	
        	
            /** classifiers.add(new RegressionByDiscretization()); // works very well on MC; computationally fast
            
            classifiers.add(new PaceRegression()); // works decently on MC; needs 68 samples for tetris
            String[] optsPR = {"-S 4", "-E ols"};
            (classifiers.get(classifiers.size() - 1)).setOptions(optsPR);
            
            classifiers.add(new RBFNetwork()); // computationally fast; decent on MC; but doesn't actually predict full values
            String[] optsRBF = {"-B 1000"};
            ((RBFNetwork)classifiers.get(classifiers.size() - 1)).setOptions(optsRBF);
            
            classifiers.add(new SMOreg()); // SVM regression; starts slow but performs well later on tetris; bad on MC, very slow with >300 samples
            
            classifiers.add(new M5P()); // performs well
            //((M5P)classifiers.get(classifiers.size() - 1)).setMinNumInstances(4.0); // sets min number of instances at a leaf node
            
                                    
            classifiers.add(new LWL()); // locally weighted regression; has weka.core.UnassignedDatasetException issue; decent w/o params on MC; worse w/ gaussian
            String[] optsLWL = {"-U 1"};
            (classifiers.get(classifiers.size() - 1)).setOptions(optsLWL);

            
            classifiers.add(new SimpleLinearRegression()); // performs badly
            classifiers.add(new LinearRegression()); // performs medium
            
            classifiers.add(new LeastMedSq());  // slow; performs badly
            
            
            classifiers.add(new LogisticBase()); // logistic regression
            classifiers.add(new LMT());
            //classifiers.add(new SimpleLogistic());
            //classifiers.add(new LWR()); // locally weighted regression       
            
            
            
            classifiers.add(new MultilayerPerceptron());        // slow; not awful on MC, but too much delay
            String[] optsMP = {"-L 0.3", "-M 0.2", "-N 50", "-H a"};
            (classifiers.get(classifiers.size() - 1)).setOptions(optsMP);

            
            classifiers.add(new NaiveBayesMultinomial());
            classifiers.add(new NaiveBayesSimple());
            classifiers.add(new AODE());
            classifiers.add(new NaiveBayes());
            classifiers.add(new NaiveBayes()); **/
            /*String[] opts = {"-K"};
            (classifiers.get(classifiers.size() - 1)).setOptions(opts);
            classifiers.add(new NaiveBayes());
            opts[0] = "-K";
            (classifiers.get(classifiers.size() - 1)).setOptions(opts);*/ 
        }
        catch (Exception e){
            System.out.println("Exception while initializing classifiers: " + e);
        }
        classifier = classifiers.get(0);
        lowImportancePrint("Regressor in use: " + classifier);
	}



	
	
    public void addInstance(Sample sample){
    	//System.out.println("Class " + this + " adding instance.");
    	double weight = sample.weight;
    	double[] attributes = sample.getAttributes();
    	if (this.numAttributes != attributes.length) {
    		System.err.println("The number of attributes used to instantiate the model doesn't match the number in the sample to be added.");
    		System.err.println("Number from instantiation: " + this.numAttributes + ". Number in sample: "  + attributes.length);
    		System.exit(1);
    	}
        data.add(new Instance(weight, attributes));
        //System.out.println("weight: " + weight + ", unique: " + sample.unique + ", label: " + sample.label);
        //String classUnique = this.toString().split("\\.")[this.toString().split("\\.").length - 1];
        //System.out.println("Sample. Feats: " + Arrays.toString(sample.feats) + ". Label: " + sample.label + ". Unique: " + sample.unique
        // 			+ ". Samples collected: " + data.numInstances());
        uniques.add(new Double(sample.unique));
        /** if (data.numInstances() == 1000 || data.numInstances() == 5000 || data.numInstances() == 10000) {
			testAllClassifiers();
			} **/
    }
    public void addInstanceWReplacement(Sample sample) {
		if (sample.weight != 0) {
			int instanceI = uniques.indexOf(new Double(sample.unique)); // checks whether this instance already exists
			if (instanceI != -1 && sample.unique != -1) { // if there was an existing instance with a matching unique identifier
				uniques.remove(instanceI);
				data.delete(instanceI);
			}
			addInstance(sample);
		}
	}




    public void buildModel(){
        try {
        	super.buildModel();
            if (classifier.getClass().getName().equals("weka.classifiers.lazy.IBk")){
                int neighbors = Math.max(1, (int)Math.sqrt(data.numInstances())); //Math.max(1, (int)(data.numInstances() / 2.0));
                //(int)Math.cbrt(data.numInstances());// Math.pow(data.numInstances(), 0.25);// Math.min(20, (int)Math.sqrt(data.numInstances())); //
                ((IBk)classifier).setKNN(neighbors);
				//System.out.println("number of neighbors for k-NN: " + neighbors);
            }

//            if (classifier.getClass().getName().equals("weka.classifiers.meta.AttributeSelectedClassifier")){
//            	// 10-fold cross-validation
//				Evaluation evaluation = new Evaluation(this.data);
//				evaluation.crossValidateModel(classifier, this.data, Math.min(10, this.data.numInstances()), new Random(1));
//				System.out.println(evaluation.toSummaryString());
//            }
        }
        catch (Exception e){
        	System.out.println("Exception while building classifier: " + e);
            System.out.println(".Classifier " + classifier.getClass().toString() + " will not be built.");
            System.out.println("Cause: " + e.getCause());
            System.out.println("\nStack trace: ");
            e.printStackTrace();
        }
    }
    
    public Instance makeInstance(Sample sample) {
    	double[] attributes = sample.getAttributes();
    	if (this.numAttributes != attributes.length) {
    		System.err.println("The number of attributes used to instantiate the model doesn't match the number in the sample to be added.");
    		System.err.println("Number from instantiation: " + this.numAttributes + ". Number in sample: "  + attributes.length);
    		System.exit(1);
    	}
        return new Instance(sample.weight, attributes);
    }
    
    
    
    

	public void writeExternal(ObjectOutput out) throws IOException {
		System.out.println("\n\n\n\n\n\n\n\n\nCALLED!!!!!!!!!!!!!!!!\n\n\n\n");
		
		out.writeObject(data);
	}
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		System.out.println("\n\n\n\n\n\n\n\n\nCALLED!!!!!!!!!!!!!!!!\n\n\n\n");
		// You must do this:
		data = (Instances)in.readObject(); 
	}

	

	
	
	

    public static void main(String[] args) {

        //file = FileReader("./iris.arff")
        //data = Instances(file)
        
        // let the Instances object know labels for each sample variable (aka attribute)
        FastVector attrInfo = new FastVector();
        attrInfo.addElement(new Attribute("pos"));
        attrInfo.addElement(new Attribute("vel"));
        attrInfo.addElement(new Attribute("hreinf"));
        
        Instances data = new Instances("Test", attrInfo, 0);
        
        System.out.println("The instances metainfo: " + data + "\n\n");
        
        // identify which variable is the dependent variable (aka label)
        data.setClassIndex(data.numAttributes() - 1);
        
        
        // three samples for the model to train on
        double[] feats1 = {1.0, 2.0, -4.0};
        Instance inst1 = new Instance(1.0, feats1);
        data.add(inst1);
        double[] feats2 = {1.0, 4.0, -8.0};
        System.out.println("The instance: " + inst1 + "\n\n");
        Instance inst2 = new Instance(1.0, feats2);
        data.add(inst2);
        double[] feats3 = {1.0, 8.0, -16.0};
        System.out.println("The instance: " + inst2 + "\n\n");
        Instance inst3 = new Instance(1.0, feats3);
        data.add(inst3);
        System.out.println("The instance: " + inst3 + "\n\n");
        
        
        System.out.println("Number of attributes: " + data.numAttributes());
        System.out.println("Class index: " + data.classIndex());
        
        //m5p = M5P();
        //m5p.buildClassifier(data);
        LinearRegression lReg = new LinearRegression();
        try{
            lReg.buildClassifier(data);
        }
        catch (Exception e){
            System.out.println("Exception while building classifier: " + e);
        }
        
        //System.out.println(m5p);
        System.out.println(lReg);
        
        double classification = 0;
        
        double[] testFeats1 = {1.0, 2.0, Instance.missingValue()};
        try{
            classification = lReg.classifyInstance(new Instance(1.0, testFeats1));
        }
        catch (Exception e){
            System.out.println("Exception while classifying instance: " + e);
        }

        System.out.println("\nclassification: " + classification);
        
        double[] testFeats2 = {1.0, 3.0, Instance.missingValue()};
        try{
            classification = lReg.classifyInstance(new Instance(1.0, testFeats2));
        }
        catch (Exception e){
            System.out.println("Exception while classifying instance: " + e);
        }
        System.out.println("\nclassification: " + classification);
        
        
        //Evaluation eval = Evaluation(data);
        //eval.evaluateModel(lReg, data);
        //eval.crossValidateModel(lReg, data, 3, Random());
        
        
        
        
        System.out.println("\n\n-------Testing class methods-------\n\n");
        
        
        WekaRegressor wbi = new WekaRegressor("KDTree", 2);
        
        
        
        // three samples for the model to train on
        
        wbi.addInstance(new Sample(Arrays.copyOfRange(feats1, 0, 2), feats1[2], 1.0));        
        wbi.addInstance(new Sample(Arrays.copyOfRange(feats2, 0, 2), feats2[2], 1.0));
        wbi.addInstance(new Sample(Arrays.copyOfRange(feats3, 0, 2), feats3[2], 1.0));    
        wbi.buildModel();

            
        double[] testFeats1A = {1.0, 2.0};
        classification = wbi.classifyInstance(testFeats1A);
        System.out.println("\nclassification: " + classification);
        
        double[] testFeats2A = {1.0, 3.0};
        classification = wbi.classifyInstance(testFeats2A);
        System.out.println("\nclassification: " + classification);
    
        //wbi.evaluateModel();
    }
}