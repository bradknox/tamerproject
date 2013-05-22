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

  
package rlVizLib.visualization.interfaces;


public interface AgentOnValueFunctionDataProvider {
    /**
     * This should actually be STATE, not observation
     * @param whichDimension
     * @return
     */
	public double getCurrentStateInDimension(int whichDimension);
	public double getMinValueForDim(int whichDimension);
	public double getMaxValueForDim(int whichDimension);
	public void updateAgentState();
}
