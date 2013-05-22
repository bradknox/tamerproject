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

  
package rlVizLib.messaging.interfaces;

import rlVizLib.general.RLVizVersion;

/**
 * We used to hard code certain things about versions, but now we actually
 * use the manifest files to store what version of RLViz we compiled against
 * and which one we are currently using, so we can automate this stuff and no
 * longer require agents and envs to do anything.
 * @author btanner
 * @deprecated
 */
public interface RLVizVersionResponseInterface {
	public RLVizVersion getTheVersionISupport();
}
