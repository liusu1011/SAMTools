/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * handles the delete action from the key press delete
 * if object is selected then it is deleted
 */
package SAMGUI.sam_controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import SAMGUI.sam_model.SamModelObject;
import SAMGUI.sam_view.SamFrame;

public class DeleteHandler implements KeyListener{

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		SamModelObject obj = (SamModelObject) e.getSource();
		if(e.getKeyCode() == 127){
			if(obj.isSelected()){
				SamFrame.getInstance().getCurrentCanvas().removeSamModelObject(obj);
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

}
