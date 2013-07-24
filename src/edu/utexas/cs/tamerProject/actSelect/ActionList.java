package edu.utexas.cs.tamerProject.actSelect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.rlcommunity.rlglue.codec.types.Action;

import edu.utexas.cs.tamerProject.featGen.FeatGenerator;

public class ActionList {
	public ArrayList<Action> actions = new ArrayList<Action>();
	public ArrayList<Action> forbiddenActions = new ArrayList<Action>();
	public Random randGenerator = new Random();
	
	
	public ActionList (int[][] theObsIntRanges, double[][] theObsDoubleRanges, 
			int[][] theActIntRanges, double[][] theActDoubleRanges) {
		ArrayList<int[]> possActIntArrays = ActionList.recurseForPossActIntArrays(new int[0], 
																	theObsIntRanges, theObsDoubleRanges, 
																	theActIntRanges, theActDoubleRanges);
		actions.clear();
		for (int[] possActIntArray: possActIntArrays){
			Action possStaticAction = new Action();
			possStaticAction.intArray = possActIntArray;
			actions.add(possStaticAction);
		}
	}
	
	public ArrayList<Action> getActionList(){return actions;} // TODO return copy?
	public ArrayList<Action> getForbiddenActionList(){return forbiddenActions;} // TODO return copy?
	public void clearActionList(){this.actions = new ArrayList<Action>();}
	public void clearForbiddenActionList(){this.forbiddenActions = new ArrayList<Action>();}
	public void addActionToList(Action a){this.actions.add(a.duplicate());}
	public int size(){return this.actions.size();}
	
	public void forbidAction(Action act){System.out.println("Forbidding action " + act); forbiddenActions.add(act);}
	public void allowAction(Action act){
		if (!forbiddenActions.remove(act)) {
			System.out.println("Attempted to allow an action that wasn't forbidden.");
		}
	}
	
	protected static ArrayList<int[]> recurseForPossActIntArrays(int[] actSoFar, int[][] theObsIntRanges, double[][] theObsDoubleRanges, 
			int[][] theActIntRanges, double[][] theActDoubleRanges){
		if (actSoFar.length ==  theActIntRanges.length) { // base case
			ArrayList<int[]> list = new ArrayList<int[]>();
			list.add(actSoFar);
			return list;
		}
		int currActIndex = actSoFar.length;
		int a = theActIntRanges[currActIndex][1];
		int b = theActIntRanges[currActIndex][0];
		int numPossibleValues = (theActIntRanges[currActIndex][1] 
		                          - theActIntRanges[currActIndex][0]) + 1;
		ArrayList<int[]> fullActs = new ArrayList<int[]>();
		// iterate through all possible values of the next action integer
		for (int i = 0; i < numPossibleValues; i++){
			int currVal = theActIntRanges[currActIndex][0] + i;
			int[] newActSoFar = new int[currActIndex + 1];
			for (int j = 0; j < actSoFar.length; j++){
				newActSoFar[j] = actSoFar[j];
			}
			newActSoFar[currActIndex] = currVal;
			fullActs.addAll(ActionList.recurseForPossActIntArrays(newActSoFar, theObsIntRanges, theObsDoubleRanges, 
																theActIntRanges, theActDoubleRanges));
		}
		return fullActs;
	}
	
	
	
	public static int getActIntIndex(int[] actIntArray, ArrayList<Action> possStaticActions){
		int i = 0;
		while (i < possStaticActions.size()) {
			if (Arrays.equals(possStaticActions.get(i).intArray, actIntArray))
				break;
			i++;
		}
		if (i == possStaticActions.size()) {
			System.err.println("\n\nNo act match found for act int array: " + Arrays.toString(actIntArray));
			System.err.print("Possible act int arrays to match: ");
			for (Action act: possStaticActions)
				System.err.print("  " + Arrays.toString(act.intArray));
			System.err.println("Stack:\n" + Arrays.toString(Thread.currentThread().getStackTrace()));
			System.err.println("Killing agent process\n\n");
			System.exit(1);
		}
		return i;
	}
	
	public Action getRandomAction(){
		int actI;
		do {actI = this.randGenerator.nextInt(this.getActionList().size());}
		while(this.getForbiddenActionList().contains(this.getActionList().get(actI)));
		return this.getActionList().get(actI).duplicate();
	}
	public int[] getRandomActIntArray(){
		int actI;
		do {actI = this.randGenerator.nextInt(this.getActionList().size());}
		while(this.getForbiddenActionList().contains(this.getActionList().get(actI)));
		return this.getActionList().get(actI).intArray.clone();
	}
	
}
