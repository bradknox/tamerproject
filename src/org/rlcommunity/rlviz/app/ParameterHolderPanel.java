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


package org.rlcommunity.rlviz.app;

import java.awt.Component;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
 
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import rlVizLib.general.ParameterHolder;


import com.jgoodies.forms.builder.DefaultFormBuilder;
import java.awt.Font;

public class ParameterHolderPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Vector<Component> allComponents=new Vector<Component>();
	Map<String, Component> nameToValueMap= new TreeMap<String, Component>();

        Font pFont = new Font("Courier",Font.PLAIN,10);

        
	public ParameterHolderPanel(){
		super(); 
	}


	public void setEnabled(boolean shouldEnable){
		for (Component thisComponent : allComponents)thisComponent.setEnabled(shouldEnable);
	}

	ParameterHolder currentParamHolder=null;

	public void switchParameters(ParameterHolder p) {
		this.currentParamHolder=p;
		allComponents.removeAllElements();
	}

	public ParameterHolder updateParamHolderFromPanel(){
		for(int i=0;i<currentParamHolder.getParamCount();i++){
			int thisParamType=currentParamHolder.getParamType(i);
			String thisParamName=currentParamHolder.getParamName(i);
                        if(thisParamName.toLowerCase().startsWith("###"))continue;
			Component theRelatedComponent=nameToValueMap.get(thisParamName);

			switch (thisParamType) {
			case ParameterHolder.boolParam:
				JCheckBox boolField=(JCheckBox)theRelatedComponent;
				currentParamHolder.setBooleanParam(thisParamName, boolField.isSelected());
				break;

			case ParameterHolder.intParam:
				JTextField intField=(JTextField)theRelatedComponent;
				currentParamHolder.setIntegerParam(thisParamName, Integer.parseInt(intField.getText()));
				break;

			case ParameterHolder.doubleParam:
				JTextField doubleField=(JTextField)theRelatedComponent;
				currentParamHolder.setDoubleParam(thisParamName, Double.parseDouble(doubleField.getText()));
				break;
			case ParameterHolder.stringParam:
				JTextField stringField=(JTextField)theRelatedComponent;
				currentParamHolder.setStringParam(thisParamName,stringField.getText());
				break;
			}

		}
		return currentParamHolder;

	}




	private Component addIntParameter(DefaultFormBuilder builder, String thisParamName, int theParam) {
		JLabel thisLabel=new JLabel(thisParamName+":", JLabel.TRAILING);
		JTextField thisField=new JTextField(5);
		thisLabel.setLabelFor(thisField);
		thisField.setText(""+theParam);

		builder.append(thisLabel); 
		builder.append(thisField); 
		builder.nextLine(); 

		allComponents.add(thisLabel);
		allComponents.add(thisField);


		return thisField;
	}

	private Component addDoubleParameter(DefaultFormBuilder builder, String thisParamName, double theParam) {
		JLabel thisLabel=new JLabel(thisParamName+":", JLabel.TRAILING);
		JTextField thisField=new JTextField(5);
		thisLabel.setLabelFor(thisField);
		thisField.setText(""+theParam);
		builder.append(thisLabel); 
		builder.append(thisField); 
		builder.nextLine(); 


		allComponents.add(thisLabel);
		allComponents.add(thisField);

		return thisField;
	}

    private void addLabel(DefaultFormBuilder builder, String thisLabelString) {
		JLabel thisLabel=new JLabel(thisLabelString);
                builder.append(thisLabel);
                builder.nextLine();
		allComponents.add(thisLabel);
    }

	private Component addStringParameter(DefaultFormBuilder builder, String thisParamName, String theParam) {
		JLabel thisLabel=new JLabel(thisParamName+":", JLabel.TRAILING);
		JTextField thisField=new JTextField(5);
		thisLabel.setLabelFor(thisField);
		thisField.setText(""+theParam);

		allComponents.add(thisLabel);
		allComponents.add(thisField);

		builder.append(thisLabel); 
		builder.append(thisField); 
		builder.nextLine(); 

		return thisField;
	}


	private Component addBoolParameter(DefaultFormBuilder builder, String thisParamName, boolean currentValue) {
		JLabel thisLabel=new JLabel(thisParamName+":", JLabel.TRAILING);
		JCheckBox thisField=new JCheckBox();
		thisField.setSelected(currentValue);
		thisLabel.setLabelFor(thisField);

		allComponents.add(thisLabel);
		allComponents.add(thisField);

		builder.append(thisLabel); 
		builder.append(thisField); 
		builder.nextLine(); 

		return thisField;
	}


	public void addParamFieldsToBuilder(DefaultFormBuilder builder) {
                int realParams=0;
                
		for(int i=0;i<currentParamHolder.getParamCount();i++){
			int thisParamType=currentParamHolder.getParamType(i);
			String thisParamName=currentParamHolder.getParamName(i);
                        if(thisParamName.toLowerCase().startsWith("###"))
                            continue;
                        
                        realParams++;
			Component newField=null;
			switch (thisParamType) {
			case ParameterHolder.boolParam:
				newField=addBoolParameter(builder,thisParamName, currentParamHolder.getBooleanParam(thisParamName));
				break;
			case ParameterHolder.intParam:
				newField=addIntParameter(builder,thisParamName, currentParamHolder.getIntegerParam(thisParamName));
				break;
			case ParameterHolder.doubleParam:
				newField=addDoubleParameter(builder,thisParamName, currentParamHolder.getDoubleParam(thisParamName));
				break;
			case ParameterHolder.stringParam:
				newField=addStringParameter(builder,thisParamName, currentParamHolder.getStringParam(thisParamName));
				break;
			}
			nameToValueMap.put(thisParamName, newField);
		}
                //Set a nice fixed width font
                for (Component component : allComponents)component.setFont(pFont);


                if(realParams==0){
                    addLabel(builder, "No configurable Parameters");
                }
	}

}
