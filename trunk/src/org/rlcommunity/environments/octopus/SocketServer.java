package org.rlcommunity.environments.octopus;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.*;

import org.rlcommunity.environments.octopus.protocol.DataReader;
import org.rlcommunity.environments.octopus.protocol.DataWriter;
import org.rlcommunity.environments.octopus.protocol.State;

public class SocketServer {
    
    private static final Logger LOGGER = Logger.getLogger("SocketServer");
    
    private int actionSize;
    
    private Octopus env;
    private RewardLogger logger;
    private int port;
    
    private Map<String, Command> commands;
    
    public SocketServer(Octopus env, int port) {
        actionSize = env.getTaskSpec().getNumActionVariables();
        
        this.env = env;
        this.port = port;
        
        commands = new HashMap<String, Command>();
        commands.put("GET_TASK", new GetTaskCommand());
        commands.put("START_LOG", new StartLogCommand());
        commands.put("START", new StartCommand());
        commands.put("STEP", new StepCommand());
    }
    
    public void run() throws IOException {
        while (true) {            
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();
            
            try {
                LOGGER.fine("Received connection");
                serverSocket.close(); /* stop listening while an agent is connected */

                InputStreamReader inStreamReader=new InputStreamReader(new BufferedInputStream(socket.getInputStream()), "UTF-8");
                DataReader in = new DataReader(inStreamReader);
                        
                OutputStreamWriter outStreamWriter=new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream()), "UTF-8");

                DataWriter out = new DataWriter(outStreamWriter);

                logger = null;
                boolean sessionTerminated = false;
                
                String line;
                while (!sessionTerminated && (line = in.readLine()) != null) {
                    line = line.trim();
                    LOGGER.finest(line);
                    if (!commands.containsKey(line)) {
                        /* illegal command; terminate the session */
                        sessionTerminated = true;
                    } else {
                        try {
                            commands.get(line).handle(in, out);
                        } catch (RuntimeException ex) {
                            ex.printStackTrace();
                            sessionTerminated = true;
                        }
                    }
                }
                
                if (logger != null) {
                    logger.finish();
                }
                
                in.close();
                out.close();
                socket.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "I/O error occurred", ex);
            }
        }
    }
    
    private interface Command {
        public void handle(DataReader in, DataWriter out) throws IOException;
    }
    
    private class GetTaskCommand implements Command {
        public void handle(DataReader in, DataWriter out) throws IOException {
//            out.writeTaskSpec(env.getTaskSpec());
        }
    }
    
    private class StartCommand implements Command {
        public void handle(DataReader in, DataWriter out) throws IOException {
//           out.writeState(env.start());
        }
    }
    
    private class StepCommand implements Command {
        public void handle(DataReader in, DataWriter out) throws IOException {
//            double[] action = in.readDoubleArray();
//            out.writeObservable(env.step(action));
        }
    }
    
    private class StartLogCommand implements Command {
        public void handle(DataReader in, DataWriter out) throws IOException {
//            String name = in.readLine();
//            env.addObserver(logger = new RewardLogger(name));
        }
    }
}
