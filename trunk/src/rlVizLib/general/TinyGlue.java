/*
Copyright 2007 Brian Tanner
brian@tannerpages.com
http://brian.tannerpages.com

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
package rlVizLib.general;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Observation_action;
import org.rlcommunity.rlglue.codec.types.Reward_observation_action_terminal;

/*
 * TinyGlue has the distinct priviledge of calling RL_start() and RL_stop().  We can count on TinyGlue to tell us when certain things are new
 * because the step counter or episode counter will go up.
 * 
 * We're moving to an observable implementation so that components can 
 * subscribe to updates, instead of having them poll.
 */
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

public class TinyGlue extends Observable {

    Observation lastObservation = null;
    Action lastAction = null;
    double lastReward = 0.0d;
	boolean terminal = false;
    int episodeNumber = 0;
    int timeStep = 0;
    int totalSteps = 0;
    double returnThisEpisode;
    double totalReturn;
    Observer lastObserver = null;
	boolean agentEnvSteps = false;

    public void addLastObserver(Observer o) {
        assert (lastObserver == null);
        lastObserver = o;
    }

    @Override
    public void deleteObservers() {
        super.deleteObservers();
        lastObserver = null;
    }

    public void updateObservers(Object theEvent) {
        setChanged();
        super.notifyObservers(theEvent);
        if (lastObserver != null) {
            lastObserver.update(this, theEvent);
        }
        super.clearChanged();

    }

	synchronized public void setAgentEnvSteps(boolean agentEnvSteps) {
		this.agentEnvSteps = agentEnvSteps;
	}

    //This makes sure that 2 people don't run through step at the same time,
    //without locking methods that step has called from blocking on synchroinzed
    //methods in this class.
    private final Semaphore stepSem=new Semaphore(1);
    //returns true of the episode is over
    public  boolean  step() {
        try {
            stepSem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(TinyGlue.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!RLGlue.isInited()) {
            RLGlue.RL_init();
        }

		if (this.agentEnvSteps) {
			return stepAgentEnv();
		}
		else {
			return stepEnvAgent();
		}
	}


	private boolean stepEnvAgent(){

        if (RLGlue.isCurrentEpisodeOver()) {
            Observation firstObservation = RLGlue.RL_env_start();

            synchronized (this) {
                lastObservation = firstObservation;
                lastReward = Double.NaN;

                episodeNumber++;
                timeStep = 1;
                totalSteps++;
                returnThisEpisode = 0.0d;
            }

            updateObservers(firstObservation);

            Action firstAction = RLGlue.RL_agent_start(firstObservation);

                        synchronized (this) {
                lastAction = firstAction;
            }
            updateObservers(firstAction);

        } else {
            Reward_observation_action_terminal ROAT = new Reward_observation_action_terminal();

            Reward_observation_terminal ROT = RLGlue.RL_env_step(lastAction);

            ROAT.o = ROT.getObservation();
            ROAT.r = ROT.getReward();
            boolean isTerminal=ROT.isTerminal();
            ROAT.terminal=0;
            if(isTerminal){
                ROAT.terminal=1;
            }



            synchronized (this) {
                totalSteps++;
                timeStep++;
                lastObservation = ROAT.getObservation();
                lastReward = ROAT.getReward();

                returnThisEpisode += lastReward;
                totalReturn += lastReward;
            }

            updateObservers(ROT);

            if (ROT.isTerminal()) {
                RLGlue.RL_agent_end(ROT.getReward());
            } else {
                ROAT.a = RLGlue.RL_agent_step(ROT.getReward(), ROT.getObservation());
            }

            synchronized (this) {
                if (!ROAT.isTerminal()) {
                    lastAction = ROAT.getAction();
                }
            }
            updateObservers(ROAT);
        }
        stepSem.release();
        return RLGlue.isCurrentEpisodeOver();
    }


    
    /*
     * Instead of the regular ordering of doing env_step() and then agent_step(),
     * this method reverses that order. Consequently, during any pause between calls to
     * stepAgentEnv(), the consequences of the action will already be known. This
     * characteristic is useful for graphically showing the action and observation.
     */
    private  boolean  stepAgentEnv() {
		/*
		 * Start of episode
		 */
		if (RLGlue.isCurrentEpisodeOver()) {
			/*
			 * Get starting observation
			 */
			Observation firstObservation = RLGlue.RL_env_start();

			/*
			 * Reset and increment record-keeping variables
			 */
			synchronized (this) {
				lastObservation = firstObservation;
				lastReward = Double.NaN;

				episodeNumber++;
				timeStep = 1;
				totalSteps++;
				returnThisEpisode = 0.0d;
			}
			updateObservers(firstObservation);
		} 
		/*
		 * Non-starting step
		 */
		else {
			//Reward_observation_action_terminal ROAT = new Reward_observation_action_terminal();

			/*
			 * Send last observation to agent and get new action
			 */
			Action action;
			if (this.timeStep == 1) { // Get first action
				action = RLGlue.RL_agent_start(lastObservation);
			}    
			else { // Regular step, include reward
				action = RLGlue.RL_agent_step(lastReward, lastObservation);
				//updateObservers(ROAT); not sending out ROAT could cause some bugs, but there are no comments about dependencies here, so I'm hoping it won't.
			}    
			synchronized (this) {
				this.lastAction = action;
			}
			updateObservers(action.duplicate());
			
			
			
			/*
			 * Apply action and get new observation and reward
			 */ 
			Reward_observation_terminal ROT = RLGlue.RL_env_step(lastAction);

			//    ROAT.o = ROT.getObservation();
			//    ROAT.r = ROT.getReward();
			//    boolean isTerminal=ROT.isTerminal();
			//    ROAT.terminal=0;
			//    if(isTerminal){
			//    ROAT.terminal=1;
			//    }

			synchronized (this) {
				totalSteps++;
				timeStep++;
				lastObservation = ROT.getObservation();
				lastReward = ROT.getReward();
				terminal = ROT.isTerminal();

				returnThisEpisode += lastReward;
				totalReturn += lastReward;
			}

			updateObservers(ROT);
		}
		
		
		if (terminal) {
			RLGlue.RL_agent_end(this.lastReward);
		}    
		
		
		stepSem.release();
		return RLGlue.isCurrentEpisodeOver();
    }


    synchronized public int getEpisodeNumber() {
        return episodeNumber;
    }

    synchronized public int getTotalSteps() {
        return totalSteps;
    }

    synchronized public int getTimeStep() {
        return timeStep;
    }

    synchronized public Observation getLastObservation() {
        return lastObservation;
    }

    synchronized public Action getLastAction() {
        return lastAction;
    }

    synchronized public Double getLastReward() {
        return lastReward;
    }

    synchronized public double getTotalReturn() {
        return totalReturn;
    }

    synchronized public double getReturnThisEpisode() {
        return returnThisEpisode;
    }
}