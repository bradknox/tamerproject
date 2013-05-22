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
package org.rlcommunity.rlviz.agentshell;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.RL_abstract_type;

import rlVizLib.dynamicLoading.Unloadable;
import rlVizLib.general.ParameterHolder;

/**
 * I'm a bit unhappy how much code is duplicated between the agentShell and
 * the environmentShell.  Maybe at some point we should be combining things more.
 * @author btanner
 */
public class JNIAgent implements AgentInterface, Unloadable {

    private boolean debugThis = false;
    private boolean validAgent=false;
    

    public native boolean JNIloadAgent(String theFullFilePath,String theSerializedParams);

    public native void JNIagentinit(String taskSpecification);

    public native void JNIagentstart(int[] intArray, double[] doubleArray, char[] charArray);

    public native void JNIagentstep(double reward, int[] intArray, double[] doubleArray, char[] charArray);

    public native void JNIagentend(double reward);

    public native void JNIagentcleanup();

    public native String JNIagentmessage(String message);

    //C accessor methods
    public native int[] JNIgetIntArray();

    public native double[] JNIgetDoubleArray();

    public native char[] JNIgetCharArray();

    private void load_agent(String theFullFilePath, ParameterHolder theParams) {
        validAgent=JNIloadAgent(theFullFilePath,theParams.stringSerialize());
    }

    public JNIAgent(String agentName, ParameterHolder theParams) {
        load_agent(agentName, theParams);
    }

    /**
     * This method takes a generic variable and fills it up with values
     * that are stored in the C/C++ agent.
     * @param genericVariable
     */
    private void fillTypeFromJNI(RL_abstract_type genericVariable) {
        int[] theInts = JNIgetIntArray();
        double[] theDoubles = JNIgetDoubleArray();
        char[] theChars = JNIgetCharArray();

        //Why are we doing all of this and not just using the arrays?
        if (theInts.length != genericVariable.intArray.length) {
            genericVariable.intArray = new int[theInts.length];
        }
        if (theDoubles.length != genericVariable.doubleArray.length) {
            genericVariable.doubleArray = new double[theDoubles.length];
        }
        if (theChars.length != genericVariable.charArray.length) {
            genericVariable.charArray = new char[theChars.length];
        }


        System.arraycopy(theInts, 0, genericVariable.intArray, 0, theInts.length);
        System.arraycopy(theDoubles, 0, genericVariable.doubleArray, 0, theDoubles.length);
        System.arraycopy(theChars, 0, genericVariable.charArray, 0, theChars.length);
    }
    // mehtods needed by the Agent interface

    public void agent_init(final String taskSpecification) {
        //	System.out.println(taskSpecification);
        JNIagentinit(taskSpecification);
    }

    public Action agent_start(Observation o) {
        if (debugThis) {
            System.out.println("JAVA : JNIAGENT :: agent_start");
        }

        JNIagentstart(o.intArray, o.doubleArray, o.charArray);
        Action theAction = new Action();
        fillTypeFromJNI(theAction);

        return theAction;
    }

    public Action agent_step(double reward, Observation o) {
        if (debugThis) {
            System.out.println("JAVA : JNIAGENT :: agent_step");
        }

        JNIagentstep(reward, o.intArray, o.doubleArray, o.charArray);
        Action theAction = new Action();
        fillTypeFromJNI(theAction);

        return theAction;
    }

    public void agent_end(double reward) {
        if (debugThis) {
            System.out.println("JAVA : JNIAGENT :: agent_end");
        }
        JNIagentend(reward);
    }

    public void agent_cleanup() {
        JNIagentcleanup();
    }

    public String agent_message(final String message) {
        if (debugThis) {
            System.out.println("JAVA : JNIAGENT :: agent_message");
        }

        return JNIagentmessage(message);
    }

    public boolean isValid(){
        return validAgent;
    }
}