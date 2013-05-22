/*
Copyright 2007 Brian Tanner
http://rl-library.googlecode.com/
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
package org.rlcommunity.environments.hotplate;

import java.util.Random;
import org.rlcommunity.rlglue.codec.types.Observation;

/**
 * This class manages all of the problem parameters, current state variables, 
 * and state transition and reward dynamics.
 *
 * @author btanner
 */
public class HotPlateState {
//	Current State Information

//This will be an N-D vector all in [0,1]
    private double[] position = null;
    private boolean signaledVersion = false;
    private int[] safeZone = null;
    private int numDims = 1;
    private double moveDistance = .05;
    final private double rewardAtGoal = 1.0d;
    final private double rewardPerStep = -1.0d;
    final private Random randomStateGenerator;
    final private Random randomNoiseGenerator;
    private int lastAction = 0;

    private boolean randomStarts=false;
    private double transitionNoise=0.0d;

    HotPlateState(int dimensions, boolean signaled, boolean randomStartStates, double transitionNoise, long randomSeed) {
        this.numDims = dimensions;
        this.signaledVersion = signaled;

        this.randomStarts=randomStartStates;
        this.transitionNoise=transitionNoise;

        if(randomSeed==0){
        this.randomStateGenerator = new Random();
        this.randomNoiseGenerator = new Random();
        }else{
            this.randomStateGenerator=new Random(randomSeed);
            this.randomNoiseGenerator=new Random(randomSeed);
        }

        //Throw away the first few because they first bits are not that random.
        randomStateGenerator.nextDouble();
        randomStateGenerator.nextDouble();
        randomNoiseGenerator.nextDouble();
        randomNoiseGenerator.nextDouble();

        reset();
    }

    public double[] getPosition() {
        return position;
    }

    public boolean getSignaled() {
        return signaledVersion;
    }

    public int[] getSafeZone() {
        return safeZone;
    }

    /**
     * Calculate the reward for the 
     * @return
     */
    public double getReward() {
        if (inGoalRegion()) {
            return rewardAtGoal;
        } else {
            return rewardPerStep;
        }
    }

    public boolean inGoalRegion() {
        return inGoalRegion(position, safeZone, signaledVersion);
    }

    /**
     * IS the agent past the goal marker?
     * @return
     */
    private static boolean inGoalRegion(double[] positionToCheck, int[] safeZoneToCheck, boolean useSafeZones) {
        double goalSize = .05;

        boolean inGoalRegion = false;

        if (!useSafeZones) {
            //This loops through all dimensions and makes sure the agent is
            //along an edge in EACH dimension
            boolean inAllGoals = true;
            for (double thisDimPos : positionToCheck) {
                inAllGoals &= (thisDimPos < goalSize || thisDimPos >= 1.0d - goalSize);
            }
            inGoalRegion = inAllGoals;
        }

        //In this case, the agent has to be in the goal region indicated by
        //safeZone

        if (useSafeZones) {
            boolean inCorrectGoals = true;
            for (int i = 0; i < positionToCheck.length; i++) {
                double thisDimPos = positionToCheck[i];
                int thisSafeZone = safeZoneToCheck[i];

                if (thisSafeZone == 0) {
                    inCorrectGoals &= thisDimPos < goalSize;

                } else {
                    inCorrectGoals &= thisDimPos >= 1.0d - goalSize;
                }
            }
            inGoalRegion = inCorrectGoals;
        }
        return inGoalRegion;
    }

    private double[] generateRandomPosition() {
        double[] tmp_position = new double[numDims];
        for (int i = 0; i < tmp_position.length; i++) {
            tmp_position[i] = randomStateGenerator.nextDouble();
        }
        return tmp_position;

    }

    /*
     * Reset the agent to a random starting state
     */
    protected void reset() {
        //Technically we only need to do this if signaled, but no reason not
        //to calculate it always and use it sometimes
        safeZone = new int[numDims];
        for (int i = 0; i < safeZone.length; i++) {
            safeZone[i] = randomStateGenerator.nextInt(2);
        }

        double[] candidatePosition = new double[numDims];
        for(int i=0;i<candidatePosition.length;i++){
            candidatePosition[i]=.5d;
        }

        if(randomStarts){
            candidatePosition=generateRandomPosition();
        }

        while (inGoalRegion(candidatePosition, safeZone, signaledVersion)) {
            candidatePosition = generateRandomPosition();
        }

        position = candidatePosition;
    }

    void update(int a) {
        lastAction = a;
        int maxMovementAction = (1 << numDims) - 1;
        if (lastAction == maxMovementAction + 1) {
            //Do nothing
            return;
        }

        String actionAsBinary = Integer.toBinaryString(a);
        //Pad the string.
        while (actionAsBinary.length() < numDims) {
            actionAsBinary = "0" + actionAsBinary;
        }
        assert (actionAsBinary.length() == numDims);

        for (int i = 0; i < numDims; i++) {
            char thisAction = actionAsBinary.charAt(i);
            Integer interpretedAsInt = Integer.parseInt("" + thisAction);

            //Should make it at most movedistance
            double thisMoveNoise=2.0d*moveDistance*transitionNoise*(randomNoiseGenerator.nextDouble()-.5d);
            if (interpretedAsInt == 0) {
                position[i] += moveDistance+thisMoveNoise;
                if (position[i] > 1.0d) {
                    position[i] = 1.0d;
                }
            } else {
                position[i] -= moveDistance+thisMoveNoise;
                if (position[i] < 0.0d) {
                    position[i] = 0.0d;
                }
            }
        }

    }

    public int getLastAction() {
        return lastAction;
    }

    int getNumActions() {
        return 1 + (1 << numDims);
    }

    public int getNumDimensions() {
        return numDims;
    }

    Observation makeObservation() {
        int numInts = 0;

        if (signaledVersion) {
            numInts = 1;
        }

        Observation theObservation = new Observation(numInts, numDims, 0);

        if (signaledVersion) {
            int safeZoneAsInt = 0;
            //This will construct a decimal number as if safeZone was a binary string
            for (int thisSafePos : safeZone) {
                safeZoneAsInt = safeZoneAsInt << 1;
                safeZoneAsInt += thisSafePos;
            }
            theObservation.intArray[0] = safeZoneAsInt;
        }

        for (int i = 0; i < numDims; i++) {
            theObservation.doubleArray[i] = position[i];
        }

        return theObservation;
    }
}
