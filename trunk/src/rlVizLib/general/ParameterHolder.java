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
package rlVizLib.general;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

public class ParameterHolder {

    public static final int intParam = 0;
    public static final int doubleParam = 1;
    public static final int boolParam = 2;
    public static final int stringParam = 3;    //This a straight port of my bt-glue C++ Code, might simplify in Java
    Map<String, Integer> intParams = new TreeMap<String, Integer>();
    Map<String, Double> doubleParams = new TreeMap<String, Double>();
    Map<String, String> stringParams = new TreeMap<String, String>();
    Map<String, Boolean> boolParams = new TreeMap<String, Boolean>();
    Map<String, Integer> allParams = new TreeMap<String, Integer>();
    Map<String, String> aliases = new TreeMap<String, String>();
    Vector<Integer> allParamTypes = new Vector<Integer>();
    Vector<String> allParamNames = new Vector<String>();
    Vector<String> allAliases = new Vector<String>();

    public static void main(String[] args) {
        ParameterHolder P = new ParameterHolder();
        P.addIntegerParam("sampleIntParam", 5);
        P.addDoubleParam("sampleDoubleParam", 2.1);
        P.addBooleanParam("sampleBoolParam", true);
        P.addStringParam("sampleStringParam", "thetest");
        P.setAlias("intParam", "sampleIntParam");

        String parameterString = P.stringSerialize();
        String cppString = "PARAMHOLDER_4_sampleIntParam_0_5_sampleDoubleParam_1_2.1_sampleBoolParam_2_true_sampleStringParam_3_thetest_5_sampleIntParam_sampleIntParam_sampleDoubleParam_sampleDoubleParam_sampleBoolParam_sampleBoolParam_sampleStringParam_sampleStringParam_intParam_sampleIntParam_";
        if (parameterString.equals(cppString)) {
            System.out.println("The strings are the same");
        }
        System.out.println("The string is:\n" + parameterString);

    }

    public ParameterHolder() {
    }

    public boolean isParamSet(String theAlias) {
        if (!aliases.containsKey(theAlias)) {
            return false;
        }
        String name = getAlias(theAlias);
        return allParams.containsKey(name);
    }

    public boolean isNull() {
        return (allParams.size() == 0);
    }

    public ParameterHolder(final String theString) {
        this();
        StringTokenizer iss = new StringTokenizer(theString, "_");

        int numParams;
        String thisParamName;

        int thisParamType;

        //Make sure the first bit of this isn't NULL!
        if (iss.nextToken().equals("NULL")) {
            return;
        }
        numParams = Integer.parseInt(iss.nextToken());

        for (int i = 0; i < numParams; i++) {
            thisParamName = iss.nextToken();
            thisParamType = Integer.parseInt(iss.nextToken());
            String thisReadValue=iss.nextToken();

            if (thisParamType == intParam) {
                Integer thisParamValue=null;
                if(thisReadValue!=null){
                    thisParamValue = Integer.parseInt(thisReadValue);
                }
                addIntegerParam(thisParamName, thisParamValue);
                
            }
            if (thisParamType == doubleParam) {
                Double thisParamValue=null;
                if(thisReadValue!=null){
                 thisParamValue = Double.parseDouble(thisReadValue);
                }
                addDoubleParam(thisParamName, thisParamValue);
            }
            if (thisParamType == boolParam) {
                Boolean thisParamValue=null;
                if(thisReadValue!=null){
                thisParamValue = Boolean.parseBoolean(thisReadValue);
                }addBooleanParam(thisParamName, thisParamValue);
            }
            if (thisParamType == stringParam) {
                addStringParam(thisParamName,thisReadValue);
            }
        }

        //Alias time
        int numAliases;
        numAliases = Integer.parseInt(iss.nextToken());


        for (int i = 0; i < numAliases; i++) {
            String thisAlias = iss.nextToken();
            String thisTarget = iss.nextToken();
            setAlias(thisAlias, thisTarget);
        }


    }

    public void setAlias(String thisAlias, String thisTarget) {
        if (thisAlias.contains(":") || thisAlias.contains("_")) {
            System.err.println("The name or alias of a parameter cannot contain a space or : or _");
            Thread.dumpStack();
            System.exit(1);
        }

        if (allParams.get(thisTarget) == null) {
            System.err.println("Careful, you are setting an alias of: " + thisAlias + " to original: " + thisTarget + " but the original isn't in the parameter set!");
            Thread.dumpStack();
            System.exit(1);
        }
        aliases.put(thisAlias, thisTarget);
        allAliases.add(thisAlias);
    }

    public void addStringParam(String thisParamName, String thisParamValue) {
        if (thisParamName.contains(":") || thisParamName.contains("_")) {
            System.err.println("The ParameterName " + thisParamName + " with Parameter Value " + thisParamValue + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        addStringParam(thisParamName);
        setStringParam(thisParamName, thisParamValue);
    }

    public void addStringParam(String thisParamName) {
        if (thisParamName.contains(":") || thisParamName.contains("_")) {
            System.out.println("The ParameterName " + thisParamName + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        allParams.put(thisParamName, stringParam);
        allParamTypes.add(stringParam);
        genericNewParam(thisParamName);
        setStringParam(thisParamName, null);
    }

    public void setStringParam(String thisParamAlias, String thisParamValue) {
        String name = getAlias(thisParamAlias);
        if (!allParams.containsKey(name)) {
            System.err.println("Careful, you are setting the value of parameter: " + name + " but the parameter hasn't been added...");
        }
        if (thisParamValue != null) {
            thisParamValue = thisParamValue.replace(":", "!!COLON!!");
            thisParamValue = thisParamValue.replace("_", "!!UNDERSCORE!!");
        }

        stringParams.put(name, thisParamValue);

    }

    private void genericNewParam(String thisParamName) {
        if (thisParamName.contains(":") || thisParamName.contains("_")) {
            System.out.println("The ParameterName " + thisParamName + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        allParamNames.add(thisParamName);
        setAlias(thisParamName, thisParamName);
    }

    public void addIntegerParam(String thisParamName) {
        if (thisParamName.contains(":") || thisParamName.contains("_")) {
            System.out.println("The ParameterName " + thisParamName + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        allParams.put(thisParamName, intParam);
        allParamTypes.add(intParam);
        genericNewParam(thisParamName);
        setIntegerParam(thisParamName, null);
    }

    public void addDoubleParam(String thisParamName) {
        if (thisParamName.contains(":") || thisParamName.contains("_")) {
            System.out.println("The ParameterName " + thisParamName + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        allParams.put(thisParamName, doubleParam);
        allParamTypes.add(doubleParam);
        genericNewParam(thisParamName);
        setDoubleParam(thisParamName, null);
    }

    public void addDoubleParam(String thisParamName, Double thisParamValue) {
        if (thisParamName.contains(":") || thisParamName.contains("_")) {
            System.out.println("The ParameterName " + thisParamName + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        addDoubleParam(thisParamName);
        setDoubleParam(thisParamName, thisParamValue);
    }

    public void setDoubleParam(String thisParamAlias, Double thisParamValue) {
        if (thisParamAlias.contains(":") || thisParamAlias.contains("_")) {
            System.out.println("The ParameterAlias " + thisParamAlias + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        String name = getAlias(thisParamAlias);
        if (!allParams.containsKey(name)) {
            System.err.println("Careful, you are setting the value of parameter: " + name + " but the parameter hasn't been added...");
        }
        doubleParams.put(name, thisParamValue);
    }

    public void addBooleanParam(String thisParamName) {

        if (thisParamName.contains(":") || thisParamName.contains("_")) {
            System.out.println("The ParameterName " + thisParamName + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        allParams.put(thisParamName, boolParam);
        allParamTypes.add(boolParam);
        genericNewParam(thisParamName);
        setBooleanParam(thisParamName, null);
    }

    public void addBooleanParam(String thisParamName, Boolean thisParamValue) {
        if (thisParamName.contains(":") || thisParamName.contains("_")) {
            System.out.println("The ParameterName " + thisParamName + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        addBooleanParam(thisParamName);
        setBooleanParam(thisParamName, thisParamValue);
    }

    public void setBooleanParam(String thisParamAlias, Boolean thisParamValue) {
        if (thisParamAlias.contains(":") || thisParamAlias.contains("_")) {
            System.out.println("The ParameterAlias " + thisParamAlias + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        String name = getAlias(thisParamAlias);
        if (!allParams.containsKey(name)) {
            System.err.println("Careful, you are setting the value of parameter: " + name + " but the parameter hasn't been added...");
        }
        boolParams.put(name, thisParamValue);
    }

    public void addIntegerParam(String thisParamName, Integer thisParamValue) {
        if (thisParamName.contains(":") || thisParamName.contains("_")) {
            System.out.println("The ParameterName " + thisParamName + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        addIntegerParam(thisParamName);
        setIntegerParam(thisParamName, thisParamValue);
    }

    public void setIntegerParam(String thisParamAlias, Integer thisParamValue) {
        if (thisParamAlias.contains(":") || thisParamAlias.contains("_")) {
            System.out.println("The ParameterAlias " + thisParamAlias + " cannot contain a : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        String name = getAlias(thisParamAlias);
        if (!allParams.containsKey(name)) {
            System.err.println("Careful, you are setting the value of parameter: " + name + " but the parameter hasn't been added...");
        }
        intParams.put(name, thisParamValue);
    }

    private String getAlias(String thisParamAlias) {
        if (thisParamAlias.contains(":") || thisParamAlias.contains("_")) {
            System.out.println("The ParameterAlias " + thisParamAlias + " cannot contain a  : or _");
            Thread.dumpStack();
            System.exit(1);
        }
        if (!aliases.containsKey(thisParamAlias)) {
            System.err.println("You wanted to look up original for alias: " + thisParamAlias + ", but that alias hasn't been set");
            Thread.dumpStack();
            System.exit(-1);
        }
        return aliases.get(thisParamAlias);

    }

    public String stringSerialize() {
        StringBuffer outs = new StringBuffer();

        //Do this here instead of externally later when we're ready
        outs.append("PARAMHOLDER_");
        //First, write the number of param names
        outs.append(allParamNames.size());
        outs.append("_");

        for (int i = 0; i < allParamNames.size(); i++) {
            outs.append(allParamNames.get(i));
            outs.append("_");

            int paramType = allParamTypes.get(i);

            outs.append(paramType);
            outs.append("_");

            if (paramType == intParam) {
                Integer theValue = getIntegerParam(allParamNames.get(i));
                if (theValue == null) {
                    outs.append("!!NULL!!");
                } else {
                    outs.append(theValue);
                }
            }
            if (paramType == doubleParam) {
                Double theValue = getDoubleParam(allParamNames.get(i));
                if (theValue == null) {
                    outs.append("!!NULL!!");
                } else {
                    outs.append(theValue);
                }
            }
            if (paramType == boolParam) {
                Boolean theValue = getBooleanParam(allParamNames.get(i));
                if (theValue == null) {
                    outs.append("!!NULL!!");
                } else {
                    outs.append(theValue);
                }
            }
            if (paramType == stringParam) {
                String theValue=getStringParamEncoded(allParamNames.get(i));
                if (theValue == null) {
                    outs.append("!!NULL!!");
                } else {
                    outs.append(theValue);
                }
            }
            outs.append("_");
        }

        //Now write all of the aliases
        outs.append(allAliases.size());
        outs.append("_");

        for (int i = 0; i < allAliases.size(); i++) {
            outs.append(allAliases.get(i));
            outs.append("_");
            outs.append(getAlias(allAliases.get(i)));
            outs.append("_");
        }

        return outs.toString();
    }

    private String getStringParamEncoded(String theAlias) {
        //Convert from an alias to the real name
        String name = getAlias(theAlias);

        if (!allParams.containsKey(name)) {
            System.out.println("Careful, you are getting the value of parameter: " + name + " but the parameter hasn't been added...");
            System.exit(1);
        }
        if (!stringParams.containsKey(name)) {
            System.out.println("Careful, you are getting the value of parameter: " + name + " but the parameter isn't a String parameter...");
            System.exit(1);
        }

        return stringParams.get(name);
    }

    public String getStringParam(String theAlias) {

        String encodedVersion = getStringParamEncoded(theAlias);
        String fixedVersion=encodedVersion;
        if(encodedVersion!=null){
            fixedVersion = encodedVersion.replace("!!COLON!!", ":");
            fixedVersion = fixedVersion.replace("!!UNDERSCORE!!", "_");
        }
        return fixedVersion;
    }

    public double getDoubleParam(String theAlias) {
        //Convert from an alias to the real name
        String name = getAlias(theAlias);

        if (!allParams.containsKey(name)) {
            System.out.println("Careful, you are getting the value of parameter: " + name + " but the parameter hasn't been added...");
            System.exit(1);
        }
        if (!doubleParams.containsKey(name)) {
            System.out.println("Careful, you are getting the value of parameter: " + name + " but the parameter isn't a double parameter...");
            System.exit(1);
        }

        return doubleParams.get(name);
    }

    public boolean getBooleanParam(String theAlias) {
        //Convert from an alias to the real name
        String name = getAlias(theAlias);

        if (!allParams.containsKey(name)) {
            System.out.println("Careful, you are getting the value of parameter: " + name + " but the parameter hasn't been added...");
            System.exit(1);
        }
        if (!boolParams.containsKey(name)) {
            System.out.println("Careful, you are getting the value of parameter: " + name + " but the parameter isn't a bool parameter...");
            System.exit(1);
        }

        return boolParams.get(name);
    }

    public int getIntegerParam(String theAlias) {
        //Convert from an alias to the real name
        String name = getAlias(theAlias);

        if (!allParams.containsKey(name)) {
            System.out.println("Careful, you are getting the value of parameter: " + name + " but the parameter hasn't been added...");
            System.exit(1);
        }
        if (!intParams.containsKey(name)) {
            System.out.println("Careful, you are getting the value of parameter: " + name + " but the parameter isn't an int parameter...");
            System.exit(1);
        }

        return intParams.get(name);
    }

    public void setParamByMagicFromString(String theAlias, String theValue) {
        String name = getAlias(theAlias);
        //This doesn't quite seem right.
        if (!allParams.containsKey(name)) {
            System.out.println("Careful, you are setting the value of parameter: " + name + " but the parameter hasn't been added...");
            System.exit(1);
        }

        int thisType = getParamTypeByName(name);
        switch (thisType) {
            case intParam:
                setIntegerParam(name, Integer.parseInt(theValue));
                break;
            case doubleParam:
                setDoubleParam(name, Double.parseDouble(theValue));
                break;
            case boolParam:
                setBooleanParam(name, Boolean.parseBoolean(theValue));
                break;
            case stringParam:
                setStringParam(name, theValue);
                break;
        }
    }

    public String getParamAsString(String theAlias) {
        String name = getAlias(theAlias);
        String theReturnValue = null;
        //This doesn't quite seem right.
        if (!allParams.containsKey(name)) {
            System.out.println("Careful, you are getting the value of parameter: " + name + " but the parameter hasn't been added...");
            System.exit(1);
        }

        int thisType = getParamTypeByName(name);
        switch (thisType) {
            case intParam:
                theReturnValue = ((Integer) getIntegerParam(name)).toString();
                break;
            case doubleParam:
                theReturnValue = ((Double) getDoubleParam(name)).toString();
                break;
            case boolParam:
                theReturnValue = ((Boolean) getBooleanParam(name)).toString();
                break;
            case stringParam:
                theReturnValue = getStringParam(name);
                break;
        }
        return theReturnValue;
    }

    public int getParamTypeByName(String theAlias) {
        String name = getAlias(theAlias);
        //This doesn't quite seem right.
        if (!allParams.containsKey(name)) {
            System.out.println("Careful, you are getting the value of parameter: " + name + " but the parameter hasn't been added...");
            System.exit(1);
        }

        if (intParams.containsKey(name)) {
            return intParam;
        }
        if (doubleParams.containsKey(name)) {
            return doubleParam;
        }
        if (boolParams.containsKey(name)) {
            return boolParam;
        }
        if (stringParams.containsKey(name)) {
            return stringParam;
        }
        throw new InvalidParameterException("Couldn't figure out type of Parameter: " + theAlias);

    }

    public String toString() {
        StringBuffer theBuffer = new StringBuffer();
        for (int i = 0; i < getParamCount(); i++) {
            int thisParamType = getParamType(i);
            String thisParamName = getParamName(i);

            switch (thisParamType) {
                case ParameterHolder.boolParam:
                    theBuffer.append("boolParam: " + thisParamName + " = " + getBooleanParam(thisParamName));
                    break;
                case ParameterHolder.intParam:
                    theBuffer.append("intParam: " + thisParamName + " = " + getIntegerParam(thisParamName));
                    break;
                case ParameterHolder.doubleParam:
                    theBuffer.append("doubleParam: " + thisParamName + " = " + getDoubleParam(thisParamName));
                    break;
                case ParameterHolder.stringParam:
                    theBuffer.append("stringParam: " + thisParamName + " = " + getStringParam(thisParamName));
                    break;
            }
            theBuffer.append("\n");
        }
        return theBuffer.toString();
    }
//	int ParameterHolder::getParamCount(){
//	return allParamNames.size();
//	}
//	String ParameterHolder::getParamName(int which){
//	return allParamNames[which];
//	}
//	PHTypes ParameterHolder::getParamType(int which){
//	return allParamTypes[which];
//	}

//	bool ParameterHolder::supportsParam(String alias){
//	return (aliases.count(alias)!=0);
//	}
    public static ParameterHolder makeTestParameterHolder() {
        ParameterHolder p = new ParameterHolder();

        p.addDoubleParam("Alpha", .1);
        p.addDoubleParam("epsilon", .03);

        p.addIntegerParam("StepCount", 5);
        p.addIntegerParam("Tiles", 16);

        p.addStringParam("AgentName", "Dave");
        p.addStringParam("AgentOccupation", "Winner");

        p.addBooleanParam("ISCool", false);
        p.addBooleanParam("ISFast", true);

        return p;
    }
    /*	public static void main(String []args){
    
    ParameterHolder p=makeTestParameterHolder();
    String serializedVersion = p.stringSerialize();
    System.out.println("serialized: "+serializedVersion);
    
    ParameterHolder unpackedP=new ParameterHolder(serializedVersion);
    
    System.out.println(p.getDoubleParam("Alpha"));
    System.out.println(p.getDoubleParam("epsilon"));
    System.out.println(p.getIntegerParam("StepCount"));
    System.out.println(p.getIntegerParam("Tiles"));
    System.out.println(p.getStringParam("AgentName"));
    System.out.println(p.getStringParam("AgentOccupation"));
    System.out.println(p.getBooleanParam("ISCool"));
    System.out.println(p.getBooleanParam("ISFast"));
    
    System.out.println("---");
    
    System.out.println(unpackedP.getDoubleParam("Alpha"));
    System.out.println(unpackedP.getDoubleParam("epsilon"));
    System.out.println(unpackedP.getIntegerParam("StepCount"));
    System.out.println(unpackedP.getIntegerParam("Tiles"));
    System.out.println(unpackedP.getStringParam("AgentName"));
    System.out.println(unpackedP.getStringParam("AgentOccupation"));
    System.out.println(unpackedP.getBooleanParam("ISCool"));
    System.out.println(unpackedP.getBooleanParam("ISFast"));
    
    }*/

    public int getParamCount() {
        return allParams.size();
    }

    public int getParamType(int i) {
        return allParamTypes.get(i);
    }

    public String getParamName(int i) {
        return allParamNames.get(i);
    }
}
