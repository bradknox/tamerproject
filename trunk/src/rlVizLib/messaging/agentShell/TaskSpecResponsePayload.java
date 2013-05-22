/*
 * Copyright 2008 Brian Tanner
 * http://bt-recordbook.googlecode.com/
 * brian@tannerpages.com
 * http://brian.tannerpages.com
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package rlVizLib.messaging.agentShell;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import rlVizLib.messaging.BinaryPayload;
import rlVizLib.messaging.environmentShell.*;

/**
 *
 * @author Brian Tanner
 */
public class TaskSpecResponsePayload {
    private boolean supported=false;
    private boolean errorStatus;
    private String errorMessage;


    TaskSpecResponsePayload(DataInputStream DIS) {
        try {
            supported=DIS.readBoolean();            
            errorStatus = DIS.readBoolean();
        } catch (IOException ex) {
            Logger.getLogger(EnvShellTaskSpecResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
        errorMessage=BinaryPayload.readRawString(DIS);
    }
    public boolean getErrorStatus() {
        return errorStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    public static TaskSpecResponsePayload makeUnsupportedPayload(){
        TaskSpecResponsePayload thePayload=new TaskSpecResponsePayload(false, "");
        thePayload.supported=false;
        return thePayload;
    }
    public TaskSpecResponsePayload(boolean errorStatus, String errorMessage) {
        this.supported=true;
        this.errorStatus = errorStatus;
        this.errorMessage = errorMessage;

        if (this.errorMessage == null) {
            this.errorMessage = "";
        }
    }

    String getAsEncodedString() {
        BinaryPayload P = new BinaryPayload();
        DataOutputStream DOS = P.getOutputStream();
        try {
            DOS.writeBoolean(supported);
            DOS.writeBoolean(errorStatus);
        } catch (IOException ex) {
            Logger.getLogger(EnvShellTaskSpecResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
        P.writeRawString(errorMessage);
        
        String encodedPayload = P.getAsEncodedString();
        return encodedPayload;
    }
    
    public String toString(){
        StringBuilder B=new StringBuilder();
        B.append("Task Spec Response Payload\n-----------");
        B.append("\n");
        B.append("Supported: "+getSupported());
        B.append("Error: "+getErrorStatus());
        B.append("\n");
        B.append("Error Message: "+getErrorMessage());
        B.append("\n");
        return B.toString();
        
    }

    public boolean getSupported() {
        return supported;
    }
}
