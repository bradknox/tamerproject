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


package rlVizLib.utilities.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a utility class that can be used by environments to keep episode logs
 * in temporary files on their computer.  These logs can be easily sent to the 
 * experiment program (if requested) via {@link rlVizLib.messaging.environment.EpisodeSummaryRequest}.
 * <p>
 * Note that these log files do not persist past the end of session, or even past
 * a call to {@link EpisodeLogger.clear}.  If you need them for longer than that
 * remember to save a copy.
 * @author Brian Tanner
 */
public class EpisodeLogger {
    //1 million chars is about 1 MB right?
    static int defaultMaxStringSizeBeforeFileStarts = 1000000;
    int maxStringSizeBeforeFileStarts;
    StringBuilder theLogStringBuilder = null;
    File theLogFile = null;
    boolean failedWithError = false;

    /**
     * This doesn't really do anything fancy for now.
     */
    public EpisodeLogger() {
        theLogStringBuilder = new StringBuilder();
        this.maxStringSizeBeforeFileStarts=defaultMaxStringSizeBeforeFileStarts;
    }
    public EpisodeLogger(int bufferSize) {
        theLogStringBuilder = new StringBuilder();
        this.maxStringSizeBeforeFileStarts=bufferSize;
    }

    public void appendLogString(String newLogString) {
        if(failedWithError)return;
        
        if (newLogString.length() + theLogStringBuilder.length() > maxStringSizeBeforeFileStarts) {
            flush();
            appendToFile(newLogString);
        } else {
            theLogStringBuilder.append(newLogString);
        }
    }
    
    public String getLogSubString(long startPoint, int amount){
            flush();
        try {
            RandomAccessFile raf = new RandomAccessFile(theLogFile, "r");
            raf.seek(startPoint);
            byte[] result=new byte[amount];
            int amountRead=raf.read(result);
            String stringResult=new String(result);
            if(amountRead<=0)return "";
            String returnString=stringResult.substring(0, amountRead);
            raf.close();
            return returnString;
        } catch (IOException ex) {
            Logger.getLogger(EpisodeLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
            return "";
    }
    
    public void clear(){
        if(theLogFile!=null)theLogFile.delete();
        theLogFile=null;
        failedWithError=false;
        theLogStringBuilder=new StringBuilder();
    }
    public void flush(){
            appendBufferToFile();
            theLogStringBuilder = new StringBuilder();        
    }

    private void appendBufferToFile() {
        appendToFile(theLogStringBuilder.toString());
    }

    private void appendToFile(String newLogString) {
        if (theLogFile == null) {
            makeTheFile();
        }

        if (!failedWithError) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(theLogFile, true));
                out.write(newLogString);
                out.close();
            } catch (IOException e) {
            System.err.println("Error ::was unable to write to temporary log file in EpisodeLogger. Suppressing further log messages after Exception printout.");
            System.err.println(e);
            failedWithError = true;
            }
        }
    }

    private void makeTheFile() {
        try {
            theLogFile = File.createTempFile("episode", "log");
            //Make sure the file doesn't outlive the program's life
            theLogFile.deleteOnExit();
        } catch (IOException ex) {
            System.err.println("Error ::was unable to create temporary log file in EpisodeLogger. Suppressing further log messages after Exception printout.");
            System.err.println(ex);
            failedWithError = true;
        }
    }
}
