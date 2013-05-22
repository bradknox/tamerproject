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


package rlVizLib.messaging.environment;

import java.io.File;

/**
 * This class is technically a cheater or fake response, because it doesn't 
 * actually function over the network.  Instead, it is build from several 
 * smaller {@link EpisodeSummaryChunkResponse} messages.
 * <p>
 * The behavior of the code that creates this log {@link EpisodeSummaryRequest} 
 * is such that this log file will not persist past the end of session, it will
 * be deleted. If you really want to <i>save</i> it permantently, make a copy.
 * @author btanner
 * @see EpisodeSummaryChunkResponse
 */public class EpisodeSummaryResponse  {

    File theTmpLogFile = null;

    public EpisodeSummaryResponse(File tmpLogFile) {
        this.theTmpLogFile = tmpLogFile;
    }

    public File getLogFile() {
        return theTmpLogFile;
    }

    //If the client didn't support it we could just make one of these
    EpisodeSummaryResponse() {
    }
};
