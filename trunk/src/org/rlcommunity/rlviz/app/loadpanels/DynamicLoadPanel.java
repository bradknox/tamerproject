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
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;


import org.rlcommunity.rlviz.app.ParameterHolderPanel;
import org.rlcommunity.rlviz.app.RLGlueLogic;

import rlVizLib.general.ParameterHolder;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

public abstract class DynamicLoadPanel implements ActionListener,LoadPanelInterface {

    JComboBox theComboBox = null;
    RLGlueLogic theGlueConnection = null;
    Vector<String> theNames = null;
    Vector<ParameterHolder> theParams = null;
    JPanel publicPanel = null;
    protected int currentLoadedIndex = -1;
    ParameterHolderPanel theParamPanel = null;
    JPanel subPanel = null;
    JPanel descriptionPanel = null;

    public DynamicLoadPanel(RLGlueLogic theGlueConnection, String comboBoxInitMessage) {
        this.theGlueConnection = theGlueConnection;

        theComboBox = new JComboBox(new String[]{comboBoxInitMessage});
        theComboBox.setEnabled(false);

        publicPanel = new JPanel();
        publicPanel.setLayout(new BoxLayout(publicPanel, BoxLayout.Y_AXIS));

        theParamPanel = new ParameterHolderPanel();
        theComboBox.addActionListener(this);

        //
        //Setup the border for the publicPanel 
        //
        TitledBorder titled = null;
        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        titled = BorderFactory.createTitledBorder(loweredetched, "Choose " + getStringType());
        titled.setTitleJustification(TitledBorder.CENTER);
        publicPanel.setBorder(titled);
    }
     
    public boolean canLoad(){
        //No choices if no names
        return !theNames.isEmpty();
    }


    /* (non-Javadoc)
	 * @see btViz.loadPanels.LoadPanelInterface#updateList()
	 */
    abstract public void updateList();

    /* (non-Javadoc)
	 * @see btViz.loadPanels.LoadPanelInterface#refreshList(java.util.Vector, java.lang.String)
	 */
    public void refreshList(Vector<String> newValues, String emptyMessage) {
        theComboBox.removeAllItems();

        for (String thisName : newValues) {
            theComboBox.addItem(thisName);
        }
        
        
        if(newValues.size() == 0){
            theComboBox.setEnabled(false);
            theComboBox.addItem(emptyMessage);
        }else{
             theComboBox.setEnabled(true);
        }
        
    }

    /* (non-Javadoc)
	 * @see btViz.loadPanels.LoadPanelInterface#getPanel()
	 */
    public JPanel getPanel() {
        return publicPanel;
    }

    /* (non-Javadoc)
	 * @see btViz.loadPanels.LoadPanelInterface#setEnabled(boolean)
	 */
    public void setEnabled(boolean b) {
        //If there are no items in the list, don't listen to these guys
        if(!theNames.isEmpty()){
            theParamPanel.setEnabled(b);
            theComboBox.setEnabled(b);
        }
    }

    /* (non-Javadoc)
	 * @see btViz.loadPanels.LoadPanelInterface#load(java.lang.String, rlVizLib.general.ParameterHolder)
	 */
    public abstract boolean load(String thisName, ParameterHolder thisP);

    /* (non-Javadoc)
	 * @see btViz.loadPanels.LoadPanelInterface#load()
	 */
    public final boolean load() {
        if (currentLoadedIndex != -1 && !theNames.isEmpty()) {
            String thisName = theNames.get(currentLoadedIndex);
            updateParamsFromPanel();
            ParameterHolder thisP = theParams.get(currentLoadedIndex);
            boolean loadCheck = load(thisName, thisP);
            if(loadCheck) return true;
            else return false;
        }else{
            System.err.println("Load was called on the DynamicLoad Panel but there are none of what you tried to load or we couldn't set the index right");
            return false;
        }
    }
    /* (non-Javadoc)
	 * @see btViz.loadPanels.LoadPanelInterface#updateParamsFromPanel()
	 */
    public void updateParamsFromPanel() {
        if (currentLoadedIndex != -1 && !theNames.isEmpty()) {
            ParameterHolder latestP = theParamPanel.updateParamHolderFromPanel();
            theParams.set(currentLoadedIndex, latestP);
        }
    }

    /* (non-Javadoc)
	 * @see btViz.loadPanels.LoadPanelInterface#getStringType()
	 */
    abstract public String getStringType();
    /* (non-Javadoc)
	 * @see btViz.loadPanels.LoadPanelInterface#actionPerformed(java.awt.event.ActionEvent)
	 */
 
    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        int whichIndex = cb.getSelectedIndex();

        updateParamsFromPanel();

        if (whichIndex > -1) {
            
            

            if (subPanel != null) {
                publicPanel.remove(theComboBox);
                publicPanel.remove(subPanel);
                subPanel = null;
            }
            if (descriptionPanel != null) {
                descriptionPanel.removeAll();
                publicPanel.remove(descriptionPanel);
                descriptionPanel = null;
            }
            
            theComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
            publicPanel.add(theComboBox);

            FormLayout layout = new FormLayout("right:pref,10px,left:pref:grow", "");

            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            builder.setDefaultDialogBorder();


            builder.appendSeparator("Parameters");

            //It's possible that the message box actually just has a "no envs loaded" message in it
            if(!theParams.isEmpty()){
                theParamPanel.switchParameters(theParams.get(whichIndex));
                currentLoadedIndex = whichIndex;
    

                theParamPanel.addParamFieldsToBuilder(builder);
                subPanel = builder.getPanel();
                subPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
                

                descriptionPanel = makeDescriptionPanel(theParams.get(whichIndex));
                publicPanel.add(descriptionPanel);

                publicPanel.add(subPanel);
            }

            publicPanel.updateUI();
            Container parent=publicPanel;
            while(parent.getParent()!=null)parent=parent.getParent();
            if(parent instanceof JFrame)((JFrame)parent).pack();
        }
    }

    private JPanel makeDescriptionPanel(ParameterHolder P) {
        JPanel newPanel = new JPanel();
        newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.PAGE_AXIS));

        String name="Default";
        if (P.isParamSet("###name")) {
            name = P.getStringParam("###name");
        }
        if (P.isParamSet("###description")) {
            String description = P.getStringParam("###description");
            
            //Roughly.. this is a hack
            int numRows = description.length()/20;
            
            JTextArea descText = new JTextArea(description,numRows,20);
            makeTextAreaLookLikeLable(descText);
            
            Font theFont = new Font("Verdana",Font.PLAIN,10);
            descText.setFont(theFont);

            newPanel.add(descText);
        }
        if (P.isParamSet("###authors")) {
            String authors = P.getStringParam("###authors");

            //Roughly.. this is a hack
            int numRows = authors.length()/20;
            

            JTextArea authorLabel = new JTextArea(authors,numRows,20);
            makeTextAreaLookLikeLable(authorLabel);
            Font theFont = new Font("Verdana", Font.ITALIC,10);

            authorLabel.setFont(theFont);
            newPanel.add(Box.createRigidArea(new Dimension(10,10)));
            newPanel.add(authorLabel);
        }
        //Btanner Jan 25/08.  Removed this cause it wraps and is ugly and its in the more info box.
//        if (P.isParamSet("###url")) {
//            String url = P.getStringParam("###url");
//            
//            
//            //Roughly.. this is a hack
//            int numRows = url.length()/20;
//
//            JTextArea urlLabel = new JTextArea(url,numRows,20);
//
//            makeTextAreaLookLikeLable(urlLabel);
//
//            Font theFont = new Font("Verdana",Font.PLAIN,10);
//            urlLabel.setFont(theFont);
//
//            newPanel.add(Box.createRigidArea(new Dimension(10,10)));
//            newPanel.add(urlLabel,Component.LEFT_ALIGNMENT);
//        }
        
        //Add the about button
        JButton theAboutButton=new JButton("More info...");
        theAboutButton.addActionListener(new AgentEnvDetailsBox(P));
        newPanel.add(theAboutButton,Component.CENTER_ALIGNMENT); 

        
        if(newPanel.getComponentCount()>0){
                    TitledBorder titled = null;
        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        titled = BorderFactory.createTitledBorder(loweredetched, name);
        titled.setTitleJustification(TitledBorder.LEFT);
        newPanel.setBorder(titled);

        }
        return newPanel;

    }
    

    
    //  This will allow selection and copy to work but still retain the label look
	private void makeTextAreaLookLikeLable (JTextArea textArea)  {
 
		//  Turn on word wrap
		textArea.setLineWrap (true);
                textArea.setMaximumSize(new Dimension(publicPanel.getPreferredSize().width,1000));

                textArea.setWrapStyleWord (true);
 
		//  Perform the other changes to complete the look
		makeTextComponentLookLikeLabel (textArea);
	}
 
 
	//  This will allow selection and copy to work but still retain the label look
	private void makeTextComponentLookLikeLabel (JTextComponent textcomponent)  {
 
		//  Make the text component non editable
		textcomponent.setEditable (false);
 
		//  Make the text area look like a label
		textcomponent.setBackground ((Color)UIManager.get ("Label.background"));
		textcomponent.setForeground ((Color)UIManager.get ("Label.foreground"));
		textcomponent.setBorder (null);
	}
    }


