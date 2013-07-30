package edu.utexas.cs.tamerProject.experimentTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.agents.tamer.HRew;
import edu.utexas.cs.tamerProject.params.Params;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndAct;

/**
 * 
 * RecordHandler both logs experience and loads previously saved logs, potentially 
 * by LogTrainer for using as virtual experience or by ExpAnalyzer for statistical 
 * analysis. There are two types of log files. Files appended with ".log" contain a the 
 * observation, action, MDP reward, human reward, a timestamp, and whether a training 
 * session is occurring. Files appended with ".rew" simply have the total MDP reward at 
 * the end of each episode saved, which is useful for quick performance analysis.
 * 
 * writeTimeStep() uses stepToStr() to create a String representation of a TimeStep class
 * and then save it to file. An example of using this class to log experience can be seen
 * in GeneralAgent.
 * 
 * loadRecord() is used to load a .log file, and getNextTimeStep() will iterate through
 * the time steps pulled from that file. The methods getXFromStepStr(), where X changes,
 * each convert the string representation of a time step to an object X (e.g., an 
 * Observation). 
 * @author bradknox
 *
 */
public class RecordHandler{
	
	private static final int OBS_INT_LOC = 0;
	private static final int OBS_DOUB_LOC = 1;
	private static final int ACT_INT_LOC = 2;
	private static final int ACT_DOUB_LOC = 3;
	private static final int MDP_REW_LOC = 4;
	private static final int HREW_LOC = 5;
	private static final int IS_TRAINING_LOC = 6;
	private static final int TIME_STAMP_LOC = 7;
	
	public String fullRecord = "";
	public String rewRecord = "";
	public String recordPath = "";
	public boolean canWriteToFile = true;
	public static boolean usePathAsURL = false; // use the path variable sent to loadRecord() as a URL at which to download log files
	public static int recordLength = 8;
	public static boolean canAccessDrive = true;
	
	String[] timeStepStrs;
	int currStepLineI;
	
	public static int firstDataLine = 1; // default should be 1
	private boolean manualEndOfEp = false; // only implemented for reading logs, not writing them
	private ObsAndAct manualEndOfEpObsAndAct = null;
	
	public enum StepType {NORMAL, START_OF_EP, END_OF_EP, END_OF_LOG}
	
	public RecordHandler(boolean canWriteToFile){
		this.canWriteToFile = canWriteToFile;
	}
	
	public void loadRecord(String path){
		try {
			if (RecordHandler.canAccessDrive) {
				this.timeStepStrs = RecordHandler.getStrArray(path);
			}
			else {
				InputStream in = null;
				if(usePathAsURL){
					in = new URL(path).openStream();
				}
				else{
					in = getClass().getResourceAsStream(path); 
				}
				InputStreamReader isr = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(isr);
				this.timeStepStrs = RecordHandler.getStrArray(br);
			}
			this.currStepLineI = RecordHandler.firstDataLine;
			this.recordPath = path;
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage() + " for path " + path + ".\nExiting.");
			e.printStackTrace();
			System.exit(0);
		}
	}

	
//	public void loadRecord(String path){
//		try {
//			if (RecordHandler.canAccessDrive) {
//				this.timeStepStrs = RecordHandler.getStrArray(path);
//			}
//			else {
//				InputStream in = getClass().getResourceAsStream(path); 
//				InputStreamReader isr = new InputStreamReader(in);
//				BufferedReader br = new BufferedReader(isr);
//				this.timeStepStrs = RecordHandler.getStrArray(br);
//			}
//			this.currStepLineI = RecordHandler.firstDataLine;
//			this.recordPath = path;
//		}
//		catch (Exception e){
//			System.err.println("Error: " + e.getMessage() + " for path " + path + ".\nExiting.");
//			e.printStackTrace();
//			System.exit(0);
//		}
//	}
	
	/**
	 * Allows an observation-action pair to be set as indicators of an episode ending,
	 * in addition to the log file's indication through newlines.
	 */
	public void setManualEndOfEp(Observation o, Action a){
		manualEndOfEp  = true;
		manualEndOfEpObsAndAct = new ObsAndAct(o,a);
	}
	
	private boolean matchesManualEndOfEp(TimeStep step) {
		//System.out.println(manualEndOfEpObsAndAct);
		System.out.println("obs match: " + manualEndOfEpObsAndAct.obsEquals(step.o));
		System.out.println("act match: " + manualEndOfEpObsAndAct.actEquals(step.a));
		return ((manualEndOfEpObsAndAct.obsIsNull() || manualEndOfEpObsAndAct.obsEquals(step.o)) &&
				(manualEndOfEpObsAndAct.actIsNull() || manualEndOfEpObsAndAct.actEquals(step.a)));
	}
	
	private boolean atEpEnd(TimeStep step){
		if (manualEndOfEp) return this.matchesManualEndOfEp(step);
		else return (step.endOfEp);
	}
	
	
	/**
	 * Return the number of episodes from the current step index. The final episode is
	 * only counted if it is complete.
	 * 
	 * @return
	 */	
	public int getNumCompleteEps() {
		int tmpI = this.currStepLineI;
		this.currStepLineI = firstDataLine;
		int numCompleteEps = 0;
		TimeStep step;
		Observation o = new Observation();
		Action a = new Action();
		while ((step = this.getNextTimeStep(o, a)) != null) {
			if (this.atEpEnd(step))
				numCompleteEps++;
		}
		this.currStepLineI = tmpI;
		System.out.println("numCompleteEps: " + numCompleteEps);
		return numCompleteEps;
	}
	
	/**
	 * Return the number of episodes from the current step index. The final episode is
	 * counted whether or not it's complete.
	 * 
	 * @return
	 */
	public int getNumEps() {
		int tmpI = this.currStepLineI;
		this.currStepLineI = firstDataLine;
//		int tmpI = this.currStepLineI;
//		this.currStepLineI = 1;
		System.out.println("currStepLineI in getNumEps: " + currStepLineI);
		int numEps = 0;
		StepType stepType;
		Observation o = new Observation();
		Action a = new Action();
		while ((stepType = this.getTimeStepType()) != StepType.END_OF_LOG) {
			if (stepType ==StepType.START_OF_EP) {
				numEps++;

			}
			//System.out.print(".");
		}
		this.currStepLineI = tmpI;
		System.out.println("numEps (including a potential incomplete one): " + numEps);
		return numEps;
	}
	

	/**
	 * Return an array of statistics for each episode. Input valType determines the content of the
	 * statistics.
	 * 
	 * Human reward might be delayed. To partially account for this, reward received before the
	 * start of an episode is counted towards the previous episode.
	 * 
	 * It would be best for this to start at the first step instead of currStepLineI. If I change this
	 * in the future, I should check whether it breaks anything.
	 */
	public double[] getValPerEp(ExpAnalyzer.DependentVar valType, boolean includeIncomplEps, int maxTotalSteps){
		int tmpI = this.currStepLineI;
		this.currStepLineI = firstDataLine;
//		int tmpI = this.currStepLineI;
//		this.currStepLineI = 1;
		System.out.println("currStepLineI in getValPerEp: " + currStepLineI);
		int totalSteps = 0;
		TimeStep step;
		Observation o = new Observation();
		Action a = new Action();
		double[] valPerEp;
		if (includeIncomplEps)
			valPerEp = new double[this.getNumEps()];
		else
			valPerEp = new double[this.getNumCompleteEps()];
		int epI = 0;
		while(epI < valPerEp.length && totalSteps != maxTotalSteps) {
			step = this.getNextTimeStep(o, a);
			if (step == null) // reached end of logged steps
				break;
			
			//System.out.println("Step: " + step);
			if (valType == ExpAnalyzer.DependentVar.MDP_REW)
				valPerEp[epI] += step.rew;
			else if (valType == ExpAnalyzer.DependentVar.HREW) {
				int thisStepEpI = step.startOfEp? Math.max(epI - 1, 0) : epI;
				for (HRew hRew: step.hRewList) {
					valPerEp[thisStepEpI] += hRew.val;	
				}
			}
			else if (valType == ExpAnalyzer.DependentVar.HREW_INSTS || 
					valType == ExpAnalyzer.DependentVar.HREW_FREQ) {
				int thisStepEpI = step.startOfEp? Math.max(epI - 1, 0) : epI;
				for (HRew hRew: step.hRewList) {
					//if (step.startOfEp && hRew.val != 0)
					//	System.out.println("start of ep");
					if (hRew.val != 0) // Ignoring zero values makes this compatible with python code. However, this will give inaccurate results for an interface that allows zero reward to be explicitly given. 
						valPerEp[thisStepEpI]++;
				}
			}
			else if (valType == ExpAnalyzer.DependentVar.POS_HREW) {
				int thisStepEpI = step.startOfEp? Math.max(epI - 1, 0) : epI;
				for (HRew hRew: step.hRewList) {
					if (hRew.val > 0)
						valPerEp[thisStepEpI] += hRew.val;	
				}
			}
			else if (valType == ExpAnalyzer.DependentVar.NEG_HREW) {
				int thisStepEpI = step.startOfEp? Math.max(epI - 1, 0) : epI;
				for (HRew hRew: step.hRewList) {
					if (hRew.val < 0)
						valPerEp[thisStepEpI] += hRew.val;	
				}
			}
			else if (valType == ExpAnalyzer.DependentVar.EP_DUR) {
				valPerEp[epI]++;
			}
				
			//System.out.println(Arrays.toString(valPerEp));
			if (this.atEpEnd(step)) {
				System.out.println("Episode ends.");
				epI++;
			}
			totalSteps++;
							
		}
		if (valType == ExpAnalyzer.DependentVar.HREW_FREQ) {
			RecordHandler recHandler = new RecordHandler(false);
			recHandler.loadRecord(this.recordPath);
			double[] epDurations = recHandler.getValPerEp(ExpAnalyzer.DependentVar.EP_DUR,
												includeIncomplEps, maxTotalSteps);
			for (int i = 0; i < valPerEp.length; i++)
			{
				valPerEp[i] = valPerEp[i] / epDurations[i];		// assuming that epDurations cannot have a element with value zero
			}
		}
		//System.out.println(Arrays.toString(valPerEp));
		this.currStepLineI = tmpI;
		System.out.println("Total steps in log file: " + totalSteps);
		return valPerEp;
	}
	


	public double[] getMDPRewPerEp(boolean includeIncomplEps, int maxTotalSteps){
		return getValPerEp(ExpAnalyzer.DependentVar.MDP_REW, includeIncomplEps, maxTotalSteps);
//		int tmpI = this.currStepLineI;
//		this.currStepLineI = 1;
//		TimeStep step;
//		Observation o = new Observation();
//		Action a = new Action();
//		double[] rewPerEp = new double[this.getNumCompleteEps()];
//		int stepI = 0;
//		while(stepI < rewPerEp.length) {
//			step = this.getNextTimeStep(o, a);
//			rewPerEp[stepI] += step.rew;
//			if (step.endOfEp)
//				stepI++;
//		}
//		this.currStepLineI = tmpI;
//		return rewPerEp;
	}
	
	public double[] getMDPRewPerEp(){return getMDPRewPerEp(false, -1);}
	
	public double[] getCumMDPRewPerEp(){
		double[] rewPerEp = this.getMDPRewPerEp();
		return RecordHandler.getCumValPerEp(rewPerEp);
	}
	public static double[] getCumValPerEp(double[] valPerEp){
		if (valPerEp.length == 0)
			return new double[0];
		double[] cumRewPerEp = new double[valPerEp.length];
		cumRewPerEp[0] = valPerEp[0];
		for (int stepI = 1; stepI < valPerEp.length; stepI++) {
			cumRewPerEp[stepI] = cumRewPerEp[stepI - 1] + valPerEp[stepI];
		}
		return cumRewPerEp;
	}
	
	public TimeStep getNextTimeStep(Observation o, Action a){
		//System.out.println("this.currStepLineI: " + this.currStepLineI);
		if (this.timeStepStrs.length <= this.currStepLineI ||
				this.timeStepStrs[this.currStepLineI].equals("finished")) // end of log
			return null;
		TimeStep step = new TimeStep();
		String stepStr = this.timeStepStrs[this.currStepLineI];
		this.currStepLineI++; //move line index before testing for start and end of ep
		//System.out.println("stepStr: " + stepStr);
		//System.out.flush();
		if (this.currStepLineI == RecordHandler.firstDataLine + 1 || 
					(this.currStepLineI >= RecordHandler.firstDataLine + 1 && 
							this.timeStepStrs[this.currStepLineI-2].equals("")))
			step.startOfEp = true;
		if (this.currStepLineI < this.timeStepStrs.length &&
				this.timeStepStrs[this.currStepLineI].equals("")) {
			step.endOfEp = true;
			this.currStepLineI++;
			// move past extra new lines
			while (this.currStepLineI < this.timeStepStrs.length &&
					this.timeStepStrs[this.currStepLineI].equals(""))
				this.currStepLineI++;
		}
		step.o = getObsFromStepStr(stepStr, o);
		step.a = getActFromStepStr(stepStr, a);
		step.rew = getRewFromStepStr(stepStr);
		step.hRewList = getHumanRewFromStepStr(stepStr);
		step.timeStamp = getTimeStampFromStepStr(stepStr);
		step.training = getTrainingFromStepStr(stepStr);
		return step;
	}
	

	public StepType getTimeStepType(){
		//System.out.println("this.currStepLineI: " + this.currStepLineI);
		if (this.timeStepStrs.length <= this.currStepLineI ||
				this.timeStepStrs[this.currStepLineI].equals("finished")) // end of log
			return StepType.END_OF_LOG;
		
		this.currStepLineI++; //move line index before testing for start and end of ep
		//System.out.println("stepStr: " + stepStr);
		//System.out.flush();
		if (this.currStepLineI == RecordHandler.firstDataLine + 1 || 
					(this.currStepLineI >= RecordHandler.firstDataLine + 1 && 
							this.timeStepStrs[this.currStepLineI-2].equals("")))
			return StepType.START_OF_EP;
		if (this.currStepLineI < this.timeStepStrs.length &&
				this.timeStepStrs[this.currStepLineI].equals("")) {
			this.currStepLineI++;
			// move past extra new lines
			while (this.currStepLineI < this.timeStepStrs.length &&
					this.timeStepStrs[this.currStepLineI].equals(""))
				this.currStepLineI++;
			return StepType.END_OF_EP;
		}
		return StepType.NORMAL; 
	}

	public void writeTimeStep(String path, Observation o, Action a, 
								double rew, ArrayList<HRew> HRewList, double timeStamp, 
								boolean training) {
		String stepStr = stepToStr(o, a, rew, HRewList, timeStamp, training);
		writeLineToFullLog(path, stepStr, true);
	}

	public void writeEpEnd(String path) {
		String emptyStr = "";
		writeLineToFullLog(path, emptyStr, true);
	}
			
	public static String[] getStrArray(String path) throws Exception{
		FileReader fr = new FileReader(path); 
		BufferedReader br = new BufferedReader(fr);
		return RecordHandler.getStrArray(br);
	}
	
	public static String[] getStrArray(BufferedReader br) throws Exception{
		ArrayList<String> strList = new ArrayList<String>();
		String line;
		while((line = br.readLine()) != null) { 
			strList.add(line); 
		} 
		//strList.remove(1); // only use for psyc-Tetris log files; when uncommented, mark with capitalized "todo"
		return strList.toArray(new String[0]);
	}
	
	

	public static String getPresentWorkingDir(){
	    File pwd = new File (".");
	    String pwdStr = pwd.getAbsolutePath().replace("/.", "");
	    return pwdStr;
	}
	
	
	public static void writeLine(String path, String str, boolean append) {
		try {
			// Create file 
		    FileWriter fstream = new FileWriter(path, append);
		    BufferedWriter out = new BufferedWriter(fstream);
		    out.write(str + "\n");
		    //Close the output stream
		    out.close();
		}
		catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			System.err.println("Error occurred while attempting to write to " + path);
			System.err.println("The usual cause of this error is that the directory does not exist and needs to be created manually (e.g. a \"data\" directory).");
			System.err.println("\nExiting.");
			e.printStackTrace();
			System.exit(0);
		}	   
	}
	
	
	
	
	public void writeParamsToFullLog(String path, Params params){
		String paramsStr = "(" + params.toOneLineStr() + ")";
		writeLineToFullLog(path, paramsStr, false);
	}
	public void writeParamsToRewLog(String path, Params params){
		String paramsStr = "(" + params.toOneLineStr() + ")";
		writeLineToRewLog(path, paramsStr, false);
	}
	
	public void writeLineToFullLog(String path, String str, boolean append) {
		if (this.canWriteToFile)
			RecordHandler.writeLine(path, str, append);
		if (!append)
			this.fullRecord = "";
		this.fullRecord += str + "\n";
	}	
	public void writeLineToRewLog(String path, String str, boolean append) {
		if (this.canWriteToFile)
			RecordHandler.writeLine(path, str, append);
		if (!append)
			this.rewRecord = "";
		this.rewRecord += str + "\n";
	}
	
	
	
	
	
	
	
	/**
	 * Creates a String representation of a TimeStep class that is used in .log files. charArray variables
	 * in the Observation and Action are not currently saved.
	 * @param step
	 * @return
	 */
	public static String stepToStr(TimeStep step){
		return stepToStr(step.o, step.a, step.rew, step.hRewList, step.timeStamp, step.training);
	}
	public static String stepToStr(Observation o, Action a, 
								double rew, ArrayList<HRew> HRewList, double timeStamp,
								boolean training){
		String stepStr = "";
		if (o == null)
			stepStr += "[]:[]:";
		else {
			stepStr += Arrays.toString(o.intArray);
			stepStr += ":";
			stepStr += Arrays.toString(o.doubleArray);
			stepStr += ":";
		}
		if (a == null)
			stepStr += "[]:[]:";
		else {
			stepStr += Arrays.toString(a.intArray);
			stepStr += ":";
			stepStr += Arrays.toString(a.doubleArray);
			stepStr += ":";
		}
		stepStr += rew;
		stepStr += ":";
		stepStr += HRewList.toString();
		stepStr += ":";
		stepStr += training;
		stepStr += ":";
		stepStr += String.format("%f", timeStamp).replace(',', '.');
		return stepStr;
	}

	
	public static Observation getObsFromStepStr(String stepStr, Observation baseObs){
		//System.out.println("stepStr: " + stepStr);
		String[] splitStepStr = stepStr.split(":");
		int[] intArray = null;
		double[] doubleArray = null;
		if (splitStepStr.length == 5) { // python format
			String[] arrayStr = splitStepStr[0].replace("[", "").replace("]", "").replace("(", "").replace(")", "").split(",");
			ArrayList<Integer> intList = new ArrayList<Integer>();
			ArrayList<Double> doubleList = new ArrayList<Double>();
			for (String s: arrayStr){
				try {
					int val = Integer.valueOf(s).intValue();
					intList.add(new Integer(val));
				}
				catch (NumberFormatException e){
					double val = Double.valueOf(s).doubleValue();
					doubleList.add(new Double(val));
				}					
			}
			intArray = new int[intList.size()];
			doubleArray = new double[doubleList.size()];
			for (int i = 0; i < intList.size(); i++) {
				intArray[i] = intList.get(i).intValue();
			}
			for (int i = 0; i < doubleList.size(); i++) {
				doubleArray[i] = doubleList.get(i).doubleValue();
			}
		}
		else if (splitStepStr.length == 6) { // python psyc-Tetris format for abbreviated logs, where no actions are recorded (just state-action features) 
			intArray = new int[0]; 
			doubleArray = getDoubleArrayFromStr(splitStepStr[1]); // this only makes sense for learning directly from features with FeatGen_NoChange  				
		}
		else if (splitStepStr.length == recordLength) { // java format (current)
			intArray = getIntArrayFromStr(splitStepStr[OBS_INT_LOC]);
			doubleArray = getDoubleArrayFromStr(splitStepStr[OBS_DOUB_LOC]); 				
		}
		else {
			System.err.println("Format of log not recognized in getObs(). Length " + splitStepStr.length + ". Original step string: " + stepStr + ". Exiting.");
			System.err.println("Stack trace: \n" + Arrays.toString(Thread.currentThread().getStackTrace()));
			System.exit(1);
		}
		baseObs.intArray = intArray;
		baseObs.doubleArray = doubleArray;
		return baseObs;
	}

	public static Action getActFromStepStr(String stepStr, Action baseAct){
		String[] splitStepStr = stepStr.split(":");
		int[] intArray = null;
		double[] doubleArray = null;
		if (splitStepStr.length == 5) { // python format
			intArray = new int[1];
			doubleArray = new double[0];
			
			String actNumStr = splitStepStr[1];
			intArray[0] = Integer.valueOf(actNumStr).intValue();
		}
		else if (splitStepStr.length == 6) { // python psyc-Tetris format for abbreviated logs, where no actions are recorded (just state-action features) 
			intArray = new int[0];
			doubleArray = new double[0]; 				
		}
		else if (splitStepStr.length == recordLength) { // java format (current)
			intArray = getIntArrayFromStr(splitStepStr[ACT_INT_LOC]);
			doubleArray = getDoubleArrayFromStr(splitStepStr[ACT_DOUB_LOC]); 				
		}
		else {
			System.err.println("Format of log not recognized in getAct(). Length " + splitStepStr.length + ". Original step string: " + stepStr + ". Exiting.");
			System.exit(0);
		}
		baseAct.intArray = intArray;
		baseAct.doubleArray = doubleArray;
		return baseAct;
	}
	

	public static double getRewFromStepStr(String stepStr){
		String[] splitStepStr = stepStr.split(":");
		double rew = 0;
		if (splitStepStr.length == 5) { // general python format
			rew = Double.valueOf(splitStepStr[2]).doubleValue();
		}
		else if (splitStepStr.length == 6) { // python psyc-Tetris format
			rew = Double.valueOf(splitStepStr[3]).doubleValue();
		}
	    else if (splitStepStr.length == recordLength) { // java format (current)
	    	rew = Double.valueOf(splitStepStr[MDP_REW_LOC]).doubleValue();
		}
		else {
			System.err.println("Format of log not recognized in getRewFromStepStr(). Length " + splitStepStr.length + ". Original step string: " + stepStr + ". Exiting.");
			System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
			System.exit(0);
        }
		return rew;
	}

	public static ArrayList<HRew> getHumanRewFromStepStr(String stepStr){
		String[] splitStepStr = stepStr.split(":");
		ArrayList<HRew> hRewList = new ArrayList<HRew>();
		if (splitStepStr.length == 5) { // general python format
			double time = RecordHandler.getTimeStampFromStepStr(stepStr);
			hRewList.add(new HRew(Double.valueOf(splitStepStr[3]).doubleValue(), time));
		}
		else if (splitStepStr.length == 6) { // python psyc-Tetris format
			double time = RecordHandler.getTimeStampFromStepStr(stepStr);
			hRewList.add(new HRew(Double.valueOf(splitStepStr[2]).doubleValue(), time));
		}
	    else if (splitStepStr.length == recordLength) { // java format (current)
	    	String hRewsStr = splitStepStr[HREW_LOC].replace("{","").replace("}","").replace("[","").replace("]","").replace(" ", "");
	    	if (!hRewsStr.equals("")) {
		    	String[] hRewStrList = hRewsStr.split(",");
		    	for (String hRewStr: hRewStrList) {
		    		String[] hRewCompons = hRewStr.split("@");
		    		double val = Double.valueOf(hRewCompons[0]).doubleValue();
		    		double time = Double.valueOf(hRewCompons[1]).doubleValue();
		    		hRewList.add(new HRew(val, time));
		    	}
	    	}
		}
		else {
			System.err.println("Format of log not recognized in getHumanRewFromStepStr(). Length " + splitStepStr.length + ". Original step string: " + stepStr + ". Exiting.");
			System.exit(0);
        }
		return hRewList;
	}
	
	public static double getTimeStampFromStepStr(String stepStr){
		String[] splitStepStr = stepStr.split(":");
		double time = 0;
		if (splitStepStr.length == 5) { // general python format
			time = Double.valueOf(splitStepStr[4]).doubleValue();
		}
		else if (splitStepStr.length == 6) { // python psyc-Tetris format; grabs step start time
			time = Double.valueOf(splitStepStr[4]).doubleValue();
		}
	    else if (splitStepStr.length == recordLength) { // java format (current)
	    	time = Double.valueOf(splitStepStr[TIME_STAMP_LOC].replace(',', '.')).doubleValue();
		}
		else {
			System.err.println("Format of log not recognized in getTimeStamp(). Length " + splitStepStr.length + ". Original step string: " + stepStr + ". Exiting.");
			System.exit(0);
        }
		return time;
	}
	
	public static boolean getTrainingFromStepStr(String stepStr){
		String[] splitStepStr = stepStr.split(":");
		boolean training = true;
		if (splitStepStr.length == 5) { // python format
			;
		}
		else if (splitStepStr.length == 6) { // python psyc-Tetris format; training was always on (though updates only occurred for non-zero feedback)
			;
		}
		else if (splitStepStr.length == recordLength) { // java format (current)
	    	training = Boolean.valueOf(splitStepStr[IS_TRAINING_LOC]).booleanValue();
		}
		else {
			System.err.println("Format of log not recognized in getTraining(). Exiting.");
			System.exit(0);
        }
		return training;
	}
	
	public static int[] getIntArrayFromStr(String s){
		String[] intArrayStr = s.replace("[", "").replace("]", "").replace("(", "").replace(")", "").split(",");
		if (intArrayStr[0].equals(""))
			intArrayStr = new String[0];
		int[] intArray = new int[intArrayStr.length];
		for (int i = 0; i <intArrayStr.length; i++) {
			intArray[i] = Integer.valueOf(intArrayStr[i].replaceAll("\\s","")).intValue();
	 	}
		return intArray;
	}

	public static double[] getDoubleArrayFromStr(String s){
		String[] doubleArrayStr = s.replace("[", "").replace("]", "").replace("(", "").replace(")", "").split(",");
		if (doubleArrayStr[0].equals(""))
			doubleArrayStr = new String[0];
		double[] doubleArray = new double[doubleArrayStr.length];
		for (int i = 0; i < doubleArrayStr.length; i++) {
			doubleArray[i] = Double.valueOf(doubleArrayStr[i]).doubleValue();
		}
		return doubleArray;
	}

	
	public static double[] getMDPRewPerEpFromRewFile(String filePath, int firstDataLine) {
		String[] rewStrArray = null;
		try{
			
			rewStrArray = RecordHandler.getStrArray(filePath);
			System.out.println(Arrays.toString(rewStrArray));
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage() + "\nExiting.");
			e.printStackTrace();
			System.exit(0);
		}
		double [] rewPerEp;
		if (rewStrArray.length > 0) {
			rewPerEp = new double[rewStrArray.length - firstDataLine];
			for (int i = firstDataLine; i < rewStrArray.length; i++) {
				rewPerEp[i-firstDataLine] = Double.valueOf(rewStrArray[i]).doubleValue();
			}
		}
		else {
			rewPerEp = new double[0];
		}
		return rewPerEp;
	}

	
	
	public static void main(String[] args){
		String stepStr = "(-0.41705455772230476, -0.0017983375089664151, 5):0:-1.0:0.0:1264966118.752";
		Observation baseObs = new Observation();
		Action baseAct = new Action();

		baseObs = getObsFromStepStr(stepStr, baseObs);
		baseAct = getActFromStepStr(stepStr, baseAct);
		double rew = getRewFromStepStr(stepStr);
		ArrayList<HRew> hRewList = getHumanRewFromStepStr(stepStr);
		double time = getTimeStampFromStepStr(stepStr);
		
		stepStr = stepToStr(baseObs, baseAct, rew, hRewList, time, false);
		System.out.println("stepStr: " + stepStr);
		
		System.out.println(getObsFromStepStr(stepStr, baseObs));
		System.out.println(getActFromStepStr(stepStr, baseAct));
		System.out.println(getRewFromStepStr(stepStr));
		System.out.println(getHumanRewFromStepStr(stepStr));
		System.out.println(getTimeStampFromStepStr(stepStr));
		
		System.out.println("\n\n");
		RecordHandler recHandler = new RecordHandler(true);
		Params params = new Params();
		recHandler.loadRecord("/Users/bradknox/projects/rlglue-3.04/rl-library/mcshapeagent/data/h/recTraj-todd-1228855955.29.log");
		TimeStep step;
		(new File("./trash.log")).delete();
		String testOutPath = "./trash.log";
		recHandler.writeParamsToFullLog(testOutPath, params);
		while ((step = recHandler.getNextTimeStep(baseObs, baseAct)) != null) {
			System.out.println("\nstep: " + step);
			recHandler.writeTimeStep(testOutPath, step.o, step.a, step.rew, step.hRewList, step.timeStamp, step.training);
			if (step.endOfEp)
				recHandler.writeEpEnd(testOutPath);
		}

		System.out.println("\n\nFinished first writing test. Now reading from that file and writing again.");
		
		recHandler.loadRecord("./trash.log");
		testOutPath = "./trash2.log";
		recHandler.writeParamsToFullLog(testOutPath, params);
		while ((step = recHandler.getNextTimeStep(baseObs, baseAct)) != null) {
			System.out.println("\nstep: " + step);
			recHandler.writeTimeStep(testOutPath, step.o, step.a, step.rew, step.hRewList, step.timeStamp, step.training);
			if (step.endOfEp)
				recHandler.writeEpEnd(testOutPath);
		}

		
	}


}