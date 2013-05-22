package edu.utexas.cs.tamerProject.demos.tetris;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;

import org.rlcommunity.environments.tetris.Tetris;

import edu.utexas.cs.tamerProject.applet.RLPanel;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.applet.TamerApplet;
import edu.utexas.cs.tamerProject.applet.TamerPanel;
import edu.utexas.cs.tamerProject.experimentTools.RecordHandler;
import edu.utexas.cs.tamerProject.experimentTools.tetris.MakeJavaLogLearnerAgent;
import edu.utexas.cs.tamerProject.experiments.GeneralExperiment;
import edu.utexas.cs.tamerProject.modeling.IncGDLinearModel;
import edu.utexas.cs.tamerProject.agents.specialty.ExtActionAgentWrap;


public class TetrisTamerFromLog extends TamerApplet{
	private static final long serialVersionUID = -6521668694138979212L;
	
	GeneralExperiment exp;
	int catI = 3;
	
	public void initPanel() {
		String catIStr = getParameter("catI");
		System.out.println("catIStr: " + catIStr);
		System.out.flush();
		if (catIStr != null) {
			this.catI = Integer.parseInt(catIStr);
		}
		System.out.println("catI: " + catI);
		
		
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
		RunLocalExperiment.stepDurInMilliSecs = 100;
		TamerPanel.DISPLAY_TRAINING = false;
		RLPanel.enableSpeedControls = true;
		RLPanel.enableSingleStepControl = true;
		RLPanel.DISPLAY_SECONDS_FOR_TIME = true;
		RLPanel.DISPLAY_REW_THIS_EP = true;
		RLPanel.PRINT_REW_AS_INT = true;
		RLPanel.nameForRew = "Lines cleared";
		
				
		super.initPanel();
	}
	
	
	protected void prepForStartTask(){
		prepPanelsForStartTask();
		rlPanel.runLocal.initExp();
		
		// Experiment-specific code below.
		if (exp != null) {
			exp.adjustAgentAfterItsInit(TetrisTamerExpHelper.getDebugArgsStrArray(), agent);
			exp.processTrainerUnique(agent, TetrisTamerFromLog.this.trainerUnique);
		}

		this.trainAgentFromLog(1);
		this.agent.toggleInTrainSess();
	}
	
	private void trainAgentFromLog(int numEpochs){
		int maxSteps = 10000;
	
		String[] logFilePaths = {"/edu/utexas/cs/tamerProject/demos/tetris/tetrisz130-2-0.06.log", 
								"/edu/utexas/cs/tamerProject/demos/tetris/tetrisz135-2-76.5.log", 
								"/edu/utexas/cs/tamerProject/demos/tetris/tetrisz137-2-462.6.log", 
								"/edu/utexas/cs/tamerProject/demos/tetris/tetrisz157-2-2167.16.log"};
		

		RecordHandler.canAccessDrive = false;
		String logFilePath = logFilePaths[catI];
		System.out.println("Training log: " + logFilePath);


		
		String taskSpec = (Tetris.getTaskSpecPayload(Tetris.getDefaultParameters())).getTaskSpec();
		double[] learnedWts = MakeJavaLogLearnerAgent.getHWeightsFromLog(logFilePath, maxSteps, numEpochs, taskSpec);
		//double[] learnedWts = {3.930045133198139e-06, 8.078609058104126e-06, 3.6151474330809797e-06, 4.893020881997718e-06, 3.7982191718819994e-06, 1.0601453524522298e-05, 1.0170461741251863e-05, 3.506808290087436e-06, -3.1285757641770864e-07, 3.2919273426427274e-06, 1.0843303042934514e-05, -2.4478709704656244e-06, -4.25102626636631e-06, -2.3385936368617054e-06, -1.0879580106492503e-06, -2.0759045857794822e-07, -3.1917533659013483e-06, 1.3467462944191388e-06, -5.081626331068921e-06, 2.3269106122931006e-06, -2.151541175422917e-05, 4.290207444031694e-07, -1.4845998302961572e-06, 2.761189014555942e-06, 5.101945841614457e-06, 3.0827504712145703e-06, 2.33881887110659e-06, 2.3106015035037644e-06, 9.80699977452736e-06, 7.996244027111863e-06, 3.4544071167129234e-06, 1.0842325185367549e-07, 3.5057794935823326e-06, 7.867167438688134e-06, -3.096699345306139e-05, -3.826645938062033e-05, -4.811713219905927e-05, -1.8115818677063897e-05, -4.6670583215099104e-05, -3.6143807988894375e-05, -1.3627752830864685e-05, -5.851786744910292e-05, 1.2731038645428353e-05, -0.0005724030327000401, 2.195433329551164e-05, -5.3109070938694076e-05, 3.396662642364187e-05}; // {3.9300451331981394e-06, 8.0786090581041263e-06, 3.6151474330809797e-06, 4.8930208819977178e-06, 3.7982191718819994e-06, 1.0601453524522298e-05, 1.0170461741251863e-05, 3.5068082900874361e-06, -3.1285757641770864e-07, 3.2919273426427274e-06, 1.0843303042934514e-05, -2.4478709704656244e-06, -4.2510262663663102e-06, -2.3385936368617054e-06, -1.0879580106492503e-06, -2.0759045857794822e-07, -3.1917533659013483e-06, 1.3467462944191388e-06, -5.081626331068921e-06, 2.3269106122931006e-06, -2.1515411754229169e-05, 4.2902074440316938e-07, -1.4845998302961572e-06, 2.7611890145559421e-06, 5.1019458416144572e-06, 3.0827504712145703e-06, 2.3388188711065901e-06, 2.3106015035037644e-06, 9.8069997745273607e-06, 7.9962440271118629e-06, 3.4544071167129234e-06, 1.0842325185367549e-07, 3.5057794935823326e-06, 7.8671674386881343e-06, -3.0966993453061387e-05, -3.8266459380620331e-05, -4.811713219905927e-05, -1.8115818677063897e-05, -4.6670583215099104e-05, -3.6143807988894375e-05, -1.3627752830864685e-05, -5.851786744910292e-05, 1.2731038645428353e-05, -0.00057240303270004013, 2.1954333295511639e-05, -5.3109070938694076e-05, 3.3966626423641867e-05};
		
		System.out.println("Learned " + learnedWts.length +  " weights: " + Arrays.toString(learnedWts));
		double[] learnedWtsWOBias = Arrays.copyOf(learnedWts, learnedWts.length - 1);
		
		
		ExtActionAgentWrap demoAgent = (ExtActionAgentWrap)agent;
		((IncGDLinearModel)demoAgent.coreAgent.model).setWeights(learnedWtsWOBias);
		((IncGDLinearModel)demoAgent.coreAgent.model).setBiasWt(learnedWts[learnedWts.length-1]);
		
		
	}


	

	
	
}