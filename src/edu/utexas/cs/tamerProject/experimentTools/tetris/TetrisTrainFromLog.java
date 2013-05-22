package edu.utexas.cs.tamerProject.experimentTools.tetris;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


import org.rlcommunity.environments.tetris.Tetris;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.specialty.ExtActionAgentWrap;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.demos.tetris.TetrisTamerExpHelper;
import edu.utexas.cs.tamerProject.experiments.GeneralExperiment;
import edu.utexas.cs.tamerProject.modeling.IncGDLinearModel;


/**
 * 
 * @author bradknox
 *
 */
public class TetrisTrainFromLog  {

	public static int NUM_EPS_PER_TEST = 30;
	public static int DEFAULT_INTERVAL_SIZE = -1;
	public static int DEFAULT_NUM_INTERVALS = 1;
	public static int NUM_EPOCHS = 1;
	
	private static void testEachLog() {

		String logDir = "/Users/bradknox/Dropbox/projects/rl-library-data/tetris/2013-03-MLJSubm"; //RecordHandler.getPresentWorkingDir().replace("/bin", "") + "/logsForDemoPrep";//+ "/data/" + expName;
		System.out.println("logDir: " + logDir);
		
		/*
		 * Get paths to logs from each experiment's control group and combine into single array.
		 */
		String[] expFileNames = (new File(logDir)).list();
		System.out.println("expFileNames: " + Arrays.toString(expFileNames));
		ArrayList<String> logFilePathsList = new ArrayList<String>();
		for (int i = 0; i < expFileNames.length; i++) {
	 		if (!expFileNames[i].contains(".log") 
						|| expFileNames[i].contains(".log~"))
	 				continue;
			logFilePathsList.add(logDir + "/" + expFileNames[i]);
		}
		String[] logFilePaths = logFilePathsList.toArray(new String[0]);
		System.out.println("logFilePaths: " + Arrays.toString(logFilePaths));
		
		
		
		HashMap<String, double[]> perfsByInterv = new HashMap<String, double[]>(); 
		for (int i = 0; i < logFilePaths.length; i++) {
 			String logFilePath = logFilePaths[i];
 			if (!logFilePath.contains(".log") 
					|| logFilePath.contains(".log~"))
 				continue;
 			
 			
			System.out.println("Training log: " + logFilePath);
			double[] meanLinesPerInterv = TetrisTrainFromLog.testLogAtIntervals(logFilePath, 
					TetrisTrainFromLog.DEFAULT_INTERVAL_SIZE,
					TetrisTrainFromLog.DEFAULT_NUM_INTERVALS);
			System.out.println(i + ", " + logFilePath + ", meanLinesPerInterv, " + Arrays.toString(meanLinesPerInterv));
			perfsByInterv.put(logFilePath, meanLinesPerInterv);
			
			System.out.println("Results so far:");
			TetrisTrainFromLog.printResults(logFilePaths, perfsByInterv, i+1);
		}		
		
		System.out.println("\n\n\n-------FINISHED ALL LOG ANALYSES. RESULTS BELOW-------\n\n\n");;
		
		TetrisTrainFromLog.printResults(logFilePaths, perfsByInterv, logFilePaths.length);

}
	
	private static void printResults(String[] logFilePaths, HashMap<String, double[]> perfsByInterv, int numResults) {
		/*
		 * Print each in a way that will be easy to convert to CSV
		 */
		for (int k = 0; k < numResults; k++) { 
			String logFilePath = logFilePaths[k];
			System.out.print(logFilePath + ", ");
			System.out.println(Arrays.toString(perfsByInterv.get(logFilePath)));
		}
	}
	
	
	
	
	
	private static double[] testLogAtIntervals(String logFilePath, int intervalSize, int numIntervals){
		GeneralAgent agent;
		EnvironmentInterface env;
		GeneralExperiment exp;
		int maxSteps;
		double[] meanLinesPerGameByInterv = new double[numIntervals];
		
		
		
		for (int i = 0; i < numIntervals; i++) {
			maxSteps = (i+1) * intervalSize;

			/*
			 * Init experiment class
			 */
			exp = new TetrisTamerExpHelper();
			
			/*
			 * Init environment
			 */
			env = exp.createEnv();
			
			/*
			 * Init agent
			 */
			String[] args = TetrisTamerExpHelper.getDebugArgsStrArray();
			agent = exp.createAgent(args, env);
	
			/*
			 * Set agent parameters
			 */
			agent.setAllowUserToggledTraining(false);
			agent.setRecordLog(false);
			agent.setRecordRew(false);
		
			/*
			 * Set experimental parameters
			 */
			RunLocalExperiment.numEpisodes = TetrisTrainFromLog.NUM_EPS_PER_TEST;
			RunLocalExperiment.maxStepsPerEpisode= 100000;
			RunLocalExperiment.stepDurInMilliSecs = 0;		
			RunLocalExperiment.PAUSE_DUR_AFTER_EP = 0;
			RunLocalExperiment runLocal = new RunLocalExperiment();
	
			
			runLocal.theAgent = agent;
			runLocal.theEnvironment = env;
			
			runLocal.init();
			runLocal.initExp(); // where agent_init() is called
			
			if (agent.getInTrainSess())
				agent.toggleInTrainSess(); // toggle ensures that member agents are also toggled off
			
			
			
			
			
			

			
			/*
			 * Learn weights from log
			 */
			String taskSpec = (Tetris.getTaskSpecPayload(Tetris.getDefaultParameters())).getTaskSpec();
			//System.out.println("\n\n\ntasks spec: " + taskSpec + "\n\n\n");
			double[] learnedWts = MakeJavaLogLearnerAgent.getHWeightsFromLog(logFilePath, 
												maxSteps, NUM_EPOCHS, taskSpec);
			
			
			
			//double[] learnedWts = {3.9300451331981394e-06, 8.0786090581041263e-06, 3.6151474330809797e-06, 4.8930208819977178e-06, 3.7982191718819994e-06, 1.0601453524522298e-05, 1.0170461741251863e-05, 3.5068082900874361e-06, -3.1285757641770864e-07, 3.2919273426427274e-06, 1.0843303042934514e-05, -2.4478709704656244e-06, -4.2510262663663102e-06, -2.3385936368617054e-06, -1.0879580106492503e-06, -2.0759045857794822e-07, -3.1917533659013483e-06, 1.3467462944191388e-06, -5.081626331068921e-06, 2.3269106122931006e-06, -2.1515411754229169e-05, 4.2902074440316938e-07, -1.4845998302961572e-06, 2.7611890145559421e-06, 5.1019458416144572e-06, 3.0827504712145703e-06, 2.3388188711065901e-06, 2.3106015035037644e-06, 9.8069997745273607e-06, 7.9962440271118629e-06, 3.4544071167129234e-06, 1.0842325185367549e-07, 3.5057794935823326e-06, 7.8671674386881343e-06, -3.0966993453061387e-05, -3.8266459380620331e-05, -4.811713219905927e-05, -1.8115818677063897e-05, -4.6670583215099104e-05, -3.6143807988894375e-05, -1.3627752830864685e-05, -5.851786744910292e-05, 1.2731038645428353e-05, -0.00057240303270004013, 2.1954333295511639e-05, -5.3109070938694076e-05, 3.3966626423641867e-05};
			System.out.println("Learned " + learnedWts.length + 
							" weights: " + Arrays.toString(learnedWts));
			double[] learnedWtsWOBias = Arrays.copyOf(learnedWts, learnedWts.length - 1);
			
			
			ExtActionAgentWrap demoAgent = (ExtActionAgentWrap)agent;
			((IncGDLinearModel)demoAgent.coreAgent.model).setWeights(learnedWtsWOBias);
			((IncGDLinearModel)demoAgent.coreAgent.model).setBiasWt(learnedWts[learnedWts.length-1]);
			
			
			
			System.out.println("About to start experiment");
			runLocal.startExp();
			while (!runLocal.expFinished) {
				GeneralAgent.sleep(500);
			}
			
			System.out.println("Experiment finished");
			
			
			meanLinesPerGameByInterv[i] = mean(runLocal.rlReturn);
			
		}
	
		
		return meanLinesPerGameByInterv;
	}
	
	
	
	
	
	
	// ultimate laziness: from http://stackoverflow.com/questions/4191687/how-to-calculate-mean-median-mode-and-range-from-a-set-of-numbers
	public static double mean(double[] m) {
	    double sum = 0;
	    for (int i = 0; i < m.length; i++) {
	        sum += m[i];
	    }
	    return sum / m.length;
	}
	
	
	public static void main(String[] args) {
		TetrisTrainFromLog.testEachLog();
	}


}
