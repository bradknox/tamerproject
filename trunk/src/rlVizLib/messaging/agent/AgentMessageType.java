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

//These numberings aren't in order and there are holes, wanted to make similar ids match between env and agent asmap

public enum AgentMessageType {

    kAgentResponse(0),
    kAgentQueryValuesForObs(1),
    kAgentCustom(2),
    kAgentQuerySupportedVersion(4),
    kAgentQueryVisualizerName(6),
    kAgentGetGraphic(7);
    
    private final int id;

    AgentMessageType(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static String name(int id) {
        if (id == kAgentResponse.id()) {
            return "kAgentResponse";
        }
        if (id == kAgentQueryValuesForObs.id()) {
            return "kAgentQueryValuesForObs";
        }
        if (id == kAgentCustom.id()) {
            return "kAgentCustom";
        }
        if (id == kAgentQuerySupportedVersion.id()) {
            return "kAgentQuerySupportedVersion";
        }
        if (id == kAgentQueryVisualizerName.id()) {
            return "kAgentQueryVisualizerName";
        }
        return "Type: " + id + " is unknown AgentMessageType";
    }

    public static String name(AgentMessageType mType) {
        return name(mType.id());
    }
}