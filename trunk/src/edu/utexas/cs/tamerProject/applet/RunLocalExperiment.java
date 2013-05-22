package edu.utexas.cs.tamerProject.applet;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.rlcommunity.rlglue.codec.LocalGlue;
import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;
//import rlVizLib.general.TinyGlue;
import edu.utexas.cs.tamerProject.glue.TinyGlueExtended;
import edu.utexas.cs.tamerProject.utils.Stopwatch;

// AGENTS
import org.rlcommunity.agents.random.RandomAgent;

// ENVIRONMENTS
import org.rlcommunity.environments.tetris.Tetris;
import org.rlcommunity.environments.mountaincar.MountainCar;
import org.rlcommunity.environments.cartpole.CartPole;
//import org.rlcommunity.environments.acrobot.Acrobot;




/**
 * RunLocalExperiment can be used alone to run an RLGlue-compatible agent with 
 * an RLGLue-compatible environment, without visualization or human interaction.
 * It is also used by RLPanel for visualization through an application or applet. 
 * <p>
 * Comment from Brian Tanner's code that I started from:
 * <p>
 * A simple example of how can you run all components from a single Java class
 * without using network sockets.  Because we remove the socket overhead, these experiments can execute
 * many more steps per second (if they are computationally cheap).
 *<p>
 * The great thing about this approach is that the experiment, agent, and environment are agnostic
 * to how they are being used: locally or over the network.  This means they are still 100% RL-Glue
 * portable and can be used together with any other language.
 */

public class RunLocalExperiment extends Observable{
	public static boolean isApplet = false;
	
	public AgentInterface theAgent = null;
	public EnvironmentInterface theEnvironment = null;
	TinyGlueExtended glue;
	
	public static int numEpisodes = 100000;
	public static int maxStepsPerEpisode=100000;	// TODO this cutoff isn't implemented
	public static long maxTotalSteps = numEpisodes * maxStepsPerEpisode;
	public static int finishExpIfNumStepsInOneEp = Integer.MAX_VALUE; // used to require some minimal performance before the experiment ends; could use return alternatively 
	public Timer stepTimer;
	Stopwatch expStopwatch;
	public static double stepDurInMilliSecs = 200;
//	double lastLoopPause = Double.MIN_VALUE;
//	double LOOP_PAUSE_INTERVAL = 1; // in seconds
	public static int PAUSE_DUR_AFTER_EP = 1000; // in milliseconds
	public boolean expInitialized = false;
	private boolean expRunning = false;
	public boolean expFinished = false;
	public static int rlNumSteps[];
	public static double rlReturn[];
	double avgSteps = 0.0;
	double avgReturn = 0.0;
	private double returnThisEp = 0.0;
	

	
	public void init() {
		/*
		 * Create the Agent
		 */
		if (theAgent == null)
			theAgent = new RandomAgent();
		
		
		/*
		 * Create the Environment
		 */
	 	if (theEnvironment == null) {
//			theEnvironment = new MountainCar();
			theEnvironment = new CartPole();
			//theEnvironment = new Tetris();
			//theEnvironment = new Acrobot();
	 	}
	 	
		/*
		 *  Add environment and agent to glue and make TinyGlue object,
		 *  used to iterate time steps in this class and by the domain
		 *  visualizers to draw the current state and action.  
		 */		
		LocalGlue localGlueImplementation = new LocalGlue(theEnvironment, theAgent);
		RLGlue.setGlue(localGlueImplementation);
		glue = new TinyGlueExtended();
		//System.out.println("Glue class name: " + glue.getClass().getName());
		//System.out.print("Glue methods: ");
		//Method[] methods = glue.getClass().getDeclaredMethods();
	    //for(int i = 0; i < methods.length; i++)
	    //  System.out.println(methods[i].getName());
	    //System.out.println("hashcode for glue: " + glue.getClass().hashCode());
	    //System.out.println("Glue class from: " + glue.getClass().getProtectionDomain().getCodeSource().getLocation());
		glue.setAgentEnvSteps(true);
		System.out.println("just called setAgentEnvSteps()");
		
		expStopwatch = new Stopwatch();
		expStopwatch.startTimer();
	}

	
	/*
	 * Initialize the experiment.
	 */
	public void initExp(){
		System.out.println("\nInit experiment\n");
		this.rlNumSteps = new int[numEpisodes];
		this.rlReturn = new double[numEpisodes];
		System.out.println("Running: " + numEpisodes + 
					" with a cutoff each of: " + maxStepsPerEpisode + " steps.");
		RLGlue.RL_init();
		//startExp();
		this.expInitialized = true;
	}
 
	/*
	 * Start the experiment at initialization or after stopping. If the experiment is already running, ignore call to start.
	 */
	public void startExp() {
		//System.out.println("\nStarting experiment\n" + 
		//		Arrays.toString(Thread.currentThread().getStackTrace()) + "\n");
		if (expRunning)
			return;
		if (RunLocalExperiment.stepDurInMilliSecs < 1)
			startExpTopSpeed();
		else {
			startExpTimer();
		}
		expRunning = true;
	}
	/*
	 * Time steps with regular intervals
	 */
	public void startExpTimer() {
		if (stepTimer != null) {
			stepTimer.cancel();
		}
		stepTimer = new Timer();
        stepTimer.schedule(new TimerTask() {
            public void run() {
            	Thread.currentThread().setName("StepThread-RLETimer");
            	if (RLApplet.DEBUG_TIME) {
            		System.out.println("Time of step thread start: " + ((System.currentTimeMillis() / 1000.0) % 1000));//RunLocalExperiment.this.expStopwatch.getTimeElapsed());
                	System.out.flush();
            	}
            	takeStep();
            	if (RLApplet.DEBUG_TIME) {
            		System.out.println("Time of step thread end: " + ((System.currentTimeMillis() / 1000.0) % 1000));//RunLocalExperiment.this.expStopwatch.getTimeElapsed());
                	System.out.flush();
            	}
            }
		}, new Date(), (long)RunLocalExperiment.stepDurInMilliSecs);
	}
	
	/*
	 * Time steps as quickly as possible. This is currently incompatible with RLApplet.
	 */
	public void startExpTopSpeed() {
	    SwingUtilities.invokeLater(new Runnable() { // TODO probably shouldn't put this in Swing Event Dispatch Thread, which this might be doing
			public void run() {
				while (glue.getEpisodeNumber() <= numEpisodes && expRunning) {
					takeStep();
//					if (((new Date()).getTime() / 1000.0) - lastLoopPause > LOOP_PAUSE_INTERVAL) {
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						lastLoopPause = (new Date()).getTime() / 1000.0;
//						System.out.println("pausing");
//					}
				}
			}
		});
	}
	

	public void takeStep(){
		takeStep(false);
	}
	
	/*
	 * Take a step using TinyGlue. Or, if all episodes have completed, wrap up.
	 */
	public void takeStep(boolean forceStep){
		//System.out.println("Step timer: " + stepTimer);
		///String timeStr = String.format("%f", ((new Date()).getTime() / 1000.0));
		//System.out.print(timeStr.substring(timeStr.length() - 8, timeStr.length() - 3) + " ");
		//System.out.println("step start time in RunLocalExperiment: " + String.format("%f", ((new Date()).getTime() / 1000.0)));
		//System.out.println("\t Steps: "+ glue.getTimeStep());

		boolean endOfEp = false;
		if (RLApplet.DEBUG_TIME) {
			System.out.println("Time before glue.step(): " + ((System.currentTimeMillis() / 1000.0) % 1000));//this.expStopwatch.getTimeElapsed());
			System.out.flush();
		}	
		if (expRunning || forceStep)
			endOfEp = glue.step();
		if (RLApplet.DEBUG_TIME) {
			System.out.println("Time after glue.step(): " + ((System.currentTimeMillis() / 1000.0) % 1000));//this.expStopwatch.getTimeElapsed());
			System.out.flush();
		}
		//glue.notifyObservers(); // TODO remove didn't add a render()
		int epNum = glue.getEpisodeNumber();
		int totalSteps = glue.getTotalSteps();
		this.returnThisEp = glue.getReturnThisEpisode();
		//System.out.println("number of glue observers: " + glue.countObservers());
//		System.out.println("glue.getTimeStep(): " + glue.getTimeStep());
//		System.out.println("glue.getReturnThisEpisode(): " + glue.getReturnThisEpisode());
		if (endOfEp) {
			System.out.print(".");
			System.out.print("Episode " + epNum + " finished. ");
			System.out.println("\t Steps: "+ glue.getTimeStep()); 
			rlNumSteps[epNum - 1] = glue.getTimeStep();
			rlReturn[epNum - 1] = glue.getReturnThisEpisode();
			if (epNum == numEpisodes) {expFinished = true;}
			if (epNum % 20 == 0 && epNum >= 20) {
				double returnSum = 0;
				for (int i = (epNum - 1) - 19 ; i <= (epNum - 1); i++ ) {returnSum += rlReturn[i];}
				double meanReturn = returnSum / 20;
				System.out.println("Mean reward per ep over last 20 eps: " + meanReturn);
			}
		}
		if (totalSteps == maxTotalSteps || glue.getTimeStep() >= finishExpIfNumStepsInOneEp) 
			{expFinished = true;}
			
		if (expFinished) { // "Experiment" is finished. Wrap things up.
    		System.out.println("Experiment finished. Killing here in run local exp");
    		stopExp();
//    		if (stepTimer != null)
//    			stepTimer.cancel(); 

    		glue.notifyObservers();
    		RLGlue.RL_cleanup();
    		
    		
    		/*add up all the steps and all the returns*/
    		for (int i = 0; i < numEpisodes; ++i) {
    			avgSteps += rlNumSteps[i];
    		    avgReturn += rlReturn[i];
    		}
    		
    		avgSteps /= (double)(epNum);
    		avgReturn /= (double)(epNum);
    		
    		System.out.println("\n-----------------------------------------------\n");
    		System.out.println("Number of episodes: " + (epNum));
    		System.out.println("Average number of steps per episode: " +  avgSteps);
    		System.out.println("Average return per episode: " + avgReturn);
    		System.out.println("-----------------------------------------------\n");
    		
    		
    	}
		//System.out.println("step notify time in RunLocalExperiment: " + String.format("%f", ((new Date()).getTime() / 1000.0)));
		updateObservers(endOfEp);
		//System.out.println("step end time in RunLocalExperiment: " + String.format("%f", ((new Date()).getTime() / 1000.0)));

		/*
		 * Do end of episode pause if desired
		 */
		if (endOfEp) {
			if (PAUSE_DUR_AFTER_EP > 0) {
				stopExp();
				updateObservers(endOfEp);
				glue.updateObservers("Test");
				try{TimeUnit.MILLISECONDS.sleep(PAUSE_DUR_AFTER_EP);}
					catch (java.lang.InterruptedException e){
						System.err.println("Exception while trying to sleep: " + e);
					}
				startExp();
			}
		}

	}
	
	/*
	 * Stop the experiment. Best thought of as a pause. Can be restarted by startExp().
	 */
	public void stopExp() {
		expRunning = false;
		if (stepTimer != null)
			stepTimer.cancel();
	}
	
    private void updateObservers(Object theEvent) {
        setChanged();
        super.notifyObservers(theEvent);
        super.clearChanged();

    }
    
	public double getReturnThisEp(){return this.returnThisEp;}
	
	public static void main(String[] args){
		RunLocalExperiment runLocal = new RunLocalExperiment();
		//runLocal.theAgent = agent;
		//runLocal.theEnvironment = env;
		runLocal.init();
		runLocal.initExp();
		runLocal.startExp();
	}
	

}