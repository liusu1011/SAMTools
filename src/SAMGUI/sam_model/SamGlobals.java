/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * 
 */
package SAMGUI.sam_model;

import java.awt.Rectangle;

public class SamGlobals {
	public final static Rectangle COMPONENT = new Rectangle(0, 0, 101, 101);
	public final static Rectangle CONNECTOR = new Rectangle(0, 0, 11, 61);
	
	public final static int PORT_TOP_BOTTOM_WIDTH = 25;
	public final static int PORT_TOP_BOTTOM_HEIGHT = 17;
	public final static int PORT_LEFT_RIGHT_WIDTH = PORT_TOP_BOTTOM_HEIGHT;
	public final static int PORT_LEFT_RIGHT_HEIGHT = PORT_TOP_BOTTOM_WIDTH;
	
	//used for drawing the shape of the port
	public final static int PORT_RIGHT_START_ANGLE = 270;
	public final static int PORT_LEFT_START_ANGLE  = 90;
	public final static int PORT_TOP_START_ANGLE  = 0;
	public final static int PORT_BOTTOM_START_ANGLE  = 180;
	
	public final static int PORT_ARC_SWEEP = 180;
	
	//first positions for sides
	public final static int COMPONENT_RIGHT = 12;
	public final static int COMPONENT_TOP = 8;
	public final static int COMPONENT_LEFT = 4;
	public final static int COMPONENT_BOTTOM = 0;
	
	//first positions for sides
	public final static int CONNECTOR_RIGHT = 6;
	public final static int CONNECTOR_TOP = 5;
	public final static int CONNECTOR_LEFT = 1;
	public final static int CONNECTOR_BOTTOM = 0;
	

	public final static int PORT_TYPE_INPUT = 0;
	public final static int PORT_TYPE_OUTPUT = 1;
	
}
