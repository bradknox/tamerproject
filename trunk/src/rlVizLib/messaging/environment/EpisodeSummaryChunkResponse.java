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


package rlVizLib.messaging.environment;

import java.util.StringTokenizer;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;

/**
 * This is the workhorse that brings the functionality to the {@link EpisodeSummaryRequest} message.
 * It transports a substring from the log file from the environment to the experiment program.
 * @author Brian Tanner
 */
class EpisodeSummaryChunkResponse extends AbstractResponse {

    String theLogData;

    /**
     * Not storing amount requested inside anymore but if I take it out of constructor
     * then I have a conflict with the constructor that builds object from messsage string
     * @param theLogString
     * @param amountRequested
     */public EpisodeSummaryChunkResponse(String theLogString, int amountRequested) {
        this.theLogData = theLogString;
    }
    
        public EpisodeSummaryChunkResponse(String responseMessage) throws NotAnRLVizMessageException {
        GenericMessage theGenericResponse = new GenericMessage(responseMessage);
        String thePayLoad = theGenericResponse.getPayload();

        this.theLogData=thePayLoad;
    }


    int getAmountReceived() {
        return theLogData.length();
    }

    String getLogData() {
        return theLogData;
    }

    @Override
    public String makeStringResponse() {
        StringBuffer thePayLoadBuffer = new StringBuffer();
        if (theLogData != null) {
            thePayLoadBuffer.append(theLogData);
        }


        String theResponse = AbstractMessage.makeMessage(MessageUser.kBenchmark.id(),
                MessageUser.kEnv.id(),
                EnvMessageType.kEnvResponse.id(),
                MessageValueType.kStringList.id(),
                thePayLoadBuffer.toString());

        return theResponse;
    }
}
