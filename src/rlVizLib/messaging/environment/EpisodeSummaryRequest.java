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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.interfaces.ProvidesEpisodeSummariesInterface;

/**
 * This is a message (with submessages) to get a string representation of an episode summary.
 * An episode summary might be quite long, so the environment might prefer to
 * store it temporarily on disk instead of in memory.  We'll therefore send it
 * back and forth in chunks, in case it's too large to fit in memory at one time.
 * <p>
 * The format of the request is:
 * to: kEnv
 * <p>
 * from: kBenchmark
 * <p>
 * command: kEnvQueryEpisodeSummary
 * <p>
 * type: String
 * <p>
 * payload: chunkstart:chunksize  where chunkStart and defaultChunkSize are integers
 * indicating which character of the summary file to start at and how many 
 * characters to send
 * @author btanner
 */
public class EpisodeSummaryRequest extends EnvironmentMessages {
//1 million chars is about a MB
    final static int defaultChunkSize = 1000000;
    private int theChunkSize;
    private long theStartCharacter;

    public EpisodeSummaryRequest(GenericMessage theMessageObject) {
        super(theMessageObject);
        String thePayLoad = super.getPayload();
        StringTokenizer paramTokenizer = new StringTokenizer(thePayLoad, ":");
        this.theChunkSize = Integer.parseInt(paramTokenizer.nextToken());
        this.theStartCharacter = Long.parseLong(paramTokenizer.nextToken());
    }

    public static String makeRequest(long startChar, int theChunkSize) {
        String theRequest = AbstractMessage.makeMessage(
                MessageUser.kEnv.id(),
                MessageUser.kBenchmark.id(),
                EnvMessageType.kEnvQueryEpisodeSummary.id(),
                MessageValueType.kStringList.id(),
                theChunkSize + ":" + startChar);

        return theRequest;
    }

    private static EpisodeSummaryChunkResponse ExecuteChunk(long startChar, int theChunkSize) {
        EpisodeSummaryChunkResponse theChunk = null;
        try {
            String theRequest = makeRequest(startChar, theChunkSize);
            String responseMessage = RLGlue.RL_env_message(theRequest);

            theChunk = new EpisodeSummaryChunkResponse(responseMessage);
        } catch (NotAnRLVizMessageException ex) {
            System.err.println("Error Parsing Log Chunk Message: " + ex);
            Thread.dumpStack();
            //Sending an empty string with a non-zero size will signal the caller
            //that no more data is coming
            theChunk = new EpisodeSummaryChunkResponse("", theChunkSize);
        }
        return theChunk;
    }

    public static EpisodeSummaryResponse Execute() {
        long nextChunkStart = 0L;
        File tmpLogFile = null;
        try {
            tmpLogFile = File.createTempFile("rlviz", "summary");
            //Make sure the file doesn't outlive the program's life
            tmpLogFile.deleteOnExit();
        } catch (IOException ex) {
            System.err.println("was unable to create temporary log file in EpisodeSummaryResponse");
            return new EpisodeSummaryResponse();
        }

        EpisodeSummaryChunkResponse thisChunk = null;

        do {
            thisChunk = ExecuteChunk(nextChunkStart, EpisodeSummaryRequest.defaultChunkSize);
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(tmpLogFile, true));
                out.write(thisChunk.getLogData());
                out.close();
            } catch (IOException e) {
                System.err.println("Error when writing received log chunk to file: " + e);
                Thread.dumpStack();
                break;
            }
            nextChunkStart += EpisodeSummaryRequest.defaultChunkSize;
        } while (thisChunk.getAmountReceived()>=EpisodeSummaryRequest.defaultChunkSize);


        EpisodeSummaryResponse theResponse = new EpisodeSummaryResponse(tmpLogFile);
        return theResponse;
    }

    @Override
    public String handleAutomatically(EnvironmentInterface theEnvironment) {
        ProvidesEpisodeSummariesInterface castedEnv = (ProvidesEpisodeSummariesInterface) theEnvironment;

        String theLogString = castedEnv.getEpisodeSummary(theStartCharacter, theChunkSize);
        EpisodeSummaryChunkResponse theResponse = new EpisodeSummaryChunkResponse(theLogString, theChunkSize);
        return theResponse.makeStringResponse();
    }

    @Override
    public boolean canHandleAutomatically(Object theEnvironment) {
        return (theEnvironment instanceof ProvidesEpisodeSummariesInterface);
    }
}
