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

package org.rlcommunity.rlviz.agentshell;

import java.lang.reflect.Method;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlviz.dynamicloading.ClassSourcePair;
import org.rlcommunity.rlviz.dynamicloading.EnvOrAgentType;
import org.rlcommunity.rlviz.dynamicloading.LocalJarAgentEnvironmentLoader;

import rlVizLib.general.ParameterHolder;
import rlVizLib.messaging.agentShell.TaskSpecResponsePayload;

public class LocalJarAgentLoader extends LocalJarAgentEnvironmentLoader implements AgentLoaderInterface{

    public LocalJarAgentLoader() {
        super(AgentShellPreferences.getInstance().getList(),EnvOrAgentType.kAgent);
    }


    public AgentInterface loadAgent(String requestedName, ParameterHolder theParams) {
        Object theAgentObject=load(requestedName, theParams);
        if(theAgentObject!=null)return (AgentInterface)theAgentObject;
        return null;
    }

    public TaskSpecResponsePayload loadTaskSpecCompat(String localName, ParameterHolder theParams, String TaskSpec) {
        if (thePublicNames == null) {
            makeList();
        }
        ClassSourcePair thisClassDetails = publicNameToClassSource.get(localName);
        return loadTaskSpecCompatFromClass(thisClassDetails.getTheClass(), theParams,TaskSpec);

    }

    private TaskSpecResponsePayload loadTaskSpecCompatFromClass(Class<?> theClass, ParameterHolder theParams,String theTaskSpec) {
        TaskSpecResponsePayload theTSP = null;

        Class<?>[] paramHolderParams = new Class<?>[]{theParams.getClass(),String.class};

        try {
            Method TaskSpecPayloadMakerMethod = theClass.getDeclaredMethod("isCompatible", paramHolderParams);
            if (TaskSpecPayloadMakerMethod != null) {
                theTSP = (TaskSpecResponsePayload) TaskSpecPayloadMakerMethod.invoke((Object[]) null, new Object[]{theParams, theTaskSpec});
            }
        } catch (Exception e) {
            return TaskSpecResponsePayload.makeUnsupportedPayload();
        }

        return theTSP;
    }


}