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
package org.rlcommunity.environments.cartpole.messages;

import java.util.StringTokenizer;

import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;


public class StateResponse extends AbstractResponse {

    private double x;
    private double x_dot;
    private double angle;
    private double angle_dot;
    private int theAction;

    public StateResponse(int action, double x, double x_dot, double angle, double angle_dot) {
        this.x = x;
        this.x_dot = x_dot;
        this.angle = angle;
        this.angle_dot = angle_dot;
        this.theAction = action;
    }

    public StateResponse(String responseMessage) throws NotAnRLVizMessageException {

        GenericMessage theGenericResponse = new GenericMessage(responseMessage);

        String thePayLoadString = theGenericResponse.getPayLoad();

        StringTokenizer stateTokenizer = new StringTokenizer(thePayLoadString, ":");

        theAction = Integer.parseInt(stateTokenizer.nextToken());
        x = Double.parseDouble(stateTokenizer.nextToken());
        x_dot = Double.parseDouble(stateTokenizer.nextToken());
        angle = Double.parseDouble(stateTokenizer.nextToken());
        angle_dot = Double.parseDouble(stateTokenizer.nextToken());
    }

    @Override
    public String toString() {
        String theResponse = this.getClass().getName()+": not implemented ";
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
        theResponseBuffer.append(x);
        theResponseBuffer.append(":");
        theResponseBuffer.append(x_dot);
        theResponseBuffer.append(":");
        theResponseBuffer.append(angle);
        theResponseBuffer.append(":");
        theResponseBuffer.append(angle_dot);

        return theResponseBuffer.toString();
    }

    public double getX() {
        return x;
    }

    public double getXDot() {
        return x_dot;
    }

    public double getAngle() {
        return angle;
    }

    public int getAction() {
        return theAction;
    }

    public double getAngleDot() {
        return angle_dot;
    }
};