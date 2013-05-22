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
package rlVizLib.messaging.agent;

import java.util.StringTokenizer;
import java.util.Vector;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.types.Observation;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.utilities.UtilityShop;
import rlVizLib.visualization.QueryableAgent;

/**
 * This mostly by the {@URL ValueFunctionVizComponent}, this message sends a vector
 * of observations to the agent and receives in return a vector of values.
 * @author Brian Taner
 */
public class AgentValueForObsRequest extends AgentMessages {

    Vector<Observation> theRequestObservations = new Vector<Observation>();

    public AgentValueForObsRequest(GenericMessage theMessageObject) {
        super(theMessageObject);

        String thePayLoad = super.getPayload();

        StringTokenizer obsTokenizer = new StringTokenizer(thePayLoad, ":");

        String numValuesToken = obsTokenizer.nextToken();
        int numValues = Integer.parseInt(numValuesToken);
        assert (numValues >= 0);
        for (int i = 0; i < numValues; i++) {
            String thisObsString = obsTokenizer.nextToken();
            theRequestObservations.add(UtilityShop.buildObservationFromString(thisObsString));
        }

    }

    public static AgentValueForObsResponse Execute(Vector<Observation> theRequestObservations) throws NotAnRLVizMessageException {
        StringBuffer thePayLoadBuffer = new StringBuffer();

        //Tell them how many
        thePayLoadBuffer.append(theRequestObservations.size());

        for (int i = 0; i < theRequestObservations.size(); i++) {
            thePayLoadBuffer.append(":");
            UtilityShop.serializeObservation(thePayLoadBuffer, theRequestObservations.get(i));
        }

        String theRequest = AbstractMessage.makeMessage(
                MessageUser.kAgent.id(),
                MessageUser.kBenchmark.id(),
                AgentMessageType.kAgentQueryValuesForObs.id(),
                MessageValueType.kStringList.id(),
                thePayLoadBuffer.toString());

        String responseMessage = RLGlue.RL_agent_message(theRequest);

        AgentValueForObsResponse theResponse;
        theResponse = new AgentValueForObsResponse(responseMessage);

        return theResponse;

    }

    /**
     * @return the theRequestStates
     */
    public Vector<Observation> getTheRequestObservations() {
        return theRequestObservations;
    }

    @Override
    public boolean canHandleAutomatically(Object theReceiver) {
        return (theReceiver instanceof QueryableAgent);
    }

    @Override
    public String handleAutomatically(AgentInterface theAgent) {
        QueryableAgent castedAgent = (QueryableAgent) theAgent;

        Vector<Double> theValues = new Vector<Double>();

        for (int i = 0; i < theRequestObservations.size(); i++) {
            theValues.add(castedAgent.getValueForState(theRequestObservations.get(i)));
        }

        AgentValueForObsResponse theResponse = new AgentValueForObsResponse(theValues);
        String stringResponse = theResponse.makeStringResponse();

        return stringResponse;
    }
}
