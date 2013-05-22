/**
 * Copyright 2009 Brian Tanner
 * brian@tannerpages.com
 * http://research.tannerpages.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package org.rlcommunity.rlviz.dynamicloading;

public enum EnvOrAgentType {

    kEnv(0),
    kAgent(1),
    kBoth(2),
    kViz(3);
    private final int id;

    EnvOrAgentType(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }
}