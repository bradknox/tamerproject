package org.rlcommunity.environments.continuousgridworld.messages;


import java.awt.geom.Rectangle2D;
import java.util.StringTokenizer;
import java.util.Vector;

import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;


public class AgentCurrentPositionResponse extends AbstractResponse{
    double agentX;
    double agentY;
    
	
	public AgentCurrentPositionResponse(double agentX,double agentY) {
        this.agentX=agentX;
        this.agentY=agentY;
	}

	public AgentCurrentPositionResponse(String responseMessage) throws NotAnRLVizMessageException {

		GenericMessage theGenericResponse = new GenericMessage(responseMessage);

		String thePayLoadString=theGenericResponse.getPayLoad();

		StringTokenizer stateTokenizer = new StringTokenizer(thePayLoadString, ":");
        String xString=stateTokenizer.nextToken();
        String yString=stateTokenizer.nextToken();

        agentX=Double.parseDouble(xString);
        agentY=Double.parseDouble(yString);
		
	}

	@Override
	public String toString() {
		String theResponse="AgentCurrentPositionResponse: not implemented ";
		return theResponse;
	}


	@Override
	public String makeStringResponse() {
		StringBuffer theResponseBuffer= new StringBuffer();
		theResponseBuffer.append("TO=");
		theResponseBuffer.append(MessageUser.kBenchmark.id());
		theResponseBuffer.append(" FROM=");
		theResponseBuffer.append(MessageUser.kEnv.id());
		theResponseBuffer.append(" CMD=");
		theResponseBuffer.append(EnvMessageType.kEnvResponse.id());
		theResponseBuffer.append(" VALTYPE=");
		theResponseBuffer.append(MessageValueType.kStringList.id());
		theResponseBuffer.append(" VALS=");
        theResponseBuffer.append(agentX);
        theResponseBuffer.append(":");
        theResponseBuffer.append(agentY);


		return theResponseBuffer.toString();
	}

    public double getX(){
        return agentX;
    }

    public double getY(){
        return agentY;
    }


};