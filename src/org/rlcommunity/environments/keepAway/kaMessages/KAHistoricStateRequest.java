package org.rlcommunity.environments.keepAway.kaMessages;

import org.rlcommunity.rlglue.codec.RLGlue;
import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;
import rlVizLib.messaging.environment.EnvironmentMessages;

public class KAHistoricStateRequest extends EnvironmentMessages {

	public KAHistoricStateRequest(GenericMessage theMessageObject){
		super(theMessageObject);
	}

	public static KAHistoricStateResponse Execute(){
		String theRequest=AbstractMessage.makeMessage(
				MessageUser.kEnv.id(),
				MessageUser.kBenchmark.id(),
				EnvMessageType.kEnvCustom.id(),
				MessageValueType.kString.id(),
				"GETKASTATEHISTORY");

		String responseMessage=RLGlue.RL_env_message(theRequest);

		KAHistoricStateResponse theResponse;
		try {
			theResponse = new KAHistoricStateResponse(responseMessage);
		} catch (NotAnRLVizMessageException e) {
			System.err.println("In KAHistoricStateRequest, the response was not RL-Viz compatible");
			theResponse=null;
		}

		return theResponse;

	}
}
