/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rlcommunity.environments.octopus.config;

/**
 *
 * @author btanner
 */
public class NewConfig {
protected ConstantSet theConstants=null;
 protected EnvSpec environment=null;
 
public NewConfig(){
    environment=new EnvSpec();
    theConstants=new ConstantSet();
}
public ConstantSet getConstants(){
    return theConstants;
}

public EnvSpec getEnvironment(){
    return environment;
}

}
