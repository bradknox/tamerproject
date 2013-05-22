package edu.utexas.cs.tamerProject.experimentTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.agents.tamer.HRew;
import edu.utexas.cs.tamerProject.experimentTools.RecordHandler;
import edu.utexas.cs.tamerProject.experimentTools.TimeStep;

/**
 * Gathers statistics from logs per time-step interval. 
 * @author bradknox
 */
public class PerStepIntervCtr {
	
	public enum Metric {MDP_REW, HREW, POS_HREW, NEG_HREW, HREW_INSTS, HREW_FREQ, EP_DUR}
	
	public static double[] getPerIntervCt(String logFilePath, int stepsPerInterv, Metric metric, String metStr) {
		Observation stepO = new Observation();
		Action stepA = new Action();
		TimeStep step;
		int stepsCtedThisInterv = 0;
		RecordHandler recHandler = new RecordHandler(false);
		recHandler.loadRecord(logFilePath);
		
		
		
		double ctThisInterv = 0;
		ArrayList<Double> perIntervCt = new ArrayList<Double>();
		
		
		while (true) {
			step = recHandler.getNextTimeStep(stepO, stepA);
			if (step == null)
				break;
			
			
			ctThisInterv += getMetricForStep(step, metric);
				
			
			stepsCtedThisInterv++;
			
			/*
			 * If stepsPerInterv reached, save rew to rewPerIntervCt, reset rew counter,
			 * ... 
			 */
			if (stepsPerInterv == stepsCtedThisInterv) {
				perIntervCt.add(ctThisInterv);
				stepsCtedThisInterv = 0;
				ctThisInterv = 0;
			}
			
			
		}
		
		double[] perIntervCtArray = new double[perIntervCt.size()];
		for (int i = 0; i < perIntervCt.size(); i++) {
			perIntervCtArray[i] = perIntervCt.get(i);
		}
		System.out.println(metStr + " per interval: " + Arrays.toString(perIntervCtArray));
		return perIntervCtArray;
	}
	
	
	
	
	
	
	
	
	
	public static double getMetricForStep(TimeStep step, Metric metric) {
		double ctr = 0;
		if (metric.equals(Metric.HREW)) {
			ArrayList<HRew> hRewList = step.hRewList;
			for (int i = 0; i < hRewList.size(); i++) {
				ctr += Math.abs(hRewList.get(i).val);
			}
		}
		if (metric.equals(Metric.HREW_INSTS)) {
			ctr = step.hRewList.size();
		}
		if (metric.equals(Metric.MDP_REW)) {
			double envRew = step.rew;
			ctr += envRew;
		}
		if (metric.equals(Metric.POS_HREW)) {
			ArrayList<HRew> hRewList = step.hRewList;
			for (int i = 0; i < hRewList.size(); i++) {
				if (hRewList.get(i).val > 0)	
					ctr += hRewList.get(i).val;
			}
		}
		if (metric.equals(Metric.NEG_HREW)) {
			ArrayList<HRew> hRewList = step.hRewList;
			for (int i = 0; i < hRewList.size(); i++) {
				if (hRewList.get(i).val < 0)	
					ctr += hRewList.get(i).val;
			}
		}
		return ctr;
	}
	
	
	

	
	

	
	
	
	
	
	public static double[] getSumOfRewPerInterv(int numberOfIntervals, String[] logPaths, 
			int intervalLen, Metric metric, String metStr) {
		double[] sumOfRewPerInterv = new double[numberOfIntervals];
		for (int i = 0; i < logPaths.length; i++) {

			
			System.out.print("Log: " + logPaths[i] + ";   ");
			double[] rewPerIntervCt = PerStepIntervCtr.getPerIntervCt(logPaths[i], intervalLen,
					metric, metStr);
			for (int j = 0; j < numberOfIntervals; j++) {
				sumOfRewPerInterv[j] += rewPerIntervCt[j];
			}
		}
		double[] meanRewPerInterv = new double[numberOfIntervals];
		for (int i = 0; i < numberOfIntervals; i++) { 
			meanRewPerInterv[i] = sumOfRewPerInterv[i] / logPaths.length;
		}
		
		return meanRewPerInterv;
	}
	
	
	
	
	
	
	public static void main(String[] args) {
		int intervalLen = 75;
		int numberOfIntervals = 9;
		String recordFileExtension = "log";
		String expName = "tetris/2013-03-MLJSubm/";
		String logDir = RecordHandler.getPresentWorkingDir().replace("/bin", "") + "/data/" + expName;
		

		
		//		String logFile = logDir + "tetris_tamerPsy_critique/controlOnly/recAbbrevTraj-3ak_1_Training-1259867580.98.log";
		String[] fileNames = (new File(logDir)).list();
		System.out.println(Arrays.toString(fileNames));
		
		String[] logPaths = new String[fileNames.length];
		int logPathI = 0;
		for (int i = 0; i < fileNames.length; i++) {
			String logFile = fileNames[i];
			if (!logFile.contains("." + recordFileExtension) 
					|| logFile.contains("." + recordFileExtension + "~"))
				continue;
			
			logPaths[logPathI] = logDir + logFile;
			logPathI++;
		}
		
		// remove empty cells in logPaths array
		logPaths = Arrays.copyOf(logPaths, logPathI);
		int numLogs = logPaths.length;


		
		
		
		
		double[] meanRewPerInterv = PerStepIntervCtr.getSumOfRewPerInterv(numberOfIntervals, logPaths, 
																			intervalLen, Metric.HREW,
																			"Sum of abs val of human reward");
		System.out.println("Number of logs: " + numLogs);
		System.out.println("Mean sum of absolute value of human reward per interval: " + Arrays.toString(meanRewPerInterv) + "\n\n");
		

		
		double[] meanPressesPerInterv = PerStepIntervCtr.getSumOfRewPerInterv(numberOfIntervals, logPaths, 
																		intervalLen, Metric.HREW_INSTS,
																		"Reward button presses per interval");
		System.out.println("Number of logs: " + numLogs);
		System.out.println("Mean reward button presses per interval: " + Arrays.toString(meanPressesPerInterv) + "\n\n");

		
		
		double[] meanMDPRewPerInterv = PerStepIntervCtr.getSumOfRewPerInterv(numberOfIntervals, logPaths, 
																			intervalLen, Metric.MDP_REW,
																			"MDP reward per interval");
		System.out.println("Number of logs: " + numLogs);
		System.out.println("Mean MDP rew per interval: " + Arrays.toString(meanMDPRewPerInterv) + "\n\n");
		
		
		double[] meanPosHRewPerInterv = PerStepIntervCtr.getSumOfRewPerInterv(numberOfIntervals, logPaths, 
				intervalLen, Metric.POS_HREW,
				"positive human reward per interval");
		System.out.println("Number of logs: " + numLogs);
		System.out.println("Mean positive human rew per interval: " + Arrays.toString(meanPosHRewPerInterv) + "\n\n");
		
		
		double[] meanNegHRewPerInterv = PerStepIntervCtr.getSumOfRewPerInterv(numberOfIntervals, logPaths, 
				intervalLen, Metric.NEG_HREW,
				"negative human reward per interval");
		System.out.println("Number of logs: " + numLogs);
		System.out.println("Mean negative human rew per interval: " + Arrays.toString(meanNegHRewPerInterv) + "\n\n");
		
	}
}
