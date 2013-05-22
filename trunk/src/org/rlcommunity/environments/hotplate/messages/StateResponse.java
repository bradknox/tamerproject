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
package org.rlcommunity.environments.hotplate.messages;

import java.util.StringTokenizer;

import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;

public class StateResponse extends AbstractResponse {

    double[] position;
    int[] safeZone;
    boolean signaled;
    int theAction;

    public StateResponse(int action, double[] position, int[] safeZone, boolean signaled) {
        assert position!=null : "Environment passed a null position vector to State Response message";
        assert position!=null : "Environment passed a null safeZone vector to State Response message";
        this.position = position;
        this.safeZone = safeZone;
        this.signaled = signaled;
        this.theAction = action;
    }

    public StateResponse(String responseMessage) throws NotAnRLVizMessageException {

        GenericMessage theGenericResponse = new GenericMessage(responseMessage);

        String thePayLoadString = theGenericResponse.getPayLoad();

        StringTokenizer stateTokenizer = new StringTokenizer(thePayLoadString, ":");

        theAction = Integer.parseInt(stateTokenizer.nextToken());
        int numDims = Integer.parseInt(stateTokenizer.nextToken());

        position = new double[numDims];
        safeZone = new int[numDims];
        for (int i = 0; i < numDims; i++) {
            position[i] = Double.parseDouble(stateTokenizer.nextToken());
        }
        for (int i = 0; i < numDims; i++) {
            safeZone[i] = Integer.parseInt(stateTokenizer.nextToken());
        }
        signaled = Boolean.parseBoolean(stateTokenizer.nextToken());
    }

    @Override
    public String toString() {
        String theResponse = "StateResponse: not implemented ";
        return theResponse;
    }

    @Override
    public String makeStringResponse() {
        StringBuffer theResponseBuffer = new StringBuffer();
        theResponseBuffer.append("TO=");
        theResponseBuffer.append(MessageUser.kBenchmark.id());
        theResponseBuffer.append(" FROM=");
        theResponseBuffer.append(MessageUser.kEnv.id());
        theResponseBuffer.append(" CMD=");
        theResponseBuffer.append(EnvMessageType.kEnvResponse.id());
        theResponseBuffer.append(" VALTYPE=");
        theResponseBuffer.append(MessageValueType.kStringList.id());
        theResponseBuffer.append(" VALS=");

        theResponseBuffer.append(theAction);
        theResponseBuffer.append(":");
        theResponseBuffer.append(position.length);
        theResponseBuffer.append(":");
        for (int i = 0; i < position.length; i++) {
            theResponseBuffer.append(position[i]);
            theResponseBuffer.append(":");
        }
        for (int i = 0; i < safeZone.length; i++) {
            theResponseBuffer.append(safeZone[i]);
            theResponseBuffer.append(":");
        }
        theResponseBuffer.append(signaled);

        return theResponseBuffer.toString();
    }

    public double[] getPosition() {
        return position;
    }

    public int[] getSafeZone() {
        return safeZone;
    }

    public boolean getSignaled() {
        return signaled;
    }

    public int getAction() {
        return theAction;
    }

};