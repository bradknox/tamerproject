package edu.utexas.cs.tamerProject.experimentTools;

import java.util.ArrayList;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPAgent;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndTerm;

public class TransitionCntr {
	private static int countTransitions(Observation o, Action a, String logFilePath) {
		Observation stepO = new Observation();
		Action stepA = new Action();
		TimeStep step;
		int transitions = 0;

		RecordHandler recHandler = new RecordHandler(false);
		recHandler.loadRecord(logFilePath);
		
		while (true) {
			step = recHandler.getNextTimeStep(stepO, stepA);
			if (step == null)
				break;
			
			/*
			 * test if transition occurs in this step 
			 */		
			if ((o == null || stepO.equals(o)) &&
							(a == null || stepA.equals(a))) {
				transitions++;
			} 
			// for loopmaze, goal is o.intArray = {5,1} and a.intArray = {3}
		}
		return transitions;
	}
	
	
	private static double[] getEpisodeDurations(Observation o, Action a, String logFilePath) {
		Observation stepO = new Observation();
		Action stepA = new Action();
		TimeStep step;
		int transitions = TransitionCntr.countTransitions(o, a, logFilePath);
		double[] epDurs = new double[transitions];
		int transitionsSoFar = 0;

		RecordHandler recHandler = new RecordHandler(false);
		recHandler.loadRecord(logFilePath);
		
		int stepsThisEp = 0;
		
		while (true) {
			step = recHandler.getNextTimeStep(stepO, stepA);
			if (step == null)
				break;
			
			stepsThisEp++;
			/*
			 * test if transition occurs in this step 
			 */		
			if ((o == null || stepO.equals(o)) &&
							(a == null || stepA.equals(a))) {
				epDurs[transitionsSoFar] = (double)stepsThisEp;
				transitionsSoFar++;
				stepsThisEp = 0;
			} 
			// for loopmaze, goal is o.intArray = {5,1} and a.intArray = {3}
		}
		return epDurs;
	}
	
}
