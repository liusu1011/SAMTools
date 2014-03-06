/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * Dialog to set properties on arc
 */
package SAMGUI.sam_controller;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import SAMGUI.sam_model.Arc;
import SAMGUI.sam_model.Component;
import SAMGUI.sam_model.SamModelObject;


public class ArcDialog extends SamDialog {
	private Arc arc;
	private JComboBox comboLocations;
	private JLabel arcVarName;
	private JTextField varText;
	
	ArcDialog(Arc arc) {
		super(arc);
		this.arc = arc;
		
		this.setup();
	}
	
	//saves properties that can be changed in dialog
	@Override
	protected void save(){
		super.save();
		
		arc.setLocationOnConnector(comboLocations.getSelectedIndex());
		arc.setVar(varText.getText());
		this.dispose();
	}
	
	private void setup(){
		comboLocations = new JComboBox();
		comboLocations.setMaximumRowCount(100);
		comboLocations.setModel(new DefaultComboBoxModel(new String[] {"Bottom", "Left", "Left + 1", "Left + 2", "Left + 3", "Top", "Right", "Right + 1", "Right + 2", "Right + 3"}));
		comboLocations.setBounds(10, 88, 155, 22);
		comboLocations.setSelectedIndex(arc.getLocationOnConnector());
		panel.add(comboLocations);
		
		
		JLabel lblLocation = new JLabel("Location On Component:");
		lblLocation.setBounds(10, 61, 186, 14);
		panel.add(lblLocation);
		
		//set arc variable for net composition model;
		arcVarName = new JLabel("Variable: ");
		arcVarName.setBounds(10, 150, 75, 14);
		panel.add(arcVarName);
		
		varText = new JTextField();
		varText.setText(arc.getVar());
		varText.setBounds(10, 170, 100, 22);
		panel.add(varText);
		
	}

}
