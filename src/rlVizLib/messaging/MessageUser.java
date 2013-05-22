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

  
package rlVizLib.messaging;


public enum MessageUser{
	kBenchmark(0),
	kEnvShell(1),
	kAgentShell(2),
	kEnv(3),
	kAgent(4);
	
	private final int id;
	
	MessageUser(int id){
        this.id = id;
    }
    public int id()   {return id;}
    
        public static String name(int id){
        if(id == kBenchmark.id())return "kBenchmark";
        if(id == kEnvShell.id())return "kEnvShell";
        if(id == kAgentShell.id())return "kAgentShell";
        if(id == kEnv.id())return "kEnv";
        if(id == kAgent.id())return "kAgent";
        return "Type: "+id+" is unknown MessageUser";
    }

    public static String name(MessageUser user) {
        return name(user.id());
    }
        

}
