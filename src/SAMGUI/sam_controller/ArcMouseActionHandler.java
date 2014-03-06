/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * handles the movement of the arc points
 * 
 */
package SAMGUI.sam_controller;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import SAMGUI.sam_model.Arc;
import SAMGUI.sam_model.Component;
import SAMGUI.sam_model.Port;
import SAMGUI.sam_model.SamModelObject;
import SAMGUI.sam_view.SamFrame;

public class ArcMouseActionHandler implements MouseInputListener {

	private Point origin;

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
		
		origin = e.getPoint();

		

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
		JComponent obj = (JComponent) e.getSource();
		
		Arc arc = (Arc) obj;
		if(arc.containsMovablePoint(origin)){
			arc.setLocation(e.getX(), e.getY());
		}

		obj.setLocation(e.getPoint());
		
		
		arc.updateBounds();

		
		SamFrame.getInstance().getCurrentCanvas().getCanvas().repaint();
		SamFrame.getInstance().getCurrentCanvas().getCanvas().validate();
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

}
