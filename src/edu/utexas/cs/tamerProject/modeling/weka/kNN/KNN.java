package edu.utexas.cs.tamerProject.modeling.weka.kNN;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.NormalizableDistance;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.core.Utils;
import edu.utexas.cs.tamerProject.modeling.weka.kNN.BallTree;
import edu.utexas.cs.tamerProject.modeling.weka.kNN.CoverTree;
import edu.utexas.cs.tamerProject.modeling.weka.kNN.KDTree;
import edu.utexas.cs.tamerProject.modeling.weka.kNN.LinearNNSearch;
import edu.utexas.cs.tamerProject.modeling.weka.kNN.NearestNeighbourSearch;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Random;

public class KNN extends Classifier {

	  private static final long serialVersionUID = 6667L;

	  NearestNeighbourSearch nNSearch;
	  int k;
	  public static double biasStrength = 0.05; // 0.5 for Nexi // 0.1 for first 6x6 loopmaze batch (later used linear model for loopmaze)
	  public static double baselineBias = 0.0;
	  public static boolean NORMALIZE = true; // makes distances independent of data gathered; only desirable sometimes
	  
	  double neighborSum;
	  
	  public KNN(){
		  this.nNSearch = new KDTree();
	  }
	  
	  public KNN(String searchType){
		  if (searchType.equals("KDTree"))
			  this.nNSearch = new KDTree();
		  else if (searchType.equals("BallTree"))
			  this.nNSearch = new BallTree();
		  else if (searchType.equals("CoverTree"))
			  this.nNSearch = new CoverTree();
		  else if (searchType.equals("LinearNNSearch"))
			  this.nNSearch = new LinearNNSearch();  

		  this.nNSearch.setMeasurePerformance(true);
	  }
	  
	  
	  // calling update() for each new instance might be faster
	  public void buildClassifier(Instances data) throws Exception{
		  // System.out.println("Building kNN with " + data.numInstances() + " instances.");
		  
		  if (!NORMALIZE) {
			  NormalizableDistance distFcn = new EuclideanDistance(data);
			  distFcn.setDontNormalize(true); 
			  // String[] distFcnOptions = {"-D"};
			  // distFcn.setOptions(distFcnOptions);
			  this.nNSearch.setDistanceFunction(distFcn);
		  }
		  
		  this.nNSearch.setInstances(data);
		  
		  //k = Math.max(1, (int)Math.sqrt(data.numInstances()));
		  //k = Math.max(1, (int)Math.cbrt(data.numInstances()));
		  k = Math.max(1, (int)Math.sqrt(Math.sqrt(data.numInstances()))); // ^(1/4)

		  //System.out.println("for neighbors k = " + k);
//		  System.out.println("Distance function options: " + Arrays.toString(this.nNSearch.getDistanceFunction().getOptions()));

		  
	  }
	  
	  
	  public double classifyInstance(Instance instance) throws Exception {
		  if (this.nNSearch.getInstances() == null || this.nNSearch.getInstances().numInstances() == 0)
			  return baselineBias;
		  Instances kNeighbors = this.nNSearch.kNearestNeighbours(instance, this.k);
		  this.neighborSum = 0;
		 // System.out.println("Query instance, k=" + this.k + ": " + Arrays.toString(instance.toDoubleArray()));
		  
		  for (int i = 0; i < kNeighbors.numInstances(); i++){
			  Instance neighbor = kNeighbors.instance(i);
			  double distance = this.nNSearch.getDistanceFunction().distance(instance, neighbor);
			 // System.out.println("Distance from instance " + Arrays.toString(neighbor.toDoubleArray()) + ": " + distance + ".");
			  double distanceBasedBias = Math.max((1 - (distance*biasStrength)), (1 / (1 + 10*biasStrength*distance))); 
			  this.neighborSum += kNeighbors.instance(i).classValue() * distanceBasedBias + ((1 - distanceBasedBias) * baselineBias);
		  }
		  
		  
		  //System.out.println("\t ---> " + (neighborSum / kNeighbors.numInstances()) + "\n\n\n");
//		  System.out.println("For k=" + this.k + ", number instances looked at: " + this.nNSearch.getPerformanceStats().m_PointCount +
//				  " of " + this.nNSearch.m_Instances.numInstances());
//		  System.out.println("Where neighbors were found in search order: " 
//				  					+ Arrays.toString(this.nNSearch.getPerformanceStats().lastSearchOrderOfKNeighbors));
//		  int[] searchOrderCopy = Arrays.copyOf(this.nNSearch.getPerformanceStats().lastSearchOrderOfKNeighbors, this.nNSearch.getPerformanceStats().lastSearchOrderOfKNeighbors.length);
//		  Arrays.sort(searchOrderCopy);
//		  System.out.println("Sorted: " 
//					+ Arrays.toString(searchOrderCopy));
		  //System.out.flush();
		  return this.neighborSum / kNeighbors.numInstances();
//		  return this.nNSearch.nearestNeighbour(instance).classValue();
	  }


}
