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
package rlVizLib.messaging.agentShell;

import java.io.DataInputStream;
import java.util.StringTokenizer;

import org.rlcommunity.rlglue.codec.RLGlue;

import rlVizLib.general.ParameterHolder;
import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.BinaryPayload;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;

public class AgentShellTaskSpecCompatRequest extends AgentShellMessages {

    private String agentName;
    private ParameterHolder theParams;
private String theTaskSpec;

    public AgentShellTaskSpecCompatRequest(GenericMessage theMessageObject) {
        super(theMessageObject);
        
        DataInputStream DIS=BinaryPayload.getInputStreamFromPayload(super.getPayLoad());
        agentName=BinaryPayload.readRawString(DIS);
        theParams=new ParameterHolder(BinaryPayload.readRawString(DIS));
        theTaskSpec=BinaryPayload.readRawString(DIS);
    }

    public ParameterHolder getTheParams() {
        return theParams;
    }
    //This is intended for debugging but works well to be just called to save code duplication
    public static String getRequestMessage(String agentName, ParameterHolder theParams, String theTaskSpec) {
        BinaryPayload P = new BinaryPayload();
        P.writeRawString(agentName);
        if (theParams == null) {
            P.writeRawString("NULL");
        } else {
            P.writeRawString(theParams.stringSerialize());
        }
        P.writeRawString(theTaskSpec);
        String payLoadString = P.getAsEncodedString();

        return AbstractMessage.makeMessage(
                MessageUser.kAgentShell.id(),
                MessageUser.kBenchmark.id(),
                AgentShellMessageType.kAgentShellTaskSpecCompat.id(),
                MessageValueType.kString.id(),
                payLoadString);

    }

    public static AgentShellTaskSpecCompatResponse Execute(String agentName, ParameterHolder theParams, String theTaskSpec) {
        String theRequestString = getRequestMessage(agentName, theParams,theTaskSpec);

        String responseMessage = RLGlue.RL_agent_message(theRequestString);

        AgentShellTaskSpecCompatResponse theResponse;
        try {
            theResponse = new AgentShellTaskSpecCompatResponse(responseMessage);
        } catch (NotAnRLVizMessageException e) {
            System.err.println("In AgentShellLoadRequest: response was not an RLViz Message");
            return null;
        }
        return theResponse;


    }

    public String getAgentName() {
        return agentName;
    }

    public ParameterHolder getParameterHolder() {
        return theParams;
    }
    
    public String getTaskSpec(){
        return theTaskSpec;
    }
}
