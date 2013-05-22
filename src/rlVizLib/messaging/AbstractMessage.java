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


package rlVizLib.messaging;

/**
 * Abstract Message is the root of the entire RLViz Messaging System. All of the
 * subclasses for agents, environments, agentShell, environmentShell extend
 * Abstract Message. Abstract message using the strategy design pattern to wrap
 * up a {@link GenericMessage}.
 * 
 * @author Brian Tanner
 * 
 */
public class AbstractMessage {
	private GenericMessage theRealMessageObject = null;

	public AbstractMessage(GenericMessage theMessageObject) {
		this.theRealMessageObject = theMessageObject;
	}

	public String getToName() {
		return MessageUser.name(getTo());
	}

	public String getFromName() {
		return MessageUser.name(getFrom());
	}
        
	/**
         * @deprecate
         * @return
         */
        public String getPayLoadTypeName() {
		return getPayloadTypeName();
	}
        
        public String getPayloadTypeName() {
		return MessageValueType.name(getPayloadType());
	}

	/**
	 * @return the theMessageType
	 */
	public int getTheMessageType() {
		return theRealMessageObject.getTheMessageType();
	}

	/**
	 * @return the from
	 */
	public MessageUser getFrom() {
		return theRealMessageObject.getFrom();
	}

	/**
	 * @return the to
	 */
	public MessageUser getTo() {
		return theRealMessageObject.getTo();
	}

	/**
         * @deprecate
	 * @return the payLoadType
	 */
	public MessageValueType getPayLoadType() {
		return getPayloadType();
	}

        public MessageValueType getPayloadType() {
		return theRealMessageObject.getPayloadType();
	}
        
	/**
         * @deprecate
	 * @return the payLoad
	 */
	public String getPayLoad() {
		return getPayload();
	}

        public String getPayload() {
		return theRealMessageObject.getPayload();
	}
        
	/*
	 * Override this if you can handle automatically given a queryable
	 * environment or agent
	 */
	public boolean canHandleAutomatically(Object theReceiver) {
		return false;
	}

	public static String makeMessage(int TO, int FROM, int CMD, int VALTYPE,
			String PAYLOAD) {
		StringBuffer theRequestBuffer = new StringBuffer();
		theRequestBuffer.append("TO=");
		theRequestBuffer.append(TO);
		theRequestBuffer.append(" FROM=");
		theRequestBuffer.append(FROM);
		theRequestBuffer.append(" CMD=");
		theRequestBuffer.append(CMD);
		theRequestBuffer.append(" VALTYPE=");
		theRequestBuffer.append(VALTYPE);
		theRequestBuffer.append(" VALS=");
		theRequestBuffer.append(PAYLOAD);

		return theRequestBuffer.toString();
	}

}
