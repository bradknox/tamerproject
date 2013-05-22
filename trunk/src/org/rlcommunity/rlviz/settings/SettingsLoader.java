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
package org.rlcommunity.rlviz.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import rlVizLib.general.ParameterHolder;

/**
 * This class will look in three places for the values of settings:
 * <ul><li> Command line arguments
 * <li> Specified settings file
 * <li> Console input</ul>
 * <p>In that order.  If a settings is <b>absolutely required</b>, then it will come down
 * to a console input question if it has not been otherwise specified.  This will
 * all happen on demand (for better or worse).  Various components will have the opportunity to
 * register their parameters in the global set.  After new parameters are registered, the command-line
 * arguments and settings files will be re-parsed.
 * <p>I'm not totally sure how this will pan out but I think it's a good plan.
 * @author Brian Tanner
 */
public class SettingsLoader {
    private String[] commandLineParams = null;
    private final ParameterHolder globalParams;
    private final ParameterHolder overrideParams = new ParameterHolder();
    private boolean settingsFileIsNotValid = false;

    public SettingsLoader(ParameterHolder globalParams) {
        this.globalParams = globalParams;
    }

    public void appendParameters(ParameterHolder newParams) {
        addNewParameters(newParams, globalParams);
    }

    public void overrideStringParameter(String name, String value) {
        if (overrideParams.isParamSet(name)) {
            overrideParams.setStringParam(name, value);
        } else {
            overrideParams.addStringParam(name, value);
        }
        updateSettings();
    }
    public void overrideBooleanParameter(String name, boolean value) {
        if (overrideParams.isParamSet(name)) {
            overrideParams.setBooleanParam(name, value);
        } else {
            overrideParams.addBooleanParam(name, value);
        }
        updateSettings();
    }

    /**
     * Add all of the parameters, aliases, and values from dest to the global parameter
     * set.  In case a parameter already exists, it's current value will NOT be 
     * overwritten by the values in dest. That way modules are encouraged to register
     * all the parameters they will need to use.  This merge will
     * not preserve aliases.
     * @param dest
     */
    private void addNewParameters(ParameterHolder newParams, ParameterHolder dest) {
        if (newParams == null) {
            return;
        }
        int numParams = newParams.getParamCount();

        for (int i = 0; i < numParams; i++) {
            String thisPName = newParams.getParamName(i);
            int thisPType = newParams.getParamType(i);

            //Check if this parameter already exists, if so we don't want to overwrite the value
            if (dest.isParamSet(thisPName)) {
                int globalType = dest.getParamTypeByName(thisPName);
                assert globalType == thisPType : "Tried to add new parameter: " + thisPName + " which didn't jive with existing parameter in type";
            } else {

                if (thisPType == ParameterHolder.boolParam) {
                    dest.addBooleanParam(thisPName, newParams.getBooleanParam(thisPName));
                }
                if (thisPType == ParameterHolder.intParam) {
                    dest.addIntegerParam(thisPName, newParams.getIntegerParam(thisPName));
                }
                if (thisPType == ParameterHolder.doubleParam) {
                    dest.addDoubleParam(thisPName, newParams.getDoubleParam(thisPName));
                }
                if (thisPType == ParameterHolder.stringParam) {
                    dest.addStringParam(thisPName, newParams.getStringParam(thisPName));
                }
            }
        }

    }

    /**
     * This is horrible right now because we're doing the override each time.  This was
     * necessary so that if the settingsfile was overridden we'd actually have it.
     */
    public void updateSettings() {
        //Override any commandline or file based settings with forced override
        copyParameterValues(overrideParams, globalParams);
        try {
            parseCommandLineParams();
        } catch (Throwable t) {
            System.out.println(t);
            printUsage();
            System.exit(1);
        }
//Override any commandline or file based settings with forced override
        copyParameterValues(overrideParams, globalParams);
        assert (globalParams.isParamSet("settingsfile"));
        String settingsFileDirectory = globalParams.getStringParam("settingsfile");
        if (settingsFileDirectory != null && !settingsFileDirectory.equals("")) {
            parseParametersFromFile(settingsFileDirectory);
        }
//Override any commandline or file based settings with forced override
        copyParameterValues(overrideParams, globalParams);
    }

    Boolean getBooleanFromUser(String paramName) {
        System.out.print("Please enter a value for " + paramName + " [y/n] :");
        String theValue = ConsoleReader.readLine();
        if (theValue.toLowerCase().startsWith("y")) {
            return true;
        }

        if (theValue.toLowerCase().startsWith("n")) {
            return false;
        }

        System.out.println(theValue + " is not a valid response");
        return getBooleanFromUser(paramName);
    }
   
    String getStringFromUser(String paramName) {
        Thread.dumpStack();
        System.out.print("Please enter a value for " + paramName + ":");
        String theValue = ConsoleReader.readLine();
        return theValue;
    }

    private void copyParameterValues(ParameterHolder source, ParameterHolder dest) {
        int numParams = source.getParamCount();

        for (int i = 0; i <
                numParams; i++) {
            String thisPName = source.getParamName(i);
            int thisPType = source.getParamType(i);

            //Check if this parameter already exists
            if (dest.isParamSet(thisPName)) {
                int globalType = dest.getParamTypeByName(thisPName);
                assert globalType == thisPType : "Tried to add new parameter: " + thisPName + " which didn't jive with existing parmeter in type";
                if (thisPType == ParameterHolder.boolParam) {
                    Boolean thisValue = source.getBooleanParam(thisPName);
                    if (thisValue != null) {
                        dest.setBooleanParam(thisPName, thisValue);
                    }

                }
                if (thisPType == ParameterHolder.intParam) {
                    Integer thisValue = source.getIntegerParam(thisPName);
                    if (thisValue != null) {
                        dest.setIntegerParam(thisPName, thisValue);
                    }

                }
                if (thisPType == ParameterHolder.doubleParam) {
                    Double thisValue = source.getDoubleParam(thisPName);
                    if (thisValue != null) {
                        dest.setDoubleParam(thisPName, thisValue);
                    }

                }
                if (thisPType == ParameterHolder.stringParam) {
                    String thisValue = source.getStringParam(thisPName);
                    if (thisValue != null) {
                        dest.setStringParam(thisPName, thisValue);
                    }

                }
            }
        }

    }

    private void processOneParameter(String theParamPair) {
        StringTokenizer tok = new StringTokenizer(theParamPair, "=");
        String paramName = tok.nextToken();
        String value = tok.nextToken();

        if (globalParams.isParamSet(paramName)) {
            //If the param is defined, set it by magic
            globalParams.setParamByMagicFromString(paramName, value);
        }

    }

    private void parseCommandLineParams() {
        assert commandLineParams != null : "parseCommandLineParams was called but no command line parameters have been registered";
        try {
            for (String theParamPair : commandLineParams) {
                processOneParameter(theParamPair);
            }

        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException();
        }

    }

    private void parseParametersFromFile(String settingsFilePath) {
        if (settingsFileIsNotValid) {
            return;
        }

        File settingsFile = new File(settingsFilePath);
        if (!settingsFile.exists()) {
            System.err.println("You gave a path to a settings file: " + settingsFilePath + " but there was no file there");
            settingsFileIsNotValid = true;
            return;
        }

        try {
            BufferedReader R = new BufferedReader(new FileReader(settingsFile));
            try {
                while (R.ready()) {
                    String thisLine = R.readLine().trim();
                    if(!(thisLine.startsWith("#")||thisLine.equals(""))){
                        processOneParameter(thisLine);
                    }
                }
                R.close();
            } catch (IOException ex) {
                System.err.println("Error trying to read settings file"+ ex);
            }
        } catch (FileNotFoundException ex) {
           System.err.println("Error trying to open settings file"+ ex);
        }
    }

    private static void printUsage() {
        StringBuilder SB = new StringBuilder("**************************");
        SB.append("\nrl-viz Application");
        SB.append("\n--------------------------");
        SB.append("\nUsage:Update this later!");

        System.out.println(SB.toString());
    }

    void setCommandlineParameters(String[] args) {
        this.commandLineParams = args;
    }

    public static void main(String[] args) {
        ParameterHolder P = new ParameterHolder();
        P.addStringParam("test");
        System.out.println(P.getStringParam("test"));
    }
}
