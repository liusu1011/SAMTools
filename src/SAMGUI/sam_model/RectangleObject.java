/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * 
 */
package SAMGUI.sam_model;

import SAMGUI.sam_controller.CanvasAddMouseHandler;
import SAMGUI.sam_controller.MouseActionHandler;

public class RectangleObject extends SamModelObject {
	public RectangleObject() {
		super();
		MouseActionHandler mouseHandler = new MouseActionHandler();

		this.addMouseListener(mouseHandler);
		this.addMouseMotionListener(mouseHandler);
		this.addMouseListener(new CanvasAddMouseHandler());
	}

}
