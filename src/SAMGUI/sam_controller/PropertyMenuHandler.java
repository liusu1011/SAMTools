/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * handles the creation of property dialogs for samModelObjects
 */
package SAMGUI.sam_controller;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import SAMGUI.sam_model.Arc;
import SAMGUI.sam_model.Component;
import SAMGUI.sam_model.Connector;
import SAMGUI.sam_model.Port;
import SAMGUI.sam_model.SamModelObject;
import SAMGUI.sam_view.SamFrame;

public class PropertyMenuHandler extends AbstractAction {
	
	SamModelObject samObj;
	
	public PropertyMenuHandler(SamModelObject obj){
		samObj = obj;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(samObj instanceof Arc){
			ArcDialog dlg = new ArcDialog((Arc) samObj);
			dlg.setVisible(true);
		}
		else if(samObj instanceof Component){
			ComponentDialog dlg = new ComponentDialog((Component) samObj);
			dlg.setVisible(true);
		}
		else if(samObj instanceof Connector){
			ConnectorDialog dlg = new ConnectorDialog((Connector) samObj);
			dlg.setVisible(true);
		}
		else if(samObj instanceof Port){
			PortDialog dlg = new PortDialog((Port) samObj);
			dlg.setVisible(true);
		}
		
	}

}
