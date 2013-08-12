package edu.utexas.cs.tamerProject.experimentTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.lang.String;
import java.lang.Integer;
import java.lang.Double;
import java.lang.Boolean;
import java.io.*;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.agents.tamerrl.TamerRLAgent;
import edu.utexas.cs.tamerProject.params.Params;

/**
 * Loads a .log file and iterates through the time steps it contains,
 * giving the steps as experience for the agent to learn from.
 * 
 * @author bradknox
 *
 */
public class LogTrainer{
	
	private String path;
	private GeneralAgent agent;
	RecordHandler recHandler = new RecordHandler(false);
	TimeStep step;
	Observation baseObs = new Observation();
	Action baseAct = new Action();

	boolean masterLogSwitchStartState;
	boolean startInTrainSess;
	boolean startPaused;
	boolean isTopLevelAgent;
	
	public LogTrainer(String path, GeneralAgent agent) {
		this.path = path;
		this.agent = agent;
	
		String[] agentPackageStrs = agent.getClass().getName().split("[.]");
		System.out.println("Training agent " + agentPackageStrs[agentPackageStrs.length - 1] + 
						" from log file " + path + ".");
		
		/*
		 * Set up agent to learn from log and save agent state that is being changed.
		 */
		masterLogSwitchStartState = agent.masterLogSwitch;
		if (!agent.countTrainingEps)
			agent.setMasterLogSwitch(false);
		startInTrainSess = agent.getInTrainSess();
		startPaused = agent.pause;
		agent.pause = false;
		isTopLevelAgent = agent.getIsTopLevelAgent();
		agent.setIsTopLevelAgent(true);
	}
	
	public void loadLog() {
		recHandler.loadRecord(path);
	}
	public RecordHandler getRecHandler(){
		return recHandler;
	}
	public void setStep(TimeStep step) {
		this.step = step;
	}

	public void trainOneEpoch() {
		loadLog();
		
		System.out.print("Steps: ");
		while (true) {
			//if (step != null)
				//System.out.println("parsed step: " + step + ", startOfEp: " + step.startOfEp + ", endOfEp: " + step.endOfEp);
			//System.out.flush();
			//System.err.flush();
			//System.out.println("\n\n---------------------------------\nNew step");
			
			step = recHandler.getNextTimeStep(baseObs, baseAct);
			if (step == null)
				break;
			processNextLogStep();
			
		}
		if (!agent.countTrainingEps) {
			agent.initRecords();
		}
	}
		
	public void trainForNSteps(int maxSteps){
		int stepsFinished = 0;
		loadLog();
		
		System.out.print("Steps: ");
		while (stepsFinished < maxSteps) {
			//if (step != null)
				//System.out.println("parsed step: " + step + ", startOfEp: " + step.startOfEp + ", endOfEp: " + step.endOfEp);
			//System.out.flush();
			//System.err.flush();
			//System.out.println("\n\n---------------------------------\nNew step");
			
			step = recHandler.getNextTimeStep(baseObs, baseAct);
			if (step == null)
				break;
			processNextLogStep();
			stepsFinished++;
		}
		if (!agent.countTrainingEps) {
			agent.initRecords();
		}
	}
	
	public boolean nextLogStepIfExists(){
		step = recHandler.getNextTimeStep(baseObs, baseAct);
		if (step == null)
			return false;
		processNextLogStep();
		return true;
	}

	/*
	 * Return agent to previous state
	 */
	public void returnAgentToPrevState() {
		if (!agent.countTrainingEps) {
			agent.setMasterLogSwitch(masterLogSwitchStartState);
		}
		if (agent.getInTrainSess() != startInTrainSess)
			agent.toggleInTrainSess();
		agent.pause = startPaused;
		agent.setIsTopLevelAgent(isTopLevelAgent);
	}
	
	

	public void processNextLogStep() {
		if (agent.totalSteps % 100 == 0)
			System.out.print(".");
		if (agent.totalSteps % 1000 == 0)
			System.out.print(agent.totalSteps);
	
		agent.hRewList = step.hRewList;
		if (step.training != agent.getInTrainSess())
			agent.toggleInTrainSess();			
		//if (agent.inTrainSess)
		//	System.out.println("agent in train session");
		//System.out.println("Step ends ep? " + step.endOfEp);
		if (step.startOfEp) {
			System.out.print("|");
			agent.agent_start(step.o.duplicate(), step.timeStamp, step.a.duplicate());
		}
		else if (step.endOfEp) {
			agent.agent_end(step.rew, step.timeStamp);
			if (agent.currEpNum == agent.trainEpLimit) {
				System.out.println("Ending training after " + agent.currEpNum + " episodes.");
				return;
			}
		}
		else {
			agent.agent_step(step.rew, step.o.duplicate(), step.timeStamp, step.a.duplicate());
		}
		//System.out.println("end of step training iteration\n");
	}
	
	
	
	/**
	 * Given a path to a log file and an agent, this replays the log for the 
	 * agent to experience.
	 * @param path
	 * @param agent
	 */
	public static void trainOnLog(String path, GeneralAgent agent, int epochs) {
		LogTrainer thisTrainer = new LogTrainer(path, agent);
		System.out.println("Training from log file " + path);

		/*
		 * Train from log.
		 */
		for (int i = 0; i < epochs; i++) {
			System.out.print("\nEpoch: " + (i + 1) + ". ");
			thisTrainer.trainOneEpoch();
		}
		thisTrainer.returnAgentToPrevState();
	}
	
}