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



package rlVizLib.dynamicLoading;


/**
 * Implement this if you don't want your agent or environment to be found by the 
 * crawlers that look for agents and envs.
 *
 * This technically should have moved to org.rlcommunity.rlviz.dynamicloading,
 * but... many environments and agents from the competition depend on this, so
 * want to transition quietly.
 * @author btanner
 */
public interface Unloadable extends org.rlcommunity.rlviz.dynamicloading.Unloadable{

}
