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

package rlVizLib.visualization;

import java.awt.Graphics2D;

/**
 * Eventually it would be good to make vizcomponents that are not constantly 
 * being polled.  This is the first step, a vizcomponent that is painted when 
 * it has been notifed somehow that it should update.  This is mainly so far 
 * for components that are observers of TinyGlue.
 * @author btanner
 */
public interface BasicVizComponent {
	public void render(Graphics2D g);
}
