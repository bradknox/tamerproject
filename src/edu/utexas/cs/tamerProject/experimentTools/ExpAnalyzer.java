package edu.utexas.cs.tamerProject.experimentTools;

/*
 * Guide to use:
 * 
 * 1) numEpisodes=n means that the first n episodes are analyzed.
 * 2) This accepts one command-line argument, which is the subdirectory that the .rew or .log files are found.
 * 3) conditionParamISets indicate which parameters, found in the filename of the .rew or .log files,
 *    are used as independent variables in analysis.
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

//import org.rlcommunity.rlglue.codec.types.Action;
//import org.rlcommunity.rlglue.codec.types.Observation;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.experimentTools.RecordHandler;

/**
 * ExpAnalyzer analyzes experiments. More precisely, it analyzes batches of .log and .rew files.
 * From these files, it produces statistics in CSV format.
 * <p>
 * The names for log files contain experimental parameters, such as a TAMER+RL combination method
 * or a trainer's unique identifier, separated by '%'s or 'z's. See descriptions of class variables for
 * more information on using this class.
 * <p> 
 * An example call of the main method of this class is 
 * <p>
 * java -Xmx512m -cp TamerProject.jar edu.utexas.cs.tamerProject.experiment.ExpAnalyzer cartpole_humanOnly/pEval/fullRun
 * <p>
 * In variable names and comments, "stat" or "statistics" usually refers to measurements of the
 * dependent variable, not the means or standard errors being calculated.
 * <p>
 * Though the release of this code does not include log files, I leave some references to their
 * directories below (near the top of analyze()) for anyone as an example of how to use this class.
 * @author bradknox
 *
 */

public class ExpAnalyzer{
 
	/** Whether the log file has a .log or .rew extension */
	public enum RecordType {REW, LOG}
	
	/**
	 * What to pull from the log files to analyze and write to a CSV file. This class was first
	 * written for the MDP_REW case, so its variable names might seem specific to that case,
	 * but any of these dependent variables can be used instead. 
	 * 
	 * [Imprecise description, since it's been awhile since I used it:]
	 * This class will find the total/mean amount of the specified dependent variable over
	 * the given interval durations (in steps or episodes).
	 * 
	 * MDP_REW - The reward coming from the environment.
	 * HREW - The reward coming from the human.
	 * POS_HREW - The positively valued reward from the human.
	 * NEG_HREW - The negatively valued reward from the human.
	 * HREW_INSTS - The number of instances of human reward. 
	 * HREW_FREQ - The number of instances of human reward per time step.
	 * EP_DUR - The number of time steps per episode.
	 *
	 */
	public enum DependentVar {MDP_REW, HREW, POS_HREW, NEG_HREW, HREW_INSTS, HREW_FREQ, EP_DUR}
	
	/** This many episodes are skipped before analysis starts. This is useful if there is
	 * a change in the algorithm during learning, and you want to examine performance only
	 * after that point.	 */
	public int numEpsToSkip = 0;
	
	/** The number of intervals to cut runs into for analysis. Mostly to reduce
	 *  noise in performance, intervals of episodes are examined together. Thus, for 
	 *  example, if numEpisodes (defined in analyze()) is 100 and numEpIntervals is 
	 *  5, then episodes 1-20 are analyzed together to create a single interval mean, as 
	 * are 21-40, etc. If numEpIntervals does not evenly divide numEpisodes, it may cause
	 * problems.	 */
	public int numEpIntervals = 1;
	
	/** The amount of episodes to be analyzed. All log files that have less episodes
	 * are ignored, as are episodes after this limit.	 */
	public int numEpisodes = -1;
	
	/**
	 * Discard any time steps after maxTotalSteps. A value of -1 counts all steps.
	 */
	public int maxTotalSteps = -1;
	
	/** Each int[] element of the outer array is a list of indices of parameter to be
	 * considered in conjuction. To illustrate, consider a file A%B%C.rew. Further, assume
	 * that in the directory to be analyzed, A is either 0 or 1 and B is either X or Y. If 
	 * conditionParamISets = {{0}, {0,1}}, then two CSV files will be produced. The first,
	 * for {0}, will compute statistics for A=0 and A=1 groups, ignoring values of B and C.
	 * The other CSV file, for {0,1}, will compute statistics for (A,B) groups of (0,X), 
	 * (0,Y), (1,X), and (1,Y).*/ 
	public int[][] conditionParamISets = null;
	
	/**
	 * Parameters/conditions in log files will be separated by the string separator. 
	 */
	public String separator = "%";
	
	/**
	 * In the data files, sometimes one or more lines contain metadata and should be skipped. 
	 */
	public int firstDataLine = 1;
	
	/**
	 * Instead of throwing out logs that don't have the designated number of episodes, add
	 * a constant as the value of dependent variables for which there is no data. 
	 */
	public boolean fillInMissingEps = false;
	
	/**
	 * When fillInMissingEps is true, the value of paddingVal is inserted into the missing 
	 * episodes. Default should be zero. 
	 */
	public double paddingVal = 0;

	/**
	 * Determines whether unfinished episodes are counted in statistics. This should be set to 
	 * true if fillInMissingEps is true.
	 */
	public boolean inclIncomEps = false;

	/**
	 * If setManualEpEnd is true, then RecordHandler will consider any transition that matches
	 * manualEpEndObs and manualEpEndAct (a null value always matches) to be an end of episode.
	 * When setManualEpEnd is ture, normal episode endings (as saved in the log) are not counted.
	 */
	private boolean setManualEpEnd = false;
	private Observation manualEpEndObs = null;
	private Action manualEpEndAct = null;



	
	
	public void analyze(String expName){
		
		//// This path assumes your data is at $RLLIBRARY/data/{name of experiment}/
		//String logDir = System.getenv("RLLIBRARY") + "/data/" + expName;
		String logDir = RecordHandler.getPresentWorkingDir().replace("/bin", "") + "/data/" + expName;
		RecordType recordType = RecordType.REW;
		DependentVar depVar = DependentVar.MDP_REW;

		/*
		 * Experiment-specific code goes immediately below. Two examples are given here.
		 */
		if (expName.equals("discountingOnline/loopmaze/UCTvsVI_pit")) {  
			recordType = RecordType.LOG;

			int[][] tmpParamISets = {{1, 2, 3}}; // exp name, TD update type (UCT "u" vs value iteration "d"), episodic or continuing, trainer unique
			conditionParamISets = tmpParamISets;
			
			numEpisodes = 10;
			numEpsToSkip = 0;
			numEpIntervals = 10;
			maxTotalSteps = 449;
			//firstDataLine = 0;
			depVar = DependentVar.POS_HREW;
			this.inclIncomEps = true;
			this.fillInMissingEps = true;

			this.separator = "z";
			
			
			setManualEpEnd = true;
			manualEpEndObs = new Observation();
			int[] endObs = {5,1};
			manualEpEndObs.intArray = endObs;
			manualEpEndAct = new Action();
			int[] endAct = {3};
			manualEpEndAct.intArray = endAct;
			
		}	
		else if (expName.equals("tetris")) {  
			recordType = RecordType.LOG;

			int[][] tmpParamISets = {{1}}; // exp name, TD update type (UCT "u" vs value iteration "d"), episodic or continuing, trainer unique
			conditionParamISets = tmpParamISets;
			
			numEpisodes = 120;
			numEpsToSkip = 0;
			numEpIntervals = 120;
			maxTotalSteps = 12000;
			//firstDataLine = 0;
			depVar = DependentVar.POS_HREW;
			this.inclIncomEps = true;
			this.fillInMissingEps = true;

			this.separator = "z";

			
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
				
		
		
		
		
		/* *************************************
		 * *************************************
		 * *************************************
		 * *************************************
		 * *****                          ******
		 * ***** General code starts here ******
		 * *****                          ******
		 * *************************************
		 * *************************************
		 * *************************************
		 * *************************************/
		
		if (this.fillInMissingEps && !this.inclIncomEps) {
			System.err.println("In ExpAnalyzer, fillInMissingEps is true but inclIncomEps is false. By my" +
								"imagination, this should never happen.");
			System.exit(1);
		}
		
		
		String recordFileExtension = "";
		if (recordType == RecordType.LOG)
			recordFileExtension = "log";
		else if (recordType == RecordType.REW)
			recordFileExtension = "rew";
		
		int epWriteInterval;
		if (numEpIntervals == -1)
			epWriteInterval = 1; // compute stats for each time step
		else
			epWriteInterval = numEpisodes / numEpIntervals; // must evenly divide numEpisodes
		System.out.println("Size of each interval (in episodes): " + epWriteInterval);
		
		
		
		for (int[] conditionParamIs : conditionParamISets) {
			/* For each file, output a CSV-format line with each of the 
			 * parameters as separate values, followed by each calculated
			 * statistic. 
			 */
			RecordHandler.firstDataLine = this.firstDataLine;
			RecordHandler recHandler = new RecordHandler(false);
			if (setManualEpEnd)
				recHandler.setManualEndOfEp(this.manualEpEndObs, this.manualEpEndAct);
			String statsPerEpCSV = "Results from removing first " + numEpsToSkip + " episodes and analyzing first remaining " + numEpisodes + " episodes";
			String cumStatsPerEpCSV = "Results from removing first " + numEpsToSkip + " episodes and analyzing first remaining" + numEpisodes + " episodes";
			ArrayList<double[]> expStatsPerEpMatrix = new ArrayList<double[]>();
			ArrayList<double[]> expCumStatsPerEpMatrix = new ArrayList<double[]>();
			ArrayList<String[]> parameterMatrix = new ArrayList<String[]>();
			ArrayList<String> skippedUniques = new ArrayList<String>();
			
			/*
			 * Iterate through log files.
			 */
			System.out.println("logDir: " + logDir);
			String[] fileNames = (new File(logDir)).list();
			System.out.println(Arrays.toString(fileNames));
			ArrayList<Set<String>> possParams = null;
			for (String fileName: fileNames) {
				if (!fileName.contains("." + recordFileExtension) 
								|| fileName.contains("." + recordFileExtension + "~"))
					continue;
				String filePath = logDir + "/" + fileName;
				System.out.println("\n\nProcessing log " + filePath);
				String parameterStr = fileName.replace(".log", "").replace(".rew", "").replace("recTraj-", "");
				
				
				/*
				 * Get value of dependent variable (e.g., reward) per episode from log files
				 */
				double[] rawStatsList = null;
				if (recordFileExtension.equals("log")) {
					rawStatsList = ExpAnalyzer.getDepVarListFromLogFile(depVar, inclIncomEps , filePath, 
																		recHandler, maxTotalSteps);
					System.out.println("rawStatsList.lenght: " + rawStatsList.length);
//					recHandler.loadRecord(filePath);
//					rawStatsList = recHandler.getStatsPerEp();
				}
				else if (recordFileExtension.equals("rew")) {
					rawStatsList = ExpAnalyzer.getDepVarListFromRewFile(depVar, filePath, firstDataLine);
//					rawStatsList = RecordHandler.getRewPerEpFromRewFile(filePath);
				}
	

				/*
				 * Remove episodes at beginning if numEpsToSkip > 0.
				 */
				double[] statsPerEp = null;
				if (rawStatsList != null)
					statsPerEp = ExpAnalyzer.removeFirstNElements(rawStatsList, numEpsToSkip);
				

				/*
				 * By default, remove logs with insufficient episodes
				 */
				if (statsPerEp.length < numEpisodes) {
					if (!this.fillInMissingEps) {
						System.out.println("Skipping log " + fileName + " with only " + statsPerEp.length + " episodes.");
						skippedUniques.add(parameterStr);
						continue; 
					} 
					else { // Pad stats per ep with zeroes.
						System.out.println("Padding log " + fileName + " with only " + statsPerEp.length + " episodes.");
						int firstPaddedI = statsPerEp.length;
						statsPerEp = Arrays.copyOf(statsPerEp, numEpisodes);
						for (int i = firstPaddedI; i < statsPerEp.length; i++) {
							statsPerEp[i] = this.paddingVal;
						}
					}
				}
				else if (statsPerEp.length > numEpisodes) {
					System.out.print(" More episodes than required: " + statsPerEp.length);
				}
				
				
				double[] cumStatsPerEp = RecordHandler.getCumValPerEp(statsPerEp);
				expStatsPerEpMatrix.add(statsPerEp);
				expCumStatsPerEpMatrix.add(cumStatsPerEp);
	
				/*
				 * Parse parameters in file name.
				 */
				String[] parameters = parameterStr.split(this.separator);
				if (possParams == null) {
					possParams = new ArrayList<Set<String>>();
					for (int i = 0; i < parameters.length; i++) {
						possParams.add(new HashSet<String>());
					}
				}
				for (int i = 0; i < parameters.length; i++) {
					possParams.get(i).add(parameters[i]);
				}
				parameterMatrix.add(parameters);
			}
			
			
			System.out.print("All skipped uniques: \n[");
			for (String unique: skippedUniques) {
				System.out.print("\"" + unique + "\", ");
			}
			System.out.println("]");
			
			
			/*
			 *  Calculate statistics over intervals.
			 */
			ArrayList<double[]> expIntrvlStatMatrix = new ArrayList<double[]>();
			for (int i = 0; i < parameterMatrix.size(); i++) {
				double[] intrvlStat = new double[numEpisodes / epWriteInterval];
				double[] statsPerEp = expStatsPerEpMatrix.get(i);
				for (int epNum = 0; epNum < numEpisodes; epNum++) {
					intrvlStat[epNum / epWriteInterval] += statsPerEp[epNum];
				}
				expIntrvlStatMatrix.add(intrvlStat);
			}
			
			
			
			
			
			/*
			 * 
			 * ****** For each possible param combinations in slots 0 or 1, calculate 
			 * mean and standard error. ******
			 * 
			 */
			
			ArrayList<String[]> paramSets = new ArrayList<String[]>();
			/*
			 * The mean statistics by episode across all runs with the same param set. 
			 * statMeans.get(i) gets statMeans for the ith param set. statMeans.get(i)[4]
			 * is the mean for the 5th episode for param set i. In the CSV file, only 
			 * every k values get written, where k=epWriteInterval. 
			 */
			ArrayList<double[]> statMeans = new ArrayList<double[]>(); 
			/*
			 * The mean cumulative statistics by episode across all runs with the same 
			 * param set.
			 */
			ArrayList<double[]> cumStatMeans = new ArrayList<double[]>(); 
			/*
			 * The mean statistics over intervals of length epWriteInterval.
			 */
			ArrayList<double[]> intrvlMeanStats = new ArrayList<double[]>();
			/*
			 * The mean statistics per episode across all runs with the same param set 
			 * (only one value per param set).
			 */
			ArrayList<double[]> meansOfRunMeans = new ArrayList<double[]>();
			ArrayList<double[]> statsStErrs = new ArrayList<double[]>();
			ArrayList<double[]> cumStatsStErrs = new ArrayList<double[]>();
			ArrayList<double[]> intrvlStatStErrs = new ArrayList<double[]>();
			ArrayList<double[]> stErrsOfRunMeans = new ArrayList<double[]>();
			
			
			int paramSetI = 0;
			ArrayList<String[]> possParamSets = getPossConditionParamSets(possParams, conditionParamIs);			
			System.out.print(possParamSets.size() + " possParamSets: ");
			for (String[] paramSet: possParamSets)
				System.out.print("  " + Arrays.toString(paramSet));
			int[] countsByParamSet = new int[possParamSets.size()]; // counts number of log files that use each specific set of parameters; parameter sets with zero logs are not written to file
			System.out.println("\ncountsByParamSet length: " + countsByParamSet.length);
			
			for (String[] paramSet: possParamSets) {
	//		for (String param0: possParams.get(0)) {
	//			for (String param1: possParams.get(1)) {
	//				for (String param3: possParams.get(3)) {
						//// save param set
	//					String[] paramSet = {param0,param1,param3};
						
						System.out.println("calc mean and st Err for params: " + Arrays.toString(paramSet));
						// calc sum of statistics per ep and cumulative statistics
						double[] statSumByEp = new double[numEpisodes];
						double[] cumStatSumByEp = new double[numEpisodes];
						double[] intrvlStatSum = new double[numEpisodes / epWriteInterval];
						double runMeanSum = 0.0;
						int count = 0;
						for (int i = 0; i < parameterMatrix.size(); i++) {
							String[] parameters = parameterMatrix.get(i);
							double[] statsPerEp = expStatsPerEpMatrix.get(i);
							double[] cumStatsPerEp = expCumStatsPerEpMatrix.get(i);
							if (ExpAnalyzer.instParamsMatchGroupParams(paramSet, parameters, conditionParamIs)) {
	//						if (parameters[0].equals(param0) && parameters[1].equals(param1) && parameters[3].equals(param3)) {
								System.out.print(cumStatsPerEp[cumStatsPerEp.length - 1] + ",   ");
								count++;
								for (int epNum = 0; epNum < numEpisodes; epNum++) {
									statSumByEp[epNum] += statsPerEp[epNum];
									cumStatSumByEp[epNum] += cumStatsPerEp[epNum];
									intrvlStatSum[epNum / epWriteInterval] += statsPerEp[epNum];
								}
								runMeanSum += cumStatsPerEp[numEpisodes - 1] / numEpisodes;
							}
						}
						if (count == 0) {
	//						paramSets.remove(paramSets.size() - 1);
							continue;
						}
						else
							paramSets.add(paramSet);
						System.out.println("count: " + count);
						countsByParamSet[paramSetI] = count;
						
						// calc means
						double[] statMeanByEp = new double[numEpisodes];
						double[] cumStatMeanByEp = new double[numEpisodes];
						double[] intrvlMeanStat = new double[numEpisodes / epWriteInterval];
						for (int epNum = 0; epNum < numEpisodes; epNum++) {
							statMeanByEp[epNum] = statSumByEp[epNum] / count;
							cumStatMeanByEp[epNum] = cumStatSumByEp[epNum] / count;
							if (epNum % epWriteInterval == 0)
								intrvlMeanStat[epNum / epWriteInterval] = intrvlStatSum[epNum / epWriteInterval] / (count * epWriteInterval);
						}
						double[] runMean = {runMeanSum / count};
						System.out.println("runMean: " + runMean[0]);
						meansOfRunMeans.add(runMean); 
						statMeans.add(statMeanByEp);
						cumStatMeans.add(cumStatMeanByEp);
						intrvlMeanStats.add(intrvlMeanStat);
						
						// calc sum of the squared deviation
						double[] statSumSqDevByEp = new double[numEpisodes];
						double[] cumStatSumSqDevByEp = new double[numEpisodes];
						double[] intrvlSumSqDev = new double[numEpisodes / epWriteInterval];
						double[] sumSqDevOfRunMeans = new double[1];
						for (int i = 0; i < parameterMatrix.size(); i++) {
							String[] parameters = parameterMatrix.get(i);
							double[] statsPerEp = expStatsPerEpMatrix.get(i);
							double[] cumStatsPerEp = expCumStatsPerEpMatrix.get(i);
							double[] intrvlStat = expIntrvlStatMatrix.get(i);
							if (ExpAnalyzer.instParamsMatchGroupParams(paramSet, parameters, conditionParamIs)) {
	//						if (parameters[0].equals(param0) && parameters[1].equals(param1) && parameters[3].equals(param3)) {
								
								for (int epNum = 0; epNum < numEpisodes; epNum++) {
									double statDeviation = statsPerEp[epNum] - statMeanByEp[epNum];
									statSumSqDevByEp[epNum] += Math.pow(statDeviation, 2);
									
									double cumStatDeviation = cumStatsPerEp[epNum] - cumStatMeanByEp[epNum];
									cumStatSumSqDevByEp[epNum] += Math.pow(cumStatDeviation, 2);
									
									if (epNum % epWriteInterval == 0) {
										double intrvlStatDeviation = (intrvlStat[epNum / epWriteInterval] / epWriteInterval)
													- intrvlMeanStat[epNum / epWriteInterval];
										intrvlSumSqDev[epNum / epWriteInterval] += Math.pow(intrvlStatDeviation, 2);
									}
										
	//								if (epNum == numEpisodes - 1) {
	//									System.out.println("statDeviation: " + statDeviation);
	//									System.out.println("cumStatDeviation: " + cumStatDeviation);
	//								}
								}
								System.out.println("Mean over this run: " + (cumStatsPerEp[numEpisodes - 1] / numEpisodes));
								double runMeanDeviation = (cumStatsPerEp[numEpisodes - 1] / numEpisodes) - runMean[0];
	//							System.out.println("runMeanDeviation: " + runMeanDeviation);
								sumSqDevOfRunMeans[0] += Math.pow(runMeanDeviation, 2);
							} 	
						}
						System.out.println("statSumSqDevByEp at end: " + statSumSqDevByEp[numEpisodes - 1]);
						System.out.println("cumStatSumSqDevByEp at end: " + cumStatSumSqDevByEp[numEpisodes - 1]);
						System.out.println("intrvlSumSqDev at end: " + intrvlSumSqDev[(numEpisodes - 1) / epWriteInterval]);
						System.out.println("sumSqDevOfRunMeans at end: " + sumSqDevOfRunMeans[0]);
						
						// calc variance, standard deviation, and standard error
						double[] statStErr = new double[numEpisodes];
						double[] cumStatStErr = new double[numEpisodes];
						double[] intrvlStatStErr = new double[numEpisodes / epWriteInterval];
						for (int epNum = 0; epNum < numEpisodes; epNum++) {
							double statVar = statSumSqDevByEp[epNum] / (count - 1);
							double statStDev = Math.sqrt(statVar);
							statStErr[epNum] = statStDev / Math.sqrt(count); 
							double cumStatVar = cumStatSumSqDevByEp[epNum] / (count - 1);
							double cumStatStDev = Math.sqrt(cumStatVar);
							cumStatStErr[epNum] = cumStatStDev / Math.sqrt(count);
							if (epNum % epWriteInterval == 0) {
								double intrvlStatVar = intrvlSumSqDev[epNum / epWriteInterval] / (count - 1);
								double intrvlStatStDev = Math.sqrt(intrvlStatVar);
								intrvlStatStErr[epNum / epWriteInterval] = intrvlStatStDev / Math.sqrt(count);
							}
	//						if (epNum == numEpisodes - 1) {
	//							System.out.println("statVar: " + statVar);
	//							System.out.println("cumStatVar: " + cumStatVar);
	//						}
						}
						double runMeansVar = sumSqDevOfRunMeans[0] / (count - 1);
						double runMeansStDev = Math.sqrt(runMeansVar);
						double[] runMeansStErr = {runMeansStDev / Math.sqrt(count)};
	//					System.out.println("runMeansVar: " + runMeansVar);
						
						statsStErrs.add(statStErr);
						cumStatsStErrs.add(cumStatStErr);
						intrvlStatStErrs.add(intrvlStatStErr);
						stErrsOfRunMeans.add(runMeansStErr);
						paramSetI++;
	//				}
	//			}
			}
			countsByParamSet = Arrays.copyOf(countsByParamSet, paramSets.size());
			System.out.println("countsByParamSet length: " + countsByParamSet.length);
		
			
		
			/*
			 *  WRITE TO FILE
			 */
		
			
			boolean transpose = false;
			
			int numParams = paramSets.get(0).length;
			if (transpose) {
				//// transpose the stats so that there are more rows than columns (typically)
				for (int row = 0; row < numParams; row++) {
					statsPerEpCSV += ",";
					cumStatsPerEpCSV += ",";					
					for (int col = 0; col < paramSets.size(); col++) {
						if (row < paramSets.get(col).length) {
							statsPerEpCSV += paramSets.get(col)[row];
							cumStatsPerEpCSV += paramSets.get(col)[row];
						}
						statsPerEpCSV += ",";
						cumStatsPerEpCSV += ",";					
					}
					statsPerEpCSV += "\n";
					cumStatsPerEpCSV += "\n";
				}
				statsPerEpCSV += ",";
				cumStatsPerEpCSV += ",";
				for (int col = 0; col < paramSets.size(); col++) {
					statsPerEpCSV += countsByParamSet[col];
					cumStatsPerEpCSV += countsByParamSet[col];
					statsPerEpCSV += ",";
					cumStatsPerEpCSV += ",";
				}
				statsPerEpCSV += "\n\n";
				cumStatsPerEpCSV += "\n\n";
				
			
				
				// write means
				for (int row = 0; row < numEpisodes; row++) {
					if ((row+1) % epWriteInterval == 0) {
						System.out.println("Writing row " + row);
						statsPerEpCSV += (row+1) + ",";
						cumStatsPerEpCSV += (row+1) + ",";
						for (int col = 0; col < paramSets.size(); col++) {
							statsPerEpCSV += statMeans.get(col)[row];
							cumStatsPerEpCSV += cumStatMeans.get(col)[row];
							statsPerEpCSV += ",";
							cumStatsPerEpCSV += ",";
						}
						statsPerEpCSV += "\n";
						cumStatsPerEpCSV += "\n";
					}
				}
				
		
		
				statsPerEpCSV += "\n";
				cumStatsPerEpCSV += "\n";
				
		
				// write standard errors
				for (int row = 0; row < numEpisodes; row++) {
					if ((row+1) % epWriteInterval == 0) {
					statsPerEpCSV += (row+1) + ",";
					cumStatsPerEpCSV += (row+1) + ",";
					for (int col = 0; col < paramSets.size(); col++) {
		//				if (row < expStatsPerEpMatrix.get(col).length) {
							statsPerEpCSV += statsStErrs.get(col)[row];
							cumStatsPerEpCSV += cumStatsStErrs.get(col)[row];
		//				}
						statsPerEpCSV += ",";
						cumStatsPerEpCSV += ",";
					}
					statsPerEpCSV += "\n";
					cumStatsPerEpCSV += "\n";
					}
				}
			}

			else {			// NOT TRANSPOSED
				statsPerEpCSV += ", ,"; 
				cumStatsPerEpCSV += ", ,";
				for (int i = 0; i < numParams; i++) {
					statsPerEpCSV += ", ";
					cumStatsPerEpCSV += ", ";
				}
			
				statsPerEpCSV += "meanOfRunMeans,stErrOfRunMeans,,";
				cumStatsPerEpCSV += "meanOfRunMeans,stErrOfRunMeans,,";
				for (int i = 0; i < 2; i++) {	
					for (int col = 0; col < numEpisodes; col++) {
						String[] statHeader = {"meanStatAtEp", "statStErrAtEp"};
						String[] cumStatHeader = {"meanCumStatAtEp", "cumStatStErrAtEp"};
						if ((col+1) % epWriteInterval == 0) {					
							statsPerEpCSV += statHeader[i] + ",";
							cumStatsPerEpCSV +=  cumStatHeader[i] + ",";
						}
					}
					statsPerEpCSV += ",";
					cumStatsPerEpCSV += ",";	
				}
				statsPerEpCSV += ",";
				for (int i = 0; i < 2; i++) {	
					for (int col = 0; col < numEpisodes; col++) {
						String[] statHeader = {"intrvlMean", "intrvlStErr"};
						if ((col+1) % epWriteInterval == 0) {					
							statsPerEpCSV += statHeader[i] + ",";
						}
					}
					statsPerEpCSV += ",";	
				}
			
	
				
				statsPerEpCSV += "\nEp Num ->, ,"; 
				cumStatsPerEpCSV += "\nEp Num ->, ,";
				for (int i = 0; i < numParams; i++) {
					statsPerEpCSV += ", ";
					cumStatsPerEpCSV += ", ";
				}
				
				// write headers for epNums of mean stat per ep and its std err
				statsPerEpCSV += ",,,";
				cumStatsPerEpCSV += ",,,";
				for (int i = 0; i < 2; i++) {
					for (int col = 0; col < numEpisodes; col++) { 
						if ((col+1) % epWriteInterval == 0) {					
							statsPerEpCSV += (col+1) + ",";
							cumStatsPerEpCSV += (col+1) + ",";
						}
					}
					statsPerEpCSV += ",";
					cumStatsPerEpCSV += ",";	
				}
			
				statsPerEpCSV += ",";
				cumStatsPerEpCSV += ",";	
			
				// write headers for mean statistics over intervals and their std err
				for (int i = 0; i < 2; i++) {
					for (int col = 0; col < numEpisodes; col++) {
						if ((col+1) % epWriteInterval == 0) {					
							statsPerEpCSV += (col-(epWriteInterval - 2)) + ",";
							cumStatsPerEpCSV += (col-(epWriteInterval - 2)) + ",";
						}
					}
					statsPerEpCSV += ",";
					cumStatsPerEpCSV += ",";	
				}
				
				statsPerEpCSV += ",";
				cumStatsPerEpCSV += ",";	
				
				statsPerEpCSV += "\n";
				cumStatsPerEpCSV += "\n";
				// each row is a different parameter set
				for (int row = 0; row < paramSets.size(); row++) {
				
					// write params
					for (int col = 0; col < numParams; col++) {					
						if (col < paramSets.get(row).length) {
							statsPerEpCSV += paramSets.get(row)[col];
							cumStatsPerEpCSV += paramSets.get(row)[col];
						}
						statsPerEpCSV += ",";
						cumStatsPerEpCSV += ",";					
					}
				
					// write run counts
					statsPerEpCSV += countsByParamSet[row];
					cumStatsPerEpCSV += countsByParamSet[row];
					statsPerEpCSV += ", ,";
					cumStatsPerEpCSV += ", ,";
					
					// write mean of run means and st dev of run means
					statsPerEpCSV += meansOfRunMeans.get(row)[0] + ",";
					cumStatsPerEpCSV += meansOfRunMeans.get(row)[0] + ",";
					statsPerEpCSV += stErrsOfRunMeans.get(row)[0] + ",";
					cumStatsPerEpCSV += stErrsOfRunMeans.get(row)[0] + ",";
					
					// write statMeans
					statsPerEpCSV += "," + ExpAnalyzer.writeArrayToCSVRow(statMeans.get(row), epWriteInterval); // write mean statistics for kth, 2kth, 3kth, ... episodes, where k=epWriteInterval
					cumStatsPerEpCSV += "," + ExpAnalyzer.writeArrayToCSVRow(cumStatMeans.get(row), epWriteInterval);
					statsPerEpCSV += "," + ExpAnalyzer.writeArrayToCSVRow(statsStErrs.get(row), epWriteInterval);
					cumStatsPerEpCSV += "," + ExpAnalyzer.writeArrayToCSVRow(cumStatsStErrs.get(row), epWriteInterval);
					statsPerEpCSV += ",," + ExpAnalyzer.writeArrayToCSVRow(intrvlMeanStats.get(row), 1);
					statsPerEpCSV += "," + ExpAnalyzer.writeArrayToCSVRow(intrvlStatStErrs.get(row), 1);
					
					
	//				for (int col = 0; col < numEpisodes; col++) {
	//					if ((col+1) % epWriteInterval == 0) {
	//					statsPerEpCSV += statMeans.get(row)[col];
	//					cumStatsPerEpCSV += cumStatMeans.get(row)[col];
	//					statsPerEpCSV += ",";
	//					cumStatsPerEpCSV += ",";
	//					}
	//				}						
	//				for (int col = 0; col < numEpisodes; col++) {
	//					if ((col+1) % epWriteInterval == 0) {
	//					statsPerEpCSV += statsStErrs.get(row)[col];
	//					cumStatsPerEpCSV += cumStatsStErrs.get(row)[col];
	//					statsPerEpCSV += ",";
	//					cumStatsPerEpCSV += ",";
	//					}
	//				}		
					statsPerEpCSV += "\n";
					cumStatsPerEpCSV += "\n";
				}
		
				
				
			}
			
	
			String resultsUnique = expName.replace("/", "-");
			resultsUnique += "-" + depVar.toString();
			for (int paramI : conditionParamIs)
				resultsUnique += "-" + paramI;
			
			ExpAnalyzer.writeStrToFile(logDir + "/expResults-statsMeanStErr-" + resultsUnique + ".csv", statsPerEpCSV);
			ExpAnalyzer.writeStrToFile(logDir + "/expResults-cumStatsMeanStErr-" + resultsUnique + ".csv", cumStatsPerEpCSV);
					
			if (numEpisodes % epWriteInterval != 0)
				System.out.println("Last interval has less episodes than the other intervals b/c epWriteInterval does not evenly divide numEpisodes.");
		}
	}
	
	
	private static ArrayList<String[]> getPossConditionParamSets(ArrayList<Set<String>> possParams, 
												int[] paramIs) {
		return ExpAnalyzer.recurseForPossConditionParamSets(possParams, paramIs, new String[0]);
	}
	
	private static ArrayList<String[]> recurseForPossConditionParamSets(ArrayList<Set<String>> possParams, 
												int[] paramIs, String[] paramSetSoFar){
		ArrayList<String[]> paramList = new ArrayList<String[]>();
		// base case
		if (paramSetSoFar.length == paramIs.length) {
			paramList.add(paramSetSoFar);
			return paramList;
		}
		int paramI = paramIs[paramSetSoFar.length];
		for (String param: possParams.get(paramI)) {
			String[] newParamSetSoFar = new String[paramSetSoFar.length + 1];
			for (int i = 0; i < paramSetSoFar.length; i++){
				newParamSetSoFar[i] = paramSetSoFar[i];
			}
			newParamSetSoFar[newParamSetSoFar.length - 1] = param;
			paramList.addAll(recurseForPossConditionParamSets(possParams, paramIs, newParamSetSoFar));
		}
		return paramList;
	}
											
	private static boolean instParamsMatchGroupParams(String[] groupParams, String[] instParams, int[] paramIs) {
		boolean match = true;
		for (int i = 0; i < paramIs.length; i++) {
			int paramI = paramIs[i];
//			System.out.println(Arrays.toString(groupParams));
//			System.out.println(Arrays.toString(instParams));
			if (!groupParams[i].equals(instParams[paramI]))
				match = false;
		}
		return match;
	}
	
	/**
	 * This method returns a list of the sum of a specific dependent variable per episode
	 * from a .log file. Incomplete episodes are included only for some of the dependent
	 * variables.
	 * 
	 * @param depVar
	 * @param filePath
	 * @param recHandler
	 * @return
	 */
	private static double[] getDepVarListFromLogFile(DependentVar depVar, boolean includeIncompEps,
									String filePath, RecordHandler recHandler, int maxTotalSteps) {
		recHandler.loadRecord(filePath);
		return recHandler.getValPerEp(depVar, includeIncompEps, maxTotalSteps);
	}
	

	/**
	 * This method returns a list of the sum of a MDP reward per episode
	 * from a .rew file. 
	 * @param depVar
	 * @param filePath
	 * @return
	 */
	private static double[] getDepVarListFromRewFile(DependentVar depVar, 
									String filePath, int firstDataLine) {
		double[] depVarList = null;
		if (depVar == DependentVar.MDP_REW){
			depVarList = RecordHandler.getMDPRewPerEpFromRewFile(filePath, firstDataLine);
		}
		else {
			System.err.println("Cannot run ExpAnalyzer on dependent variable " 
					+ depVar + ". Rew files only contain MDP reward information.");
		}
			
		return depVarList;
	}
	
	
	private static double[] removeFirstNElements(double[] startArray, int n) {
		//System.out.println("startArray: " + Arrays.toString(startArray));
		if (startArray.length - n < 0) {
			System.err.println("Attempting to remove more elements than are in array. " +
					"startArray of length " + startArray.length + ". " +
					"removing: " + n + ".");
		}
		double[] resultArray = new double[startArray.length - n];
		for (int i = n; i < startArray.length; i++)
			resultArray[i - n] = startArray[i];
//		System.out.println("resultArray: " + Arrays.toString(resultArray));
		return resultArray;
	}
	
												
	private static void writeStrToFile(String path, String str) {
		try {
			// Create file 
		    FileWriter fstream = new FileWriter(path, false);
		    BufferedWriter out = new BufferedWriter(fstream);
		    out.write(str + "\n");
		    //Close the output stream
		    out.close();
		}
		catch (Exception e) {
			System.err.println("Error: " + e.getMessage() + "\nExiting.");
			e.printStackTrace();
			System.exit(0);
		}	   
	}
	
	/**
	 * Write value of kth, 2kth, 3kth, ... array elements, where k=writeInterval.
	 * For example, if writeInterval=5, writes indices 4, 9, 14, ....
	 * 
	 * @param array
	 * @param writeInterval
	 * @return
	 */
	// 
	private static String writeArrayToCSVRow(double[] array, int writeInterval) {
		String result = "";
		for (int col = 0; col < array.length; col++) {
			if ((col+1) % writeInterval == 0) {
				result += array[col];
				result += ",";
			}
		}	
//		System.out.println("adding: " + result);
		return result;
	}
	
	public static void main(String[] args){
		(new ExpAnalyzer()).analyze(args[0]);
	}
	
}