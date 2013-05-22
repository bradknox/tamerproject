package org.rlcommunity.environments.octopus.protocol;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;



public class DataWriter extends FilterWriter {
    
    private static final String LINE_SEP = "\n";
    
    public DataWriter(OutputStreamWriter out) {
        super(out);
    }
    
    public void writeLine(String line) throws IOException {
        out.write(line);
        out.write(LINE_SEP);
        out.flush();
    }
    
    public void writeInt(int i) throws IOException {
        writeLine(Integer.toString(i));
    }
    
    public void writeBoolean(boolean b) throws IOException {
        writeInt(b ? 1 : 0);
    }
    
    public void writeDouble(double d) throws IOException {
        writeLine(Double.toString(d));
    }
    
    public void writeDoubleArray(double[] arr) throws IOException {
        writeInt(arr.length);
        for (double d: arr) {
            writeDouble(d);
        }
    }
    
    public void writeTaskSpec(TaskDescription ts) throws IOException {
        writeInt(ts.getNumStateVariables());
        writeInt(ts.getNumActionVariables());
    }
    
    public void writeState(State s) throws IOException {
        writeBoolean(s.isTerminal());
        writeDoubleArray(s.getData());
    }
    
    public void writeObservable(Observable o) throws IOException {
        writeDouble(o.getReward());
        writeBoolean(o.getState().isTerminal());
        writeDoubleArray(o.getState().getData());
    }
}
