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



package org.rlcommunity.rlviz.app.loadpanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

import rlVizLib.rlVizCore;
import rlVizLib.general.ParameterHolder;

/**
 *
 * @author Brian Tanner
 */
public class AgentEnvDetailsBox implements ActionListener{
ParameterHolder theParamHolder;

    public AgentEnvDetailsBox(ParameterHolder theParamHolder){
        this.theParamHolder=theParamHolder;
        
    }
    public void actionPerformed(ActionEvent e) {
        createAboutBox();
    }

    private void createAboutBox() {
        String name="Details not specified.";
        String description="";
        String authors="";
        String url="";
        String loadName="unknown";
        String loadSource="unknown";

                        
        if (theParamHolder.isParamSet("###name")) {
            name = theParamHolder.getStringParam("###name");
        }

        if (theParamHolder.isParamSet("###description")) {
            description = theParamHolder.getStringParam("###description");
        }
        if (theParamHolder.isParamSet("###authors")) {
            authors = theParamHolder.getStringParam("###authors");
        }

        if (theParamHolder.isParamSet("###url")) {
            url = theParamHolder.getStringParam("###url");
        }
        if (theParamHolder.isParamSet("###loadname")) {
            loadName = theParamHolder.getStringParam("###loadname");
        }
        if (theParamHolder.isParamSet("###loadsource")) {
            loadSource = theParamHolder.getStringParam("###loadsource");
        }
        
        //default title and icon
        String theMessage =name;
        theMessage += "\n----------------------";
        theMessage += "\n"+description;
        theMessage += "\n\nCreated by: "+authors;
        theMessage += "\n"+url;
        theMessage += "\n\nTechnical Details\n----------------------";
        
        theMessage += "\nFull Qualified: "+loadName;
        theMessage += "\nLoaded From: "+loadSource;
        
        theMessage+="";
        
        

        JOptionPane.showMessageDialog(null, theMessage, "About "+name, JOptionPane.INFORMATION_MESSAGE);
    }

    
}
