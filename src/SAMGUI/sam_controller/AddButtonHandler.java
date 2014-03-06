/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * Handles events from the model toolbar in SamFrame
 * 
 */

package SAMGUI.sam_controller;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JToggleButton;

import SAMGUI.sam_view.SamCanvas;
import SAMGUI.sam_view.SamFrame;
import SAMGUI.sam_view.SamFrame.Toggle;

public class AddButtonHandler extends AbstractAction {
	public AddButtonHandler() {
		putValue(NAME, "AddButtonHandler");
		putValue(SHORT_DESCRIPTION, "Handles button click from model toolbar");
	}

	public void actionPerformed(ActionEvent e) {
		JToggleButton btn = (JToggleButton) e.getSource();
		Toggle setting = Toggle.getValue(btn);

		SamFrame.getInstance().setToggle(setting);
		SamCanvas canvas = SamFrame.getInstance().getCurrentCanvas();

		if (btn.isSelected()) {
			canvas.setToggle(setting);
		} else {
			canvas.setToggle(Toggle.INVALID);
		}
	}

}