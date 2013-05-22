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

package org.rlcommunity.rlviz.environmentshell;

import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import org.rlcommunity.rlglue.codec.types.RL_abstract_type;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

import rlVizLib.dynamicLoading.Unloadable;
import rlVizLib.general.ParameterHolder;

//
//  JNIEnvironment.java
//  
//
//  Created by mradkie on 9/11/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

public class JNIEnvironment implements EnvironmentInterface, Unloadable {

    public native boolean JNIloadEnvironment(String theFilePath, String theParams);
    public native String JNIenvinit();
    public native void JNIenvstart();
    public native void JNIenvstep(int[] intArray, double[] doubleArray,char[] charArray);
    public native void JNIenvcleanup();
    public native String JNIenvmessage(String s);
    public native int[] JNIgetIntArray();
    public native double[] JNIgetDoubleArray();
    public native char[] JNIgetCharArray();

    public native double JNIgetReward();
    public native int JNIgetTerminal();
    public  boolean validEnv = false;

    private void load_environment(String theFullFilePath, ParameterHolder theParams) {
        validEnv = JNIloadEnvironment(theFullFilePath, theParams.stringSerialize());
    }

    public JNIEnvironment(String theFullFilePath, ParameterHolder theParams) {
        load_environment(theFullFilePath, theParams);
    }

    public String env_init() {
        return JNIenvinit();
    }

    private void fillTypeFromJNI(RL_abstract_type genericVariable){
        int[] theInts = JNIgetIntArray();
        double[] theDoubles = JNIgetDoubleArray();
        char[] theChars=JNIgetCharArray();

        //Why are we doing all of this and not just using the arrays?
        if(theInts.length!=genericVariable.intArray.length){
            genericVariable.intArray=new int[theInts.length];
        }
        if(theDoubles.length!=genericVariable.doubleArray.length){
            genericVariable.doubleArray=new double[theDoubles.length];
        }
        if(theChars.length!=genericVariable.charArray.length){
            genericVariable.charArray=new char[theChars.length];
        }


        System.arraycopy(theInts, 0, genericVariable.intArray, 0, theInts.length);
        System.arraycopy(theDoubles, 0, genericVariable.doubleArray, 0, theDoubles.length);
        System.arraycopy(theChars, 0, genericVariable.charArray, 0, theChars.length);
    }

    public Observation env_start() {
        JNIenvstart();
        Observation theObservation = new Observation();
        fillTypeFromJNI(theObservation);

        return theObservation;
    }

    public Reward_observation_terminal env_step(Action a) {
        JNIenvstep(a.intArray, a.doubleArray, a.charArray);

        Observation theObservation = new Observation();
        fillTypeFromJNI(theObservation);

        double theReward = JNIgetReward();

        int terminalFlag = JNIgetTerminal();

        Reward_observation_terminal ret = new Reward_observation_terminal(theReward, theObservation, terminalFlag==1);

        return ret;
    }

    public void env_cleanup() {
        JNIenvcleanup();
    }


    public String env_message(String message) {
        return JNIenvmessage(message);
    }
    
    public boolean isValid(){
        return validEnv;
    }
}
