/*
Copyright 2008 Matt Radkie
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

/**
 * @author Matt Radkie
 */
package rlVizLib.utilities;

/**
 * @deprecated Use The Task Spec from the RLGlue Java Codec.
 * @author btanner
 */
public class TaskSpec {

    TaskSpecDelegate theTSO = null;
    int TSVersion = 0;

    
    public int getVersion(){
        return TSVersion;
    }

@SuppressWarnings("deprecation")
    public TaskSpec(String taskSpec) {
        String errorAccumulator="Task Spec Parse Results:";
        try {
            theTSO = new TaskSpecV3(taskSpec);
            TSVersion = 3;
        } catch (Exception e) {
            errorAccumulator+="\nParsing as V3: "+e.toString();
        }

        if (theTSO == null) {
            try {
                theTSO = new TaskSpecV2(taskSpec);
                TSVersion = 2;
            } catch (Exception e) {
            errorAccumulator+="\nParsing as V2: "+e.toString();
            }
        }
        
        if (theTSO == null) {
            System.err.println("Task spec couldn't be parsed");
            throw new IllegalArgumentException(errorAccumulator);
        }

    }


    @Override
    public String toString() {
        return theTSO.getStringRepresentation();
    }

    public String dump() {
        return theTSO.dump();
    }

    //check if obs_min[index] is negative infinity
    public boolean isObsMinNegInfinity(int index) {
        return theTSO.isObsMinNegInfinity(index);
    }
    //check if action_min[index] is negative infinity
    public boolean isActionMinNegInfinity(int index) {
        return theTSO.isActionMinNegInfinity(index);
    }
    //check if obs_max[index] is positive infinity
    public boolean isObsMaxPosInfinity(int index) {
        return theTSO.isObsMaxPosInfinity(index);
    }
    //check if action_max[index] is positive infinity
    public boolean isActionMaxPosInfinity(int index) {
        return theTSO.isActionMaxPosInfinity(index);
    }
    //check if the value range for observation[index] is known
    public boolean isObsMinUnknown(int index) {
        return theTSO.isObsMinUnknown(index);
    }

    public boolean isObsMaxUnknown(int index) {
        return theTSO.isObsMaxUnknown(index);
    }
    //check if the value range for action[index] is known
    public boolean isActionMinUnknown(int index) {
        return theTSO.isActionMinUnknown(index);
    }

    public boolean isActionMaxUnknown(int index) {
        return theTSO.isActionMaxUnknown(index);
    }

    public boolean isMinRewardNegInf() {
        return theTSO.isMinRewardNegInf();
    }

    public boolean isMaxRewardInf() {
        return theTSO.isMaxRewardInf();
    }

    public boolean isMinRewardUnknown() {
        return theTSO.isMinRewardUnknown();
    }

    public boolean isMaxRewardUnknown() {
        return theTSO.isMaxRewardUnknown();
    }
    public double getTaskSpecVersion() {
        return theTSO.getVersion();
    }

    public void setVersion(int version) {
        theTSO.setVersion(version);
    }

    public char getEpisodic() {
        return theTSO.getEpisodic();
    }

    public void setEpisodic(char episodic) {
        theTSO.setEpisodic(episodic);
    }

    public int getObsDim() {
        return theTSO.getObsDim();
    }

    public void setobsDim(int dim) {
        theTSO.setObsDim(dim);
    }

    public int getNumDiscreteObsDims() {
        return theTSO.getNumDiscreteObsDims();
    }

    public void setNumDiscreteObsDims(int numDisc) {
        theTSO.setNumDiscreteObsDims(numDisc);
    }

    public int getNumContinuousObsDims() {
        return theTSO.getNumContinuousObsDims();
    }

    public void setNumContinuousObsDims(int numCont) {
        theTSO.setNumContinuousObsDims(numCont);
    }

    public char[] getObsTypes() {
        return theTSO.getObsTypes();
    }

    public void setObsTypes(char[] types) {
        theTSO.setObsTypes(types);
    }

    public double[] getObsMins() {
        return theTSO.getObsMins();
    }

    public void setObsMins(double[] mins) {
        theTSO.setObsMins(mins);
    }

    public double[] getObsMaxs() {
        return theTSO.getObsMaxs();
    }

    public void setObsMaxs(double[] maxs) {
        theTSO.setObsMaxs(maxs);
    }
    public int getActionDim() {
        return theTSO.getActionDim();
    }

    public void setActionDim(int dim) {
        theTSO.setActionDim(dim);
    }

    public int getNumDiscreteActionDims() {
        return theTSO.getNumDiscreteActionDims();
    }

    public void setNumDiscreteActionDims(int numDisc) {
        theTSO.setNumDiscreteActionDims(numDisc);
    }

    public int getNumContinuousActionDims() {
        return theTSO.getNumContinuousActionDims();
    }

    public void setNumContinuousActionDims(int numCont) {
        theTSO.setNumContinuousActionDims(numCont);
    }

    public char[] getActionTypes() {
        return theTSO.getActionTypes();
    }

    public void setActionTypes(char[] types) {
        theTSO.setActionTypes(types);
    }

    public double[] getActionMins() {
        return theTSO.getActionMins();
    }

    public void setActionMins(double[] mins) {
        theTSO.setActionMins(mins);
    }

    public double[] getActionMaxs() {
        return theTSO.getActionMaxs();
    }

    public void setActionMaxs(double[] maxs) {
        theTSO.setActionMaxs(maxs);
    }
    public double getRewardMax() {
        return theTSO.getRewardMax();
    }

    public void setRewardMax(double max) {
        theTSO.setRewardMax(max);
    }

    public double getRewardMin() {
        return theTSO.getRewardMin();
    }

    public void setRewardMin(double min) {
        theTSO.setRewardMin(min);
    }

    public String getExtraString() {
        return theTSO.getExtraString();
    }

    public void setExtraString(String newString) {
        theTSO.setExtraString(newString);
    }

    public int getParserVersion() {
        return theTSO.getParserVersion();
    }
    
    public static void main(String[] args){
        String sampleTS="2:e:2_[f,f]_[-1.2,0.6]_[-0.07,0.07]:1_[i]_[0,2]";
        TaskSpec theTSO=new TaskSpec(sampleTS);
//        System.out.println(sampleTS+" is version: "+theTSO.getVersion());
//
//    
//        sampleTS="2:e:2_[f,f]_[-1.2,0.6]_[-0.07,0.07]:1_[i]_[0,2]:[]";
//        theTSO=new TaskSpec(sampleTS);
//        System.out.println(sampleTS+" is version: "+theTSO.getVersion());
//
//        sampleTS="2:e:2_[f,f]_[-1.2,0.6]_[-0.07,0.07]:1_[i]_[0,2]:[0,3]";
//        theTSO=new TaskSpec(sampleTS);
//        System.out.println(sampleTS+" is version: "+theTSO.getVersion());

        sampleTS="2:e:2_[f,f]_[-1.2,0.6]_[-0.07,0.07]:1_[i]_[0,2]:[0,3]:Extra strings and stuff here";
        theTSO=new TaskSpec(sampleTS);
        System.out.println(sampleTS+" is version: "+theTSO.getVersion() +"\n" +theTSO.toString());
        System.out.println(theTSO.dump());

//        sampleTS="2:e:2_[f,f]_[-1.2,0.6]_[-0.07,0.07]:1_[i]_[0,2]:[0,3]:";
//        theTSO=new TaskSpec(sampleTS);
//        System.out.println(sampleTS+" is version: "+theTSO.getVersion());
//
//        sampleTS="2:e:[0,3]";
//        theTSO=new TaskSpec(sampleTS);
//        System.out.println(sampleTS+" is version: "+theTSO.getVersion());
}
}
