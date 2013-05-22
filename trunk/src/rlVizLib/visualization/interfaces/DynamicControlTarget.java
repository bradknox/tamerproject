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

import java.awt.Component;
import java.util.Vector;

//If you implement this interface, it means that you have a JPanel and you are willing to let random people
//plop controls down onto that panel.  They will worry about listening to changes and stuff, you just have to 
//give them a spot.

//This will be useful because the visualizer may want to plot a control down, like a value function resolution slider
public interface DynamicControlTarget {
    
    //Only call this once per visualizer
    public void addControls(Vector<Component> c);
    public void removeControl(Component c);

}
