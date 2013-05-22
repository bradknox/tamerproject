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
package rlVizLib.messaging.environmentShell;

import java.io.DataInputStream;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.BinaryPayload;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;


public class EnvShellTaskSpecResponse extends AbstractResponse {
    private TaskSpecPayload theTaskSpecPayload=null;

    public EnvShellTaskSpecResponse(String taskSpec, boolean errorStatus, String errorMessage) {
        this(new TaskSpecPayload(taskSpec,errorStatus,errorMessage));
    }
    public EnvShellTaskSpecResponse(TaskSpecPayload theTaskSpecPayload){
        this.theTaskSpecPayload=theTaskSpecPayload;
    }
    public TaskSpecPayload getTaskSpecPayload(){
        return theTaskSpecPayload;
    }

//	Constructor when the benchmark is interpreting the returned response
    public EnvShellTaskSpecResponse(String theMessage) throws NotAnRLVizMessageException {
        GenericMessage theGenericResponse = new GenericMessage(theMessage);

        String thePayLoadString = theGenericResponse.getPayload();
        DataInputStream DIS=BinaryPayload.getInputStreamFromPayload(thePayLoadString);
        theTaskSpecPayload=new TaskSpecPayload(DIS);
    }

    @Override
    public String makeStringResponse() {
        String encodedPayload=theTaskSpecPayload.getAsEncodedString();

        String theResponse = AbstractMessage.makeMessage(
                MessageUser.kBenchmark.id(),
                MessageUser.kEnvShell.id(),
                EnvShellMessageType.kEnvShellResponse.id(),
                MessageValueType.kStringList.id(),
                encodedPayload);

        return theResponse;
    }
};