/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * handles the the adding of objects when you click when you want to add them
 */
package SAMGUI.sam_controller;

import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import SAMGUI.sam_view.SamCanvas;
import SAMGUI.sam_view.SamFrame;

public class CanvasAddMouseHandler implements MouseInputListener {

	@Override
	public void mouseClicked(MouseEvent e) {
		

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		SamCanvas canvas = SamFrame.getInstance().getCurrentCanvas();
		canvas.addSamModelObject(e.getPoint(), e.getSource());

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
