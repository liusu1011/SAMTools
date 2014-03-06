
/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * handles the mouse movement of labels and samModelObjects
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

public class MouseActionHandler implements MouseInputListener {

	int offsetX = 0;
	int offsetY = 0;
	
	//default value for expanding canvas
	private final int EXPAND = 60;

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		JComponent obj = (JComponent) e.getSource();
		if (e.getButton() == MouseEvent.BUTTON1) {
			Point origin = javax.swing.SwingUtilities.convertPoint(obj,
					e.getPoint(), obj.getParent());
			
			//sets offset when mouse clicked on component
			offsetX = obj.getLocation().x - origin.x;
			offsetY = obj.getLocation().y - origin.y;
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
		JComponent obj = (JComponent) e.getSource();
		

		Point moved = javax.swing.SwingUtilities.convertPoint(obj,
				e.getPoint(), obj.getParent());
		
		int movedX = moved.x + offsetX;
		int movedY = moved.y + offsetY;
		
		if(movedX < 0){
			movedX = 0;
		}
		
		if(movedY < 0){
			movedY = 0;
		}
		
		
		obj.setLocation(movedX, movedY);
		if(obj instanceof SamModelObject){
			((SamModelObject)obj).setLabelLocation();
		}
		
		if(obj.getParent() instanceof JPanel){
			
			JPanel pnl = (JPanel) obj.getParent();
			
			int expandX = pnl.getWidth();
			int expandY = pnl.getHeight();
			
			//if object is being moved outside the bounds of the panel
			//it is inside, then panel will be grown
			
			if(pnl.getWidth() < movedX + EXPAND){
				expandX = movedX + obj.getWidth() + EXPAND;
			}
			
			if(pnl.getHeight() < movedY + EXPAND){
				expandY = movedY + obj.getHeight() + EXPAND;
			}
			

			pnl.setPreferredSize(new Dimension(expandX, expandY));

			
			pnl.scrollRectToVisible(obj.getBounds());
		}
		
		
		obj.setLocation(movedX, movedY);
		
		if(obj instanceof SamModelObject){
			SamModelObject sam = (SamModelObject) obj;
			
			//update the bounds of the child objects
			//for instance if a compnent has a port, this will
			//update its location
			
			sam.updateChildren();
		}
		
		SamFrame.getInstance().getCurrentCanvas().getCanvas().repaint();
		SamFrame.getInstance().getCurrentCanvas().getCanvas().validate();
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

}
