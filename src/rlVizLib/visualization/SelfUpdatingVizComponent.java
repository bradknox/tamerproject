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

  
package rlVizLib.visualization;


/**
 * This should be renamed to PollingVizComponent or something, but that would break 
 * a lot of code.  Actually, that's ok.  I can deprecate this and make that one.
 * @author btanner
 */
public interface SelfUpdatingVizComponent extends BasicVizComponent {
        public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener);
}
