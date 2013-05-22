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

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.RLGlue;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.BinaryPayload;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.interfaces.HasImageInterface;

public class AgentGraphicRequest extends AgentMessages {

    public AgentGraphicRequest(GenericMessage theMessageObject) {
        super(theMessageObject);
    }

    public static Response Execute() {
        String theRequest = AbstractMessage.makeMessage(
                MessageUser.kAgent.id(),
                MessageUser.kBenchmark.id(),
                AgentMessageType.kAgentGetGraphic.id(),
                MessageValueType.kNone.id(),
                "NULL");

        String responseMessage = RLGlue.RL_agent_message(theRequest);

        Response theResponse;
        try {
            theResponse = new Response(responseMessage);
        } catch (NotAnRLVizMessageException ex) {
            URL defaultURL=AgentGraphicRequest.class.getResource("/images/defaultsplash.png");
            theResponse = new Response(getImageFromURL(defaultURL));
        }
        if(theResponse.getImage()==null){
            URL defaultURL=AgentGraphicRequest.class.getResource("/images/defaultsplash.png");
            theResponse=new Response(getImageFromURL(defaultURL));
        }
        return theResponse;
    }

    private static BufferedImage getImageFromURL(URL imageURL) {
        BufferedImage theImage = null;
        try {
            theImage = ImageIO.read(imageURL);
        } catch (IOException ex) {
            Logger.getLogger(AgentGraphicRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return theImage;
    }

    @Override
    public String handleAutomatically(AgentInterface theAgent) {
        HasImageInterface castedAgent = (HasImageInterface) theAgent;

        BufferedImage theImage = getImageFromURL(castedAgent.getImageURL());

        Response theResponse = new Response(theImage);
        return theResponse.makeStringResponse();
    }

    @Override
    public boolean canHandleAutomatically(Object theAgent) {
        System.out.println("Can handle automatically called on:" + theAgent.getClass().getName());
        return (theAgent instanceof HasImageInterface);
    }

    public static class Response extends AbstractResponse {

        private BufferedImage theImage;

        public Response(BufferedImage theImage) {
            this.theImage = theImage;

        }

        public Response(String responseMessage) throws NotAnRLVizMessageException {
            try {
                GenericMessage theGenericResponse = new GenericMessage(responseMessage);
                String payLoad = theGenericResponse.getPayLoad();
                DataInputStream DIS = BinaryPayload.getInputStreamFromPayload(payLoad);
                theImage = ImageIO.read(DIS);
            } catch (IOException ex) {
                Logger.getLogger(AgentGraphicRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public String makeStringResponse() {
            try {
                BinaryPayload P = new BinaryPayload();
                DataOutputStream DOS = P.getOutputStream();
                ImageIO.write(theImage, "PNG", DOS);
                String theEncodedImage = P.getAsEncodedString();
                String theResponse = AbstractMessage.makeMessage(MessageUser.kBenchmark.id(), MessageUser.kAgent.id(), AgentMessageType.kAgentResponse.id(), MessageValueType.kStringList.id(), theEncodedImage);

                return theResponse;
            } catch (IOException ex) {
                Logger.getLogger(AgentGraphicRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        public BufferedImage getImage() {
            return theImage;

        }
    };
}
