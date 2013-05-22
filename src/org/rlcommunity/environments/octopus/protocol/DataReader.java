package org.rlcommunity.environments.octopus.protocol;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class DataReader extends FilterReader {

    private static final String LINE_SEP = "\n";
    
    private static final Logger LOGGER = Logger.getLogger("protocol.DataReader");
    
    private Scanner scanner;
    private int maxArraySize;
    
    public DataReader(InputStreamReader in) {
        this(in, 10000);
    }
    
    public DataReader(InputStreamReader in, int maxArraySize) {
        super(in);
        scanner = new Scanner(in);
        scanner.useDelimiter(LINE_SEP);
        this.maxArraySize = maxArraySize;
    }
    
    public String readLine() throws IOException {
        if (!scanner.hasNext()) {
            return null;
        }
        String line = scanner.next();
        LOGGER.finest(line);
        checkException();
        return line;
    }
    
    public boolean readBoolean() throws IOException {
        boolean b = (scanner.nextInt() != 0);
        checkException();
        return b;
    }
    
    public int readInt() throws IOException {
        int i = scanner.nextInt();
        checkException();
        return i;
    }
    
    public double readDouble() throws IOException {
        double d = scanner.nextDouble();
        checkException();
        return d;
    }
    
    public double[] readDoubleArray() throws IOException {
        int size = readInt();
        if (size > maxArraySize) {
            throw new IOException("Encountered oversized array.");
        }
        double[] arr = new double[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readDouble();
        }
        return arr;
    }

    public TaskDescription readTaskSpec() throws IOException {
        int stateSize = readInt();
        int actionSize = readInt();
        return new TaskDescription(stateSize, actionSize);
    }
    
    public State readState() throws IOException {
        boolean terminal = readBoolean();
        double[] data = readDoubleArray();
        return new State(data, terminal);
    }
    
    public Observable readObservable() throws IOException {
        double reward = readDouble();
        State state = readState();
        return new Observable(state, reward);
    }
    
    private void checkException() throws IOException {
        if (scanner.ioException() != null) {
            throw scanner.ioException();
        }
    }
}
