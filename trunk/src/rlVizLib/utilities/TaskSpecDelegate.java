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


package rlVizLib.utilities;

/**
 * Here's the trick.  We'll write this class to be the go-to class, that the
 * task spec object will talk to.  We'll extend this class over time, adding more 
 * stuff to it, but we'll be careful such that we don't need to *ever* change
 * existing subclasses.  Lets pretend that's what I mean.  
 * @deprecated We're now goign to use task spec stuff from the RLGlue Java Codec.
 * @author mradkie
 */
public abstract class TaskSpecDelegate {

    String dump() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    String getStringRepresentation() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    
    	//check if obs_min[index] is negative infinity
	public boolean isObsMinNegInfinity(int index)
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isObsMinNegInfinity");
	}
	//check if action_min[index] is negative infinity
	public boolean isActionMinNegInfinity(int index)
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isActionMinNegInfinity");
	}
	//check if obs_max[index] is positive infinity
	public boolean isObsMaxPosInfinity(int index)
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isObsMaxPosInfinity");
	}
	//check if action_max[index] is positive infinity
	public boolean isActionMaxPosInfinity(int index)
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isActionMaxPosInfinity");
	}
	//check if the value range for observation[index] is known
	public boolean isObsMinUnknown(int index)
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isObsMinUnknown");
	}
	public boolean isObsMaxUnknown(int index)
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isObsMaxUnknown");
	}
	//check if the value range for action[index] is known
	public boolean isActionMinUnknown(int index)
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isActionMinUnknown");
	}
	public boolean isActionMaxUnknown(int index)
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isActionMaxUnknown");
	}
	public boolean isMinRewardNegInf()
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isMinRewardNegInf");		
	}
	public boolean isMaxRewardInf()
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isMaxRewardInf");
	}
	public boolean isMinRewardUnknown()
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isMinRewardUnknown");
	}
	public boolean isMaxRewardUnknown()
	{
            throw new NoSuchMethodError("This version of the task spec does not support: isMaxRewardUnknown");
	}
	/*
        * public double version;
        * public char episodic;
        * public int obs_dim;
        * public int num_discrete_obs_dims;
        * public int num_continuous_obs_dims;
        * public char[] obs_types;
        * public double[] obs_mins;
        * public double[] obs_maxs;
         * */
        public double getVersion(){
            throw new NoSuchMethodError("This version of the task spec does not support: getVersion");
        }
        public void setVersion(int version){
            throw new NoSuchMethodError("This version of the task spec does not support: setVersion");
        }
        public char getEpisodic(){
            throw new NoSuchMethodError("This version of the task spec does not support: getEpisodic");
        }
        public void setEpisodic(char episodic){
            throw new NoSuchMethodError("This version of the task spec does not support: setEpisodic");
        }
        public int getObsDim(){
            throw new NoSuchMethodError("This version of the task spec does not support: getobsDim");
        }
        public void setObsDim(int dim){
            throw new NoSuchMethodError("This version of the task spec does not support: setobsDim");
        }
        public int getNumDiscreteObsDims(){
            throw new NoSuchMethodError("This version of the task spec does not support: getNumDiscreteObsDims");
        }
        public void setNumDiscreteObsDims(int numDisc){
            throw new NoSuchMethodError("This version of the task spec does not support: setNumDiscreteObsDims");
        }
        public int getNumContinuousObsDims(){
            throw new NoSuchMethodError("This version of the task spec does not support: getNumContinuousActionDims");
        }
        public void setNumContinuousObsDims(int numCont){
            throw new NoSuchMethodError("This version of the task spec does not support: setNumContinuousActionDims");
        }
        public char[] getObsTypes(){
            throw new NoSuchMethodError("This version of the task spec does not support: getObsTypes");
        }
        public void setObsTypes(char[] types){
            throw new NoSuchMethodError("This version of the task spec does not support: setObsTypes");
        }
        public double[] getObsMins(){
            throw new NoSuchMethodError("This version of the task spec does not support: getObsMins");
        }
        public void setObsMins(double[] mins){
            throw new NoSuchMethodError("This version of the task spec does not support: setObsMins");
        }
        public double[] getObsMaxs(){
            throw new NoSuchMethodError("This version of the task spec does not support: getObsMaxs");
        }
        public void setObsMaxs(double[] maxs){
            throw new NoSuchMethodError("This version of the task spec does not support: setObsMaxs");
        }
        /*
        * public int action_dim;
        * public int num_discrete_action_dims;
        * public int num_continuous_action_dims;
        * public char[] action_types;
        * public double[] action_mins;
        * public double[] action_maxs;
        */
        public int getActionDim(){
            throw new NoSuchMethodError("This version of the task spec does not support: getActionDim");
        }
        public void setActionDim(int dim){
            throw new NoSuchMethodError("This version of the task spec does not support: setActionDim");
        }
        public int getNumDiscreteActionDims(){
            throw new NoSuchMethodError("This version of the task spec does not support: getNumDiscreteActionDims");
        }
        public void setNumDiscreteActionDims(int numDisc){
            throw new NoSuchMethodError("This version of the task spec does not support: setNumDiscreteActionDims");
        }
        public int getNumContinuousActionDims(){
            throw new NoSuchMethodError("This version of the task spec does not support: getNumContinuousActionDims");
        }
        public void setNumContinuousActionDims(int numCont){
            throw new NoSuchMethodError("This version of the task spec does not support: setNumContinuousActionDims");
        }
        public char[] getActionTypes(){
            throw new NoSuchMethodError("This version of the task spec does not support: getActionTypes");
        }
        public void setActionTypes(char[] types){
            throw new NoSuchMethodError("This version of the task spec does not support: setActionTypes");
        }
        public double[] getActionMins(){
            throw new NoSuchMethodError("This version of the task spec does not support: getActionMins");
        }
        public void setActionMins(double[] mins){
            throw new NoSuchMethodError("This version of the task spec does not support: setActionMins");
        }
        public double[] getActionMaxs(){
            throw new NoSuchMethodError("This version of the task spec does not support: getActionMaxs");
        }
        public void setActionMaxs(double[] maxs){
            throw new NoSuchMethodError("This version of the task spec does not support: setActionMaxs");
        }
        /*
        * public double reward_max;
        * public double reward_min;
        * public String extraString;
        * static final int parser_version = 3;
        */
        public double getRewardMax(){
            throw new NoSuchMethodError("This version of the task spec does not support: getRewardMax");
        }
        public void setRewardMax(double max){
            throw new NoSuchMethodError("This version of the task spec does not support: setRewardMax");
        }
        public double getRewardMin(){
            throw new NoSuchMethodError("This version of the task spec does not support: getRewardMin");
        }
        public void setRewardMin(double min){
            throw new NoSuchMethodError("This version of the task spec does not support: getRewardMin");
        }
        public String getExtraString(){
            throw new NoSuchMethodError("This version of the task spec does not support: getExtraString");
        }
        public void setExtraString(String newString){
            throw new NoSuchMethodError("This version of the task spec does not support: setExtraString");
        }
        public int getParserVersion(){
            throw new NoSuchMethodError("This version of the task spec does not support: getParserVersion");
        }
}
