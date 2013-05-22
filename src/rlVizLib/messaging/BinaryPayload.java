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
package rlVizLib.messaging;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Brian Tanner
 */
public class BinaryPayload {

    private final DataOutputStream theOutputStream;
    private final ByteArrayOutputStream BOS;
    private boolean alreadyEncoded = false;

    public BinaryPayload() {
        BOS = new ByteArrayOutputStream();
        theOutputStream = new DataOutputStream(new BufferedOutputStream(BOS));
    }

    private void checkAlreadyEncoded() {
        if (alreadyEncoded) {
            throw new RuntimeException("You CANNOT getOutputStream on " + this.getClass().getName() + " after you have encoded the result.");
        }
    }

    public DataOutputStream getOutputStream() {
        checkAlreadyEncoded();
        return theOutputStream;
    }

    public String getAsEncodedString() {
        alreadyEncoded = true;
        try {
            theOutputStream.close();
            byte[] theStringBytes = BOS.toByteArray();
            byte[] b64encoded = Base64.encodeBase64(theStringBytes);
            String theBytesAsString = new String(b64encoded);
            return theBytesAsString;
        } catch (IOException ex) {
            System.err.println("Problem closing the output stream." + ex);
            Thread.dumpStack();
            System.exit(1);
        }
        return null;
    }

    public static DataInputStream getInputStreamFromPayload(String thePayLoadString) {
        byte[] encodedPayload = thePayLoadString.getBytes();
        byte[] payLoadInBytes = Base64.decodeBase64(encodedPayload);
        ByteArrayInputStream BIS = new ByteArrayInputStream(payLoadInBytes);
        DataInputStream theInputStream = new DataInputStream(new BufferedInputStream(BIS));
        return theInputStream;
    }

    public void writeRawString(String theString) {
        checkAlreadyEncoded();
            try {
        if (theString == null) {
                theOutputStream.writeInt(0);
        }else{
                theOutputStream.writeInt(theString.length());
                theOutputStream.writeBytes(theString);
        }
            } catch (IOException ex) {
                Logger.getLogger(BinaryPayload.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

public static String readRawString(DataInputStream DIS){
        try {
            int numBytes = DIS.readInt();
            if (numBytes == 0) {
                return "";
            }
            byte[] theBytes=new byte[numBytes];
            DIS.readFully(theBytes);
            return new String(theBytes);
        } catch (IOException ex) {
            Logger.getLogger(BinaryPayload.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
}

}