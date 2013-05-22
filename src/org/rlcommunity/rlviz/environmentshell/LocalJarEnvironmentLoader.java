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
package org.rlcommunity.rlviz.environmentshell;

import java.lang.reflect.Method;

import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlviz.dynamicloading.EnvOrAgentType;
import org.rlcommunity.rlviz.dynamicloading.LocalJarAgentEnvironmentLoader;
import org.rlcommunity.rlviz.dynamicloading.ClassSourcePair;

import rlVizLib.general.ParameterHolder;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;

public class LocalJarEnvironmentLoader extends LocalJarAgentEnvironmentLoader implements EnvironmentLoaderInterface {

    public LocalJarEnvironmentLoader() {
        super(EnvironmentShellPreferences.getInstance().getList(), EnvOrAgentType.kEnv);
    }

    public EnvironmentInterface loadEnvironment(String requestedName, ParameterHolder theParams) {
        Object theEnvObject = load(requestedName, theParams);
        if (theEnvObject != null) {
            return (EnvironmentInterface) theEnvObject;
        }
        return null;
    }

    public TaskSpecPayload loadTaskSpecPayload(String localName, ParameterHolder theParams) {
        if (thePublicNames == null) {
            makeList();
        }

        ClassSourcePair thisClassDetails = publicNameToClassSource.get(localName);
        return loadTaskSpecPayloadFromClass(thisClassDetails.getTheClass(), theParams);

    }

    private TaskSpecPayload loadTaskSpecPayloadFromClass(Class<?> theClass, ParameterHolder theParams) {
        TaskSpecPayload theTSP = null;

        Class<?>[] paramHolderParams = new Class<?>[]{theParams.getClass()};

        try {
            Method TaskSpecPayloadMakerMethod = theClass.getDeclaredMethod("getTaskSpecPayload", paramHolderParams);
            if (TaskSpecPayloadMakerMethod != null) {
                theTSP = (TaskSpecPayload) TaskSpecPayloadMakerMethod.invoke((Object[]) null, new Object[]{theParams});
            }
        } catch (Exception e) {
            return TaskSpecPayload.makeUnsupportedPayload();
        }

        return theTSP;
    }
}
