/*
Copyright 2008 Brian Tanner
http://bt-recordbook.googlecode.com/
brian@tannerpages.com
http://research.tannerpages.com
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
package org.rlcommunity.rlviz.settings;

import rlVizLib.general.ParameterHolder;

/**
 * I very coarsely copied this out of the bt-recordbook project.  It is handy.
 * I'm not sure if it should be in RLVizLib or RLVizApp. I'm going to use 
 * app for now, because the reason I want to use it is to handle app parameters.
 * @author Brian Tanner
 */
public class RLVizSettings {

    private final static SettingsLoader theSettingsLoader;
    private final static ParameterHolder globalSettings;

    static {
        globalSettings = new ParameterHolder();

        //Defaults
        globalSettings.addStringParam("settingsfile","");
//        globalSettings.addStringParam("env-agent-jar-path","");
        theSettingsLoader = new SettingsLoader(globalSettings);
    }

    /**
     * Lets us check if something is set.
     * @param paramName
     * @return
     */
    public static boolean isStringParamSet(String paramName) {
        if (!globalSettings.isParamSet(paramName)) {
            return false;
        }
        String theValue = globalSettings.getStringParam(paramName);
        if (theValue == null || theValue.equals("")) {
            return false;
        }
        return true;
    }

    public static Boolean getBooleanSetting(String paramName) {
        assert (globalSettings.isParamSet(paramName));
        Boolean theValue = globalSettings.getBooleanParam(paramName);
        if (theValue == null) {
            theValue = theSettingsLoader.getBooleanFromUser(paramName);
            globalSettings.setBooleanParam(paramName, theValue);
        }
        return theValue;
    }

    public static void overrideStringSetting(String paramName, String paramValue) {
        theSettingsLoader.overrideStringParameter(paramName, paramValue);
    }
    public static void overrideBooleanParameter(String paramName, boolean paramValue) {
        theSettingsLoader.overrideBooleanParameter(paramName, paramValue);
    }

    public static int getIntSetting(String paramName) {
        assert globalSettings.isParamSet(paramName) : "Tried to get string param for: " + paramName + " and it hasn't been defined yet.";
        Integer theValue = globalSettings.getIntegerParam(paramName);
        if (theValue == null) {
            System.err.println("ACK need to write code to get int from user");
            System.exit(1);
        }
        return theValue;
    }

    public static String getStringSetting(String paramName) {
        assert globalSettings.isParamSet(paramName) : "Tried to get string param for: " + paramName + " and it hasn't been defined yet.";
        String theValue = globalSettings.getStringParam(paramName);
        if (theValue == null || theValue.equals("")) {
            theValue = theSettingsLoader.getStringFromUser(paramName);
            globalSettings.setStringParam(paramName, theValue);
        }
        return theValue;
    }


    /**
     * Introducing this specialized initializer because we want to for SURE know
     * the nodetype when we determine this processname, which we need immediately 
     * to setup all the log settings.  So, it's a little kludgy, but all main([]) 
     * methods should basically pass a nodetype in when they call initialize.
     * @param args
     * @param nodetype
     */
    public static void initializeSettings(String[] args) {
        theSettingsLoader.setCommandlineParameters(args);
        theSettingsLoader.updateSettings();
    }

    public static void addNewParameters(ParameterHolder P) {
        theSettingsLoader.appendParameters(P);
        theSettingsLoader.updateSettings();
    }

}
