package org.rlcommunity.environments.keepAway.kaMessages;

import org.rlcommunity.rlglue.codec.RLGlue;
import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;
import rlVizLib.messaging.environment.EnvironmentMessages;

public class KAStateRequest extends EnvironmentMessages {

	public KAStateRequest(GenericMessage theMessageObject){
		super(theMessageObject);
	}

	public static KAStateResponse Execute(){
		String theRequest=AbstractMessage.makeMessage(
				MessageUser.kEnv.id(),
				MessageUser.kBenchmark.id(),
				EnvMessageType.kEnvCustom.id(),
				MessageValueType.kString.id(),
				"GETKASTATE");

		String responseMessage=RLGlue.RL_env_message(theRequest);

		KAStateResponse theResponse;
		try {
			theResponse = new KAStateResponse(responseMessage);
		} catch (NotAnRLVizMessageException e) {
			System.err.println("In KAStateRequest, the response was not RL-Viz compatible");
			theResponse=null;
		}

		return theResponse;

	}
}
