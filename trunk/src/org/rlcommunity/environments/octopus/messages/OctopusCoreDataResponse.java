/* Mountain Car Domain
 * Copyright (C) 2007, Brian Tanner brian@tannerpages.com (http://brian.tannerpages.com/)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */
package org.rlcommunity.environments.octopus.messages;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.rlcommunity.environments.octopus.components.Target;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;

public class OctopusCoreDataResponse extends AbstractResponse {

    private Set<Target> theTargets;
    private double theSurfaceLevel;

    public OctopusCoreDataResponse(Set<Target> theTargets, double theSurfaceLevel) {
        this.theTargets = theTargets;
        this.theSurfaceLevel=theSurfaceLevel;
    }

    public OctopusCoreDataResponse(String responseMessage) throws NotAnRLVizMessageException {
            GenericMessage theGenericResponse = new GenericMessage(responseMessage);
            String thePayLoadString = theGenericResponse.getPayLoad();
            setVarsFromEncodedPayloadString(thePayLoadString);
    }

    public Double getSurfaceLevel() {
        return theSurfaceLevel;
    }

    @Override
    public String makeStringResponse() {
        ObjectOutputStream OOS = null;
        try {
            StringBuffer theResponseBuffer = new StringBuffer();
            theResponseBuffer.append("TO=");
            theResponseBuffer.append(MessageUser.kBenchmark.id());
            theResponseBuffer.append(" FROM=");
            theResponseBuffer.append(MessageUser.kEnv.id());
            theResponseBuffer.append(" CMD=");
            theResponseBuffer.append(EnvMessageType.kEnvResponse.id());
            theResponseBuffer.append(" VALTYPE=");
            theResponseBuffer.append(MessageValueType.kString.id());
            theResponseBuffer.append(" VALS=");
            ByteArrayOutputStream BOS = new ByteArrayOutputStream();
            OOS = new ObjectOutputStream(new BufferedOutputStream(BOS));
            OOS.writeObject(theTargets);
            OOS.writeDouble(theSurfaceLevel);
            OOS.close();
            byte[] theStringBytes = BOS.toByteArray();
            byte[] b64encoded=Base64.encodeBase64(theStringBytes);
            String theBytesAsString = new String(b64encoded);
            theResponseBuffer.append(theBytesAsString);
            return theResponseBuffer.toString();
        } catch (IOException ex) {
            Logger.getLogger(OctopusCoreDataResponse.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                OOS.close();
            } catch (IOException ex) {
                Logger.getLogger(OctopusCoreDataResponse.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;

    }

    public Set<Target> getTargets() {
        return theTargets;
    }

    private void setVarsFromEncodedPayloadString(String thePayLoadString) {
        ObjectInputStream OIS = null;
        try {
            byte[] encodedPayload=thePayLoadString.getBytes();
            byte[] payLoadInBytes = Base64.decodeBase64(encodedPayload);
            ByteArrayInputStream BIS = new ByteArrayInputStream(payLoadInBytes);
            OIS = new ObjectInputStream(BIS);
            theTargets = (Set<Target>) OIS.readObject();
            theSurfaceLevel=OIS.readDouble();
            OIS.close();
        } catch (IOException ex) {
            Logger.getLogger(OctopusCoreDataResponse.class.getName()).log(Level.SEVERE, "Payload had length: "+thePayLoadString.length(), ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OctopusCoreDataResponse.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                OIS.close();
            } catch (IOException ex) {
                Logger.getLogger(OctopusCoreDataResponse.class.getName()).log(Level.SEVERE, "Problem decoding octopus target message", ex);
            }
        }
    }
};