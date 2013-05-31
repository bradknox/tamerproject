/*
Adapted by Brad Knox from RandomAgent.java by Brian Tanner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package edu.utexas.cs.tamerProject.agents.specialty;

import java.net.URL;
import java.util.Random;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.net.URLClassLoader;

import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;


import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.AbstractRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import rlVizLib.general.ParameterHolder;
import rlVizLib.general.hasVersionDetails;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.agent.AgentMessageParser;
import rlVizLib.messaging.agent.AgentMessages;
import rlVizLib.messaging.agentShell.TaskSpecResponsePayload;

import edu.utexas.cs.tamerProject.actSelect.ActionSelect;
import edu.utexas.cs.tamerProject.modeling.*;
import edu.utexas.cs.tamerProject.experimentTools.RecordHandler;
import edu.utexas.cs.tamerProject.featGen.*;
import edu.utexas.cs.tamerProject.params.Params;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.agents.tamer.HRew;
import edu.utexas.cs.tamerProject.agents.tamerrl.TamerRLAgent;

/**
 * ExtActionAgentWrap is used for domains where a sequence of multiple atomic actions are
 * presented to the agent as a single action choice. 
 * <p>
 * This class is specifically used for Tetris, where atomic actions are left, right, etc. 
 * moves but the learning literature typically abstracts actions to choices of piece 
 * placements.
 * <p>
 * Class variables that are inherited from GeneralAgent should not be used. Instead, the
 * variables of the same name in coreAgent should be used, since they are being updated
 * appropriately. For classes that need to interact with those variables, I simply make
 * the variables private and add accessor and mutator methods that are overridden in this
 * class.
 * 
 * 
 * @author bradknox
 *
 */
public class ExtActionAgentWrap extends GeneralAgent{
	public GeneralAgent coreAgent;
	private int[] currExtendedAction;
	private int currExtActI;
	private double rewThisExtAct;
	public Action atomicAct = new Action();
	public boolean callCoreAgentEveryStep = false; // used for logs where the intermediate steps aren't saved
	
	
	// override methods to have them interact with the coreAgent
	public void toggleInTrainSess() {this.coreAgent.toggleInTrainSess();}
	public void togglePause() {this.coreAgent.togglePause();}
	public void receiveKeyInput(char c) {coreAgent.receiveKeyInput(c);}
	public void processPreInitArgs(String[] args) {coreAgent.processPreInitArgs(args);}
	public boolean getRecordRew() {return this.coreAgent.getRecordRew();}
    public void setRecordRew(boolean recordRew) {this.coreAgent.setRecordRew(recordRew);}
    public boolean getRecordLog() {return this.coreAgent.getRecordLog();}
    public void setRecordLog(boolean recordLog) {this.coreAgent.setRecordLog(recordLog);}
    public void setAllowUserToggledTraining(boolean allowUserToggledTraining) {
    	coreAgent.setAllowUserToggledTraining(allowUserToggledTraining);}
    public RecordHandler getRecHandler() {return this.coreAgent.getRecHandler();}
    public boolean getInTrainSess() {return this.coreAgent.getInTrainSess();}
    
	
    public ExtActionAgentWrap() {
        this(getDefaultParameters());
    }

    public ExtActionAgentWrap(ParameterHolder p) {
        super();
    }
    
    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        return p;
    }
    
    public static TaskSpecResponsePayload isCompatible(ParameterHolder P, String TaskSpec){
        return new TaskSpecResponsePayload(false,"");
    }
    
    public void createInnerAgent(){
    	if (coreAgent == null)
    		coreAgent = new TamerAgent();
    }

    // Called when the environment is loaded (when "Load Experiment" is clicked in RLViz)
    public void agent_init(String taskSpec){
    	this.coreAgent.agent_init(taskSpec);
    	this.atomicAct.intArray = new int[1]; //// eventually might make this general to other-sized actions
    }
    
 

	// Called at the beginning of each episode (in RLViz, it's first called when "Start" is first clicked)
    public Action agent_start(Observation o, double time, Action predeterminedAct){
    	Action extendedAction = coreAgent.agent_start(o, time, predeterminedAct);
    	this.rewThisExtAct = 0;
    	this.currExtendedAction = extendedAction.intArray; //// to be more general, would need to store whole action, not just intArray
    	this.currExtActI = 0;
    	this.atomicAct.intArray[0] = this.currExtendedAction[currExtActI]; //// eventually might make this general to other-sized actions
    	return this.atomicAct;
    }
    
        
    public Action agent_step(double r, Observation o, double startTime, Action predeterminedAct) {
    //public Action agent_step(double r, Observation o) {
    	this.rewThisExtAct += r;
    	this.currExtActI++;
    	if (currExtActI >= this.currExtendedAction.length ||
    			this.callCoreAgentEveryStep) {
    		//System.out.println("this.currExtActI: " + this.currExtActI);
    		//System.out.println("asking agent for new extended action.");
    		Action extendedAction = coreAgent.agent_step(this.rewThisExtAct, o, startTime, predeterminedAct);
        	this.currExtendedAction = extendedAction.intArray; //// to be more general, would need to store whole action, not just intArray
        	this.rewThisExtAct = 0;
        	this.currExtActI = 0;
        	//System.out.println("this.currExtendedAction.length: " + this.currExtendedAction.length);
    	}

    	this.atomicAct.intArray[0] = this.currExtendedAction[this.currExtActI];
    	return this.atomicAct;
    }
    
    public void agent_end(double r, double time) {
    	this.rewThisExtAct += r;
    	System.out.println(this.coreAgent.currEpNum + ": " + this.coreAgent.rewThisEp);
    	this.coreAgent.agent_end(r, time);
    }
    
    public void agent_cleanup() {}
    
    public String agent_message(String theMessage) {
        AgentMessages theMessageObject;
        try {
            theMessageObject = AgentMessageParser.parseMessage(theMessage);
        } catch (NotAnRLVizMessageException e) {
            System.err.println("Someone sent random agent a message that wasn't RL-Viz compatible");
            return "I only respond to RL-Viz messages!";
        }
        catch (Exception e){
        	System.err.println("Exception while parsing message: " + e);
        	return "There was a problem with this message.";
        }

        if (theMessageObject.canHandleAutomatically(this)) {
            return theMessageObject.handleAutomatically(this);
        }
//        System.err.println("Didn't know how to respond to message.");
        return null;
    }
    

    public URL getImageURL() {
        return null;
    }
    

    public static void main(String[] args){
    	ExtActionAgentWrap agent = new ExtActionAgentWrap();
    	agent.createInnerAgent();
    	agent.processPreInitArgs(args);
    	//if (agent.glue) {
        	AgentLoader L = new AgentLoader(agent);
        	L.run();
    	//}
    	//else {
    		//agent.runSelf();
    	//}
    }  
    
}