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

import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.GenericMessageParser;
import rlVizLib.messaging.NotAnRLVizMessageException;

public class AgentShellMessageParser extends GenericMessageParser {

    public static AgentShellMessages parseMessage(String theMessage) throws NotAnRLVizMessageException {
        GenericMessage theGenericMessage = new GenericMessage(theMessage);
        return AgentShellMessageParser.makeMessage(theGenericMessage);
    }

    public static AgentShellMessages makeMessage(GenericMessage theGenericMessage) {
        int cmdId = theGenericMessage.getTheMessageType();
        if (cmdId == AgentShellMessageType.kAgentShellListRequest.id()) {
            return new AgentShellListRequest(theGenericMessage);
        }
        if (cmdId == AgentShellMessageType.kAgentShellLoad.id()) {
            return new AgentShellLoadRequest(theGenericMessage);
        }
        if (cmdId == AgentShellMessageType.kAgentShellUnload.id()) {
            return new AgentShellUnLoadRequest(theGenericMessage);
        }
        if (cmdId == AgentShellMessageType.kAgentShellTaskSpecCompat.id()) {
            return new AgentShellTaskSpecCompatRequest(theGenericMessage);
        }

        System.out.println("AgentShellMessageParser - unknown query type: " + cmdId);
        Thread.dumpStack();
        System.exit(1);
        return null;
    }
}
