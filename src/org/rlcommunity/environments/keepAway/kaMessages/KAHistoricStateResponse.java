package org.rlcommunity.environments.keepAway.kaMessages;

import java.util.StringTokenizer;
import java.util.Vector;

import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;

/*
 * This is the first trick, we'll find something better later
 */
public class KAHistoricStateResponse extends AbstractResponse {
Vector<String> KAStateResponsePayloads=null;

public KAHistoricStateResponse(Vector<String> KAStateResponsePayloads) {
	this.KAStateResponsePayloads=KAStateResponsePayloads;
}

	public KAHistoricStateResponse(String responseMessage) throws NotAnRLVizMessageException {
		KAStateResponsePayloads=new Vector<String>();
		
		GenericMessage theGenericResponse = new GenericMessage(responseMessage);
		String thePayLoadString=theGenericResponse.getPayLoad();
		
		
		StringTokenizer firstTokenizer = new StringTokenizer(thePayLoadString, ":");

		int numMessages=Integer.parseInt(firstTokenizer.nextToken());
	//	System.out.println("\t\tPARSING: "+numMessages+" messages in: "+responseMessage);
		
		if(numMessages==0)return;
		

		
		
		
		for(int i=0;i<numMessages;i++){
			KAStateResponsePayloads.add(firstTokenizer.nextToken("@"));
		}
	}

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
		
		theResponseBuffer.append(KAStateResponsePayloads.size());
		theResponseBuffer.append(":");
		for(int i=0;i<KAStateResponsePayloads.size();i++){
			theResponseBuffer.append(KAStateResponsePayloads.get(i));
			theResponseBuffer.append("@");
			
		}
		return theResponseBuffer.toString();
	}

	public Vector<String> getKAStateResponsePayloads() {
		return KAStateResponsePayloads;
	}
	

}
