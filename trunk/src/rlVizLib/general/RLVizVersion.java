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

  
package rlVizLib.general;

import java.util.StringTokenizer;

import rlVizLib.rlVizCore;

public class RLVizVersion implements Comparable<RLVizVersion>{
	int majorRevision;
	int minorRevision;
	
	public static final RLVizVersion NOVERSION=new RLVizVersion(0,0);
	public static final RLVizVersion CURRENTVERSION;
        
        static{
            CURRENTVERSION=rlVizCore.getRLVizSpecVersion();
        }
	
	public RLVizVersion(int majorRevision, int minorRevision){
		this.majorRevision=majorRevision;
		this.minorRevision=minorRevision;
	}
	
	public RLVizVersion(String serialized){
            try {
		StringTokenizer theTokenizer=new StringTokenizer(serialized,".");
		majorRevision=Integer.parseInt(theTokenizer.nextToken());
		minorRevision=Integer.parseInt(theTokenizer.nextToken());
            } catch (Exception exception) {
                majorRevision=NOVERSION.majorRevision;
                minorRevision=NOVERSION.minorRevision;
            }

	}

	public int getMajorRevision() {
		return majorRevision;
	}

	public int getMinorRevision() {
		return minorRevision;
	}
	
	public String serialize(){
		String theString=majorRevision+"."+minorRevision;
		return theString;
	}
        
    @Override
        public String toString(){
            return ""+getMajorRevision()+"."+getMinorRevision();
        }


    @Override
    public boolean equals(Object otherObject){
        if(otherObject instanceof RLVizVersion)
            return compareTo((RLVizVersion)otherObject)==0;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.majorRevision;
        hash = 29 * hash + this.minorRevision;
        return hash;
    }

    public int compareTo(RLVizVersion otherVersion) {
		
		if(otherVersion.getMajorRevision()<getMajorRevision())
			return 1;
		if(otherVersion.getMajorRevision()>getMajorRevision())
			return -1;
		if(otherVersion.getMinorRevision()<getMinorRevision())
			return 1;
		if(otherVersion.getMinorRevision()>getMinorRevision())
			return -1;
		
		return 0;

	}

}
