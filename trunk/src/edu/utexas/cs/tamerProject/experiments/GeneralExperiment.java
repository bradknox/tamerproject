package edu.utexas.cs.tamerProject.experiments;

import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;

public abstract class GeneralExperiment{
	public abstract GeneralAgent createAgent(String[] args, EnvironmentInterface env);
	public abstract void adjustAgentAfterItsInit(String[] args, GeneralAgent agent);
	public abstract void processTrainerUnique(GeneralAgent agent, String trainerUnique);
	public abstract EnvironmentInterface createEnv();
}