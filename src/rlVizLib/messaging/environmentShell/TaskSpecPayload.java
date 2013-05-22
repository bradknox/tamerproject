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

package rlVizLib.messaging.environmentShell;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import rlVizLib.messaging.BinaryPayload;

/**
 *
 * @author Brian Tanner
 */
public class TaskSpecPayload {
    private boolean supported;
    private String taskSpec;
    private boolean errorStatus;
    private String errorMessage;

    TaskSpecPayload(DataInputStream DIS) {
        try {
            supported=DIS.readBoolean();
            errorStatus = DIS.readBoolean();
        } catch (IOException ex) {
            Logger.getLogger(EnvShellTaskSpecResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
        taskSpec=BinaryPayload.readRawString(DIS);
        errorMessage=BinaryPayload.readRawString(DIS);
    }

    public String getTaskSpec() {
        return taskSpec;
    }

    public boolean getErrorStatus() {
        return errorStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean getSupported(){
        return supported;
    }

    /**
     * Use this by supporting classes if the feature is not supported.
     * @return
     */
            
    public static TaskSpecPayload makeUnsupportedPayload(){
        TaskSpecPayload theReturn=new TaskSpecPayload("",true,"");
        theReturn.supported=false;
        return theReturn;
    }
    /**
     * Use this constructor if you support this query but there is a proble,/
     * @param taskSpec
     * @param errorStatus
     * @param errorMessage
     */
    public TaskSpecPayload(String taskSpec, boolean errorStatus, String errorMessage) {
        this.supported=true;
        this.taskSpec = taskSpec;
        this.errorStatus = errorStatus;
        this.errorMessage = errorMessage;

        if (this.taskSpec == null) {
            this.taskSpec = "";
        }
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
        P.writeRawString(taskSpec);
        P.writeRawString(errorMessage);
        
        String encodedPayload = P.getAsEncodedString();
        return encodedPayload;
    }
    
    public String toString(){
        StringBuilder B=new StringBuilder();
        B.append("Task Spec Payload\n-----------\n");
        B.append("Supported: "+getSupported()+"\n");
        B.append("Task Spec");
        B.append("\t:");
        B.append(getTaskSpec());
        B.append("\n");
        B.append("Error: "+getErrorStatus());
        B.append("\n");
        B.append("Error Message: "+getErrorMessage());
        B.append("\n");
        return B.toString();
        
    }
}
