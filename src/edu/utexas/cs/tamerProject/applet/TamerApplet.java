package edu.utexas.cs.tamerProject.applet;

import java.util.Date;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import org.rlcommunity.environments.acrobot.Acrobot;
import org.rlcommunity.environments.cartpole.CartPole;
import org.rlcommunity.environments.mountaincar.MountainCar;
import org.rlcommunity.environments.tetris.Tetris;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.imitation.ImitationAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.specialty.ExtActionAgentWrap;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.agents.tamerrl.TamerRLAgent;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMaze;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMazeSecretPath;

/**
 * TamerApplet extends RLApplet's functionality (as does TamerPanel for RLPanel)
 * to (1) include button presses that give reward or control signals, (2) save
 * log data through PHP from remote browsers, (3) and allow other experimentally
 * important features, like having a PreExpPanel that allows user input before 
 * training and a PostExpPanel that gives information after training.
 * 
 * @author bradknox
 *
 */
public class TamerApplet extends RLApplet {
	private static final long serialVersionUID = -4750265772829267948L;
	private int numInTaskSeq = 0;

	protected String trainerUnique = "testUnique";

	private String savedFullLog = "";
	private String savedRewLog = "";
	private String filePrefix = "testExp%testUnique";
	public static boolean isHIT = false;

	protected GeneralAgent agent = null;
	protected EnvironmentInterface env = null;

	PreExpPanel preExpPanel;
	PostExpPanel postExpPanel;
	Timer startCheckTimer;
	Timer resetCheckTimer;

	String agentType = null;
	String lastTask = null;
	
	public void initPanel() {

		String isHITStr = null;
		String numInTaskSeqStr = null;
		String trainControlStr = null;
		String envName = null;
		String fullLogBoolStr = null;
		String rewLogBoolStr = null;
		
		if (IN_BROWSER) {
			// Boolean value turns on Mechanical Turk features if true.
			 isHITStr = getParameter("isHIT");
			 
			 numInTaskSeqStr = getParameter("numInTaskSeq");	
			 
			// Get agent type.
			 this.agentType = getParameter("agent"); // TAMER is default	
			 
			// If false, trainer cannot stop or start training (time step
			// transitions can still occur though).
			 trainControlStr = getParameter("trainingControl");
			 
			// Get task domain.
			 envName = getParameter("domain");
			
			// Determines whether all time-step info is saved to file.
			 fullLogBoolStr = getParameter("fullLog");
			 
			// Determines whether a file with reward per episode is saved.
			 rewLogBoolStr = getParameter("rewLog");
			 
			 this.lastTask = getParameter("lastTask");
		}
		
		
		if (isHITStr != null)
			TamerApplet.isHIT = Boolean.parseBoolean(isHITStr);

		// The number in the task sequence allows multiple applets to have a
		// forced order (useful for Turk).
		
		if (numInTaskSeqStr != null)
			this.numInTaskSeq = Integer.parseInt(numInTaskSeqStr);



		if (agent == null) {
			if (agentType != null) {

				System.out.println("Agent type: " + agentType);
				if (agentType.equals("tamer")) // standard TAMER agent
					agent = new TamerAgent();
				else if (agentType.equals("extActionTamer")) {// For when
																// extended-time
																// actions are
																// use as if
																// they are
																// atomic
																// actions. Used
																// for Tetris.
					agent = new ExtActionAgentWrap();
					((ExtActionAgentWrap) agent).createInnerAgent();
				} else if (agentType.equals("imitation")) {// Learning by
															// demonstration
															// agent. Uses
															// policy
															// regression.
					agent = new ImitationAgent();
					((ImitationAgent) agent).okayToHang = false;
				} else if (agentType.equals("control")) { // Ostensibly not a
															// learning agent,
															// only controlled
															// by human.
															// However, it's
															// really the LfD
															// agent with
															// autonomy disabled
															// (i.e., always in
															// demo mode).
					agent = new ImitationAgent();
					((ImitationAgent) agent).setControlOnlyBeforeStart(true);
					((ImitationAgent) agent).okayToHang = false;
				} 
				else if (agentType.equals("sarsa")) // SARSA(lambda) learning
														// algorithm
					agent = new SarsaLambdaAgent();
				else if (agentType.equals("tamerrl")) {
					agent = new TamerRLAgent();
					String[] agentArgs = { "-expName", "test", "-combMethod",
							"6", "-combParam", "100", "-simulLearning",
							"-eligTrace" };
					agent.processPreInitArgs(agentArgs);
				}
				else {
					System.err
							.println("Agent type unknown. Leave unspecified to get default TAMER agent. Exiting.");
					System.exit(1);
				}
			} else
				// agentType undefined; use TAMER by default
				agent = new TamerAgent();

		}

		System.out.println("agent: " + agent);

		if (trainControlStr != null)
			agent.setAllowUserToggledTraining(Boolean
					.parseBoolean(trainControlStr));

		if (env == null && envName != null) {
			System.out.println("Envinronment name: " + envName);
			if (envName.equals("cart pole"))
				env = new CartPole();
			else if (envName.equals("mountain car"))
				env = new MountainCar();
			else if (envName.equals("tetris")) {
				env = new Tetris();
				if (!agentType.equals("extActionTamer")) {
					System.err
							.println("Using Tetris without ExtActionAgentWrap as agent. Exiting.");
					System.exit(1);
				}
			} 
			else if (envName.equals("loopmaze"))
				env = new LoopMaze();
			else if (envName.equals("acrobot"))
				env = new Acrobot();
		}

		if (fullLogBoolStr != null)
			agent.setRecordLog(Boolean.parseBoolean(fullLogBoolStr));
		savedFullLog = "";

		if (rewLogBoolStr != null)
			agent.setRecordRew(Boolean.parseBoolean(rewLogBoolStr));
		savedRewLog = "";

		if (hasBeenReset) {
			agent.setRecordLog(false);
			agent.setRecordRew(false);
		}
		System.out.println("agent.getRecordLog(): " + agent.getRecordLog());
		System.out.println("agent.getRecordRew(): " + agent.getRecordRew());

		preExpPanel = new PreExpPanel();
		preExpPanel.setSize(this.getWidth(), this.getHeight());
		preExpPanel.init();
		this.getContentPane().add(preExpPanel);

		startCheckTimer = new Timer();
		startCheckTimer.schedule(new TimerTask() {
			public void run() {
				Thread.currentThread().setName("StartCheckTimer-TamerApplet");
				if (!TamerApplet.isHIT || readyToStart()) {
					System.out.println("Version of TamerApplet: " + this.getClass().getSimpleName());
					prepForStartTask();
					startTask();
				}
				System.err.println("startCheckTimer has called run() in TamerApplet. This message should stop after first call to startTask().");
			}
		}, new Date(), (long) 200);

		System.out.println("\n\n\nEnd of TamerApplet.initPanel()\n\n\n");
	}

	protected void prepForStartTask() {
		prepPanelsForStartTask();
		rlPanel.runLocal.initExp();
		// When overloading this method in a child class, add agent changes that
		// go between agent_init() and the first agent_start() call here.
	}

	protected void prepPanelsForStartTask() {
		startCheckTimer.cancel();
		// TamerApplet.this.saveSysInfo(); // won't work on a browser b/c of
		// permissions
		TamerApplet.this.requestFocusInWindow();
		getContentPane().remove(preExpPanel);
		preExpPanel = null;
		rlPanel.init(agent, env);
		rlPanel.runLocal.addObserver(TamerApplet.this); // this seems redundant,
														// since RLApplet
														// already adds itself
		getContentPane().add(rlPanel);
	}

	protected void startTask() {
		System.err.println("TamerApplet.startTask()");
		rlPanel.runLocal.startExp();
		if (TamerApplet.isHIT
				&& !((GeneralAgent) rlPanel.agent).getInTrainSess()
				&& this.agentType!=null && !this.agentType.equals("control")) {
			((GeneralAgent) rlPanel.agent).toggleInTrainSess();
		}
	}

	public void reset() {
		System.out.println("reset() called in TamerApplet");
		if (resetCheckTimer != null)
			resetCheckTimer.cancel();
		// if (preExpPanel != null)
		// preExpPanel.
		if (rlPanel != null && rlPanel.repaintTimer != null) // unnecessary with
																// current
																// implementation,
																// but good to
																// be careful...
			rlPanel.repaintTimer.stop();
		if (postExpPanel != null && postExpPanel.repaintTimer != null)
			postExpPanel.repaintTimer.stop();
		super.reset();
		postExpPanel = null;
	}

	public RLPanel makeRLPanel() {
		return new TamerPanel();
	}

	/*
	 * Called when TinyState object (in RunLocal) changes
	 */
	@Override
	public void update(Observable observable, Object obj) {
		super.update(observable, obj);
		if (this.rlPanel.runLocal.expFinished) {
			System.out
					.println("TamerApplet noticed that the experiment is finished");
			saveLog();
			saveTerminationToLog();
			saveFinishedState();
			saveExpEndStats();
			rlPanel.repaintTimer.stop();
			TamerApplet.this.requestFocusInWindow();
			this.getContentPane().remove(rlPanel);
			rlPanel = null;

			postExpPanel = new PostExpPanel();
			postExpPanel.setIsLastTask(getIsLastTaskFromParams());
			postExpPanel.hitIDNum = Integer.parseInt(this.trainerUnique
					.replaceAll("[^0-9]", "")); // assumes that ID has a number
												// in it

			postExpPanel.setSize(this.getWidth(), this.getHeight());
			postExpPanel.init();
			this.getContentPane().add(postExpPanel);
			resetCheckTimer = new Timer();
			resetCheckTimer.schedule(new TimerTask() {
				public void run() {
					Thread.currentThread().setName("ResetCheckTimer-TamerApplet");
					if (postExpPanel.resetPressed)
						TamerApplet.this.reset();
				}
			}, new Date(), (long) 1000);
		}
	}

	private boolean getIsLastTaskFromParams() {
		return Boolean.parseBoolean(this.lastTask);
	}

	public void saveLog() {
		GeneralAgent agent = ((GeneralAgent) this.rlPanel.agent);

		if (agent.getRecordRew()) { // // send reward-only log from
									// GeneralAgent.recHandler to PHP
			String rewRecord = agent.getRecHandler().rewRecord;
			String unwrittenRecord = rewRecord.replace(this.savedRewLog, "");
			String msg = this.filePrefix + "-" + numInTaskSeq + ".rew|"
					+ unwrittenRecord;
			savedRewLog = rewRecord;
			System.out.println("msg: " + msg);
			this.sendStringToPHP(msg);
		}
		if (agent.getRecordLog()) { // // send full log from
									// GeneralAgent.recHandler to PHP
			System.out
					.println("((GeneralAgent)this.rlPanel.agent).stepsThisEp: "
							+ ((GeneralAgent) this.rlPanel.agent).stepsThisEp);
			String fullRecord = agent.getRecHandler().fullRecord;
			// System.out.println("\n\nfull record: " + fullRecord);
			String unwrittenRecord = fullRecord.replace(this.savedFullLog, "");
			System.out.println("\nunwritten record: " + unwrittenRecord);
			String msg = this.filePrefix + "-" + numInTaskSeq + ".log|"
					+ unwrittenRecord;
			savedFullLog = fullRecord;
			System.out.println("msg: " + msg);
			this.sendStringToPHP(msg);
		}
	}

	public void saveSysInfo() {
		String sysInfo = System.getProperty("os.name");
		sysInfo += "\nOS Version: " + System.getProperty("os.version");
		sysInfo += "\nOS Arch: " + System.getProperty("os.arch");
		sysInfo += "\nJRE version: "
				+ System.getProperty("java.runtime.version");

		String msg = this.filePrefix + "-" + numInTaskSeq + ".info|" + sysInfo;

		System.out.println("Sending system info as msg: " + msg);
		this.sendStringToPHP(msg);
	}

	public void saveExpEndStats() {
		String expEndStats = agent.makeEndInfoStr();
		String msg = this.filePrefix + "-" + numInTaskSeq + ".end|"
				+ expEndStats;
		System.out
				.println("Sending agent stats from end of experiment as msg: "
						+ msg);
		this.sendStringToPHP(msg);
	}

	public void saveTerminationToLog() {
		if (agent.getRecordRew()) { // // send reward-only log from
									// GeneralAgent.recHandler to PHP
			String msg = this.filePrefix + "-" + numInTaskSeq + ".rew|finished";
			System.out.println("msg: " + msg);
			this.sendStringToPHP(msg);
		}
		if (agent.getRecordLog()) { // // send full log from
									// GeneralAgent.recHandler to PHP
			String msg = this.filePrefix + "-" + numInTaskSeq + ".log|finished";
			System.out.println("msg: " + msg);
			this.sendStringToPHP(msg);
		}
	}

	private void saveFinishedState() {
		if (!this.hasBeenReset)
			this.sendStringToPHP(this.filePrefix + ".state|"
					+ this.numInTaskSeq + " ");
	}

	public int getHITState() {
		if (preExpPanel.isHITIDGiven()) {
			this.trainerUnique = preExpPanel.getHITID();
			this.filePrefix = "";
			// if (exp != null) {
			// this.filePrefix += exp.expPrefix + "%";
			// }
			this.filePrefix += this.trainerUnique;
			System.out.println("filePrefix: " + this.filePrefix);
		} else {
			return Integer.MIN_VALUE;
		}
		String stateStr = this.sendStringToPHP(this.filePrefix + ".state-r|");
		System.out.println("stateStr: " + stateStr);
		try {
			String[] statesDivStr = stateStr.split(" ");
			String currStateStr = statesDivStr[statesDivStr.length - 1];
			if (preExpPanel != null)
				preExpPanel.HITStateChecked = true;
//			System.out.println("Current state string: " + currStateStr); // TODO remove
			if (currStateStr.equals(""))
				return 0;
			return Integer.parseInt(currStateStr);
		} catch (Exception e) {
			System.err.println("Exception caught while getting HIT state. "
					+ "This can occur if the first applet task is incomplete "
					+ "and no state file has been saved. If that's the case,"
					+ " ignore this exception.");
			System.err.println(e);
			e.printStackTrace();
			return 0;
		}
	}

	// private void waitForStartState(){
	// //// draw wait screen
	// boolean clicked = true;
	//
	// //// wait
	// while (getHITState() < this.numInTaskSeq - 1 || !clicked) {
	// drawEndScreen((Graphics2D)this.getContentPane().getGraphics());
	// System.out.println("task state: " + getHITState());
	// System.out.println("this state num: " + numInTaskSeq);
	// try {Thread.sleep(2000L);}
	// catch (InterruptedException e) {e.printStackTrace();}
	// System.out.println("clicked: " + clicked);
	// clicked = false;
	// }
	//
	// //// sleep here for the time of the preceding video
	// }

	private boolean readyToStart() {
		if (!preExpPanel.isHITReadyForStart()) {
			int hitState = getHITState();
			System.out.println("task state: " + hitState);
			System.out.println("this state num: " + numInTaskSeq);
			if (hitState >= this.numInTaskSeq - 1)
				preExpPanel.prevHITFinished = true;
		}
		if (preExpPanel.isHITReadyForStart() && preExpPanel.startPressed) {
			return true;
		} else {
			// // draw wait screen
			preExpPanel.repaint();
			// System.out.println("preExpPanel.startPressed: " +
			// preExpPanel.startPressed);
			preExpPanel.startPressed = false;
			return false;
		}
	}

}
