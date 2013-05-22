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
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.rlcommunity.environments.octopus.components.Compartment;
import org.rlcommunity.environments.octopus.components.Vector2D;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;

public class OctopusStateResponse extends AbstractResponse {

    private List<List<Vector2D>> theCompartmentShapes;

    public OctopusStateResponse(List<Compartment> theCompartments) {
        ArrayList<List<Vector2D>> theShapes = new ArrayList<List<Vector2D>>();
        for (Compartment thisCompartment : theCompartments) {
            theShapes.add(thisCompartment.getShapeForDrawing());
        }
        theCompartmentShapes = theShapes;
    }

    public OctopusStateResponse(String responseMessage) throws NotAnRLVizMessageException {
            GenericMessage theGenericResponse = new GenericMessage(responseMessage);
            String thePayLoadString = theGenericResponse.getPayLoad();
            theCompartmentShapes=readEncodedPayloadFromString(thePayLoadString);
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
            OOS.writeObject(theCompartmentShapes);
            OOS.close();
            byte[] theStringBytes = BOS.toByteArray();
            byte[] b64encoded=Base64.encodeBase64(theStringBytes);
            String theBytesAsString = new String(b64encoded);
            theResponseBuffer.append(theBytesAsString);
            return theResponseBuffer.toString();
        } catch (IOException ex) {
            Logger.getLogger(OctopusStateResponse.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                OOS.close();
            } catch (IOException ex) {
                Logger.getLogger(OctopusStateResponse.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;

    }

    public List<List<Vector2D>> getCompartmentShapes() {
        return theCompartmentShapes;
    }

    private List<List<Vector2D>> readEncodedPayloadFromString(String thePayLoadString) {
        ObjectInputStream OIS = null;
        try {
             byte[] encodedPayload=thePayLoadString.getBytes();
            byte[] payLoadInBytes = Base64.decodeBase64(encodedPayload);
           ByteArrayInputStream BIS = new ByteArrayInputStream(payLoadInBytes);
            OIS = new ObjectInputStream(BIS);
            theCompartmentShapes = (List<List<Vector2D>>) OIS.readObject();
            OIS.close();
            return theCompartmentShapes;
        } catch (IOException ex) {
//            Logger.getLogger(OctopusStateResponse.class.getName()).log(Level.SEVERE, "Payload had length: "+thePayLoadString.length(), ex);
//            List<List<Vector2D>> theReturnList=new ArrayList<List<Vector2D>>();
//            return theReturnList;
            return null;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OctopusStateResponse.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(OIS!=null){
                OIS.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(OctopusStateResponse.class.getName()).log(Level.SEVERE, "Problem deciding octopus state message", ex);
            }
        }
        return null;


    }
};