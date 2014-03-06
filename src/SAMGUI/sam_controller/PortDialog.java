/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * dialog for changing port properties
 */
package SAMGUI.sam_controller;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import SAMGUI.sam_model.Port;
import SAMGUI.sam_model.SamModelObject;
import SAMGUI.sam_view.SamFrame;


public class PortDialog extends SamDialog {
	private Port port;
	private JComboBox comboLocations;
	private JComboBox comboType;
	private JComboBox comboPlaceInterfaces;
	
	private boolean ableToSetPlaceInterface = false;
	
	PortDialog(Port port) {
		super(port);
		this.port = port;
		
		this.setup();
	}
	
	@Override
	protected void save(){
		super.save();
		
		port.swapLocation(comboLocations.getSelectedIndex());
		port.setPortType(comboType.getSelectedIndex());
		if(this.ableToSetPlaceInterface)
			port.setPlaceInterface((String)comboPlaceInterfaces.getSelectedItem());
		this.dispose();
	}
	
	private void setup(){
		
		comboLocations = new JComboBox();
		comboLocations.setMaximumRowCount(100);
		comboLocations.setModel(new DefaultComboBoxModel(new String[] {"Bottom", "Bottom + 1", "Bottom + 2", "Bottom + 3", "Left", "Left + 1", "Left + 2", "Left + 3", "Top", "Top + 1", "Top + 2", "Top + 3", "Right", "Right + 1", "Right + 2", "Right + 3"}));
		comboLocations.setBounds(10, 88, 125, 22);
		comboLocations.setSelectedIndex(port.getLocationOnComponent());
		panel.add(comboLocations);
		
		JLabel lblLocation = new JLabel("Location On Component:");
		lblLocation.setBounds(10, 61, 176, 14);
		panel.add(lblLocation);
		
		JLabel lblInputoutputType = new JLabel("Input/Output Type:");
		lblInputoutputType.setBounds(10, 124, 133, 14);
		panel.add(lblInputoutputType);
		
		comboType = new JComboBox();
		comboType.setModel(new DefaultComboBoxModel(new String[] {"Input", "Output"}));
		comboType.setMaximumRowCount(2);
		comboType.setBounds(10, 151, 115, 22);
		comboType.setSelectedIndex(port.getPortType());
		panel.add(comboType);
		
		if(port.containsArc()){
			comboType.setEnabled(false);
		}
		
		JLabel lblSetPortPlaceInterface = new JLabel("SetPortPlaceInterface: ");
		lblSetPortPlaceInterface.setBounds(10, 213, 173, 14);
		panel.add(lblSetPortPlaceInterface);
		
		
		if(port.getParentComponent().isSetElemNetSpec){
			comboPlaceInterfaces = new JComboBox();
			comboPlaceInterfaces.setMaximumRowCount(100);
			if(port.getPlaceCountFromElementaryModel() == 0){
				comboPlaceInterfaces.setModel(new DefaultComboBoxModel(new String[] {"No Places Found in Elementary Model!"}));
			}else{
			String[] pnList = port.getPlaceNameListFromElementaryModel();
			comboPlaceInterfaces.setModel(new DefaultComboBoxModel(pnList));
			comboPlaceInterfaces.setBounds(10, 243, 153, 22);
			
			if(port.getIsSetPlaceInterface()){
				String n = port.getPlaceInterfaceName();
				comboPlaceInterfaces.setSelectedItem(n);
			}else{
				comboPlaceInterfaces.setSelectedIndex(0);
			}
			
			panel.add(comboPlaceInterfaces);
			
			this.ableToSetPlaceInterface = true;
			}	
		}
	}
}
	
