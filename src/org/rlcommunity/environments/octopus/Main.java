package org.rlcommunity.environments.octopus;


import java.util.*;
import org.rlcommunity.rlglue.codec.types.Action;


public class Main {
    
    private static final int PROFILING_LENGTH = 700;
    private static enum ControlMode {
        INTERNAL, EXTERNAL, EXTERNAL_GUI, PROFILING;
    }
    
    public static void main(String[] args) {

        Octopus oct=new Octopus();
        
        oct.env_init();
        
        oct.env_start();
        
        Action bigAction=new Action(32,0);
            
        Random r=new Random();
        for(int i=0;i<500;i++){
            if(i%100==0)System.out.println("Done step: "+i);
            int[] actions=new int[1];
            
                actions[0]=r.nextInt(6);
            
            bigAction.intArray=actions;
            
            oct.env_step(bigAction);
            
        }
    }
}
//        ControlMode controlMode = ControlMode.EXTERNAL_GUI;
//
//
//        Constants.init(config.getConstants());
//        
//        DisplayFrame display = null;
//        Octopus env = new Octopus(config.getEnvironment());
//        
//        if (controlMode == ControlMode.INTERNAL || controlMode == ControlMode.EXTERNAL_GUI) {
//            display = new DisplayFrame(env);
//        }
//        
//        if (controlMode == ControlMode.PROFILING) {
//            long startTime = System.currentTimeMillis();
//            double[] action = new double[3 * env.getArm().getCompartments().size()];
//            for (int t = 0; t < PROFILING_LENGTH; t++) {
//                for (int i = 0; i < action.length; i++) {
//                    action[i] = Math.random();
//                }
////                env.step(action);
//            }
//            System.out.printf("Time elapsed: %d%n",
//                    System.currentTimeMillis() - startTime);
//            System.exit(0);
//        } else {
//            try {
//                new SocketServer(env, serverPort).run();
//            } catch (IOException ex) {
//                System.err.printf("An I/O error occurred: %s.", ex.getMessage());
//                ex.printStackTrace();
//            }
//        }
