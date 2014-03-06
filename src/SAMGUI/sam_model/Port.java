/* Author: Alexander Pataky
 * 
 * Modified: by Su Liu
 */
package SAMGUI.sam_model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;

import javax.swing.BorderFactory;

import pipe.dataLayer.Place;

import SAMGUI.sam_controller.CanvasAddMouseHandler;
import SAMGUI.sam_view.SamFrame;


public class Port extends SamModelObject {

	private String arcName = "";
	private String componentName = "";
	private int locationOnComponent;
	private int portType;
	private int startAngle;
	private Place placeInterface; 
	private boolean isSetPlaceInterface = false;
	
	public Port() {
		super();
		portType = SamGlobals.PORT_TYPE_OUTPUT;
		this.setLocationOnComponent(SamGlobals.COMPONENT_BOTTOM);
		this.addMouseListener(new CanvasAddMouseHandler());
		
	}

	public Port(String name) {
		this();
		this.setName(name);
		
	}
	
	
	public int getLocationOnComponent() {
		return locationOnComponent;
	}

	public void setLocationOnComponent(int locationOnComponent) {
		this.locationOnComponent = locationOnComponent;
		this.setLabelLocation();
		this.setBounds(0, 0, 0, 0);
	}
	
	public void swapLocation(int movingToLocation){
		Component parent = getParentComponent();
		if(parent != null){
			parent.swapPortLocations(this.locationOnComponent, movingToLocation);
		}
		
		this.setLocationOnComponent(movingToLocation);
		this.setLabelLocation();
		this.updateBounds();
	}
	
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getComponentName() {
		return componentName;
	}
	
	public Component getParentComponent() {
		if(this.getParentModel() != null){
			if(this.componentName.compareTo("") != 0 && this.componentName != null){
				return getParentModel().getComponent(componentName);
			}
		}
		return null;
	}
	
	public String getArcName() {
		return arcName;
	}

	public void setArcName(String arcName) {
		this.arcName = arcName;
	}

	public boolean containsArc() {
		return (arcName != null && arcName.compareTo("") != 0);
//		return arcName != null;
	}

	public void setPortType(int type) {
		this.portType = type;
	}

	public int getPortType() {
		return this.portType;
	}
	
	public void paintComponent(Graphics g){
        super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        //drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) 
        g2.draw(getPortShape());
        
    }
	
	public void updateBounds(){
		setBounds(null);
		
		this.updateChildren();
		
		
	}
	
	public void updateChildren(){
		Arc arc = this.getArc();
		if(arc != null){
			arc.updateBounds();
			arc.setInitialLabelLocation();
		}
	}
	
	public Arc getArc(){
		if(arcName != null){
			if(parentModel != null){
				return parentModel.getArc(arcName);
			}
		}
		
		return null;
	}
	
	//returns point where arc should connect to this
	public Point getArcPoint(){
		Point result = new Point();
		
		if(this.componentName.compareTo("") == 0){
			return result;
		}
		
		else{
			Component parent = this.getParentModel().getComponent(componentName);
			
			if(parent != null){
				if(parent.containsPort(this.getName())){
					if(this.locationOnComponent < SamGlobals.COMPONENT_BOTTOM){
						//error or bug? something not right
					}
					else if(this.locationOnComponent < SamGlobals.COMPONENT_LEFT){
						//port on bottom
						if(this.portType == SamGlobals.PORT_TYPE_OUTPUT){
							result.y = parent.getY() + parent.getHeight();
						}
						else{
							result.y = parent.getY() + parent.getHeight() - this.getHeight();
						}
						
						result.x = parent.getX() + parent.getWidth() - ((locationOnComponent - SamGlobals.COMPONENT_BOTTOM + 1) * this.getWidth());
						
						result.x = result.x + (this.getWidth() / 2);
						result.y = result.y + this.getHeight();
					}
					else if(this.locationOnComponent < SamGlobals.COMPONENT_TOP){
						//port on left
						if(this.portType == SamGlobals.PORT_TYPE_OUTPUT){
							result.x = parent.getX() - this.getWidth();
						}
						else{
							result.x = parent.getX();
						}
						
						result.y = parent.getY() + parent.getHeight() - ((locationOnComponent - SamGlobals.COMPONENT_LEFT + 1) * this.getHeight());

						result.y = result.y + (this.getHeight() / 2);
					}
					else if(this.locationOnComponent < SamGlobals.COMPONENT_RIGHT){
						//port on top
						if(this.portType == SamGlobals.PORT_TYPE_OUTPUT){
							result.y = parent.getY() - this.getHeight();
						}
						else{
							result.y = parent.getY();
						}
						
						result.x = parent.getX() + ((locationOnComponent - SamGlobals.COMPONENT_TOP) * this.getWidth());
						
						result.x = result.x + (this.getWidth() / 2);
					}
					else{
						//port on right (hopefully)
						if(this.portType == SamGlobals.PORT_TYPE_OUTPUT){
							result.x = parent.getX() + parent.getWidth();
						}
						else{
							result.x = parent.getX() + parent.getWidth() - this.getWidth();
						}
						
						result.y = parent.getY() + ((locationOnComponent - SamGlobals.COMPONENT_RIGHT) * this.getHeight());
						
						result.x = result.x + this.getWidth();
						result.y = result.y + (this.getHeight() / 2);
					}
				}
			}
		}
		
		return result;
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height){
		Rectangle b = this.getBounds();
		super.setBounds(b.x, b.y, b.width, b.height);
	}
	
	@Override
	public void setBounds(Rectangle bounds){
		Rectangle b = this.getBounds();
		super.setBounds(b);
	}
	
	//returns the bounds relative to type (input/output) and parent Component
	public Rectangle getBounds(){
		Rectangle result = new Rectangle(0,0,0,0);
		
		if(this.componentName.compareTo("") == 0){
			return result;
		}
		
		else{
			Component parent = this.getParentModel().getComponent(componentName);
			
			if(parent != null){
				if(parent.containsPort(this.getName())){
					if(this.locationOnComponent < SamGlobals.COMPONENT_BOTTOM){
						//error or bug? something not right
					}
					else if(this.locationOnComponent < SamGlobals.COMPONENT_LEFT){
						//port on bottom
						if(this.portType == SamGlobals.PORT_TYPE_OUTPUT){
							result.y = parent.getY() + parent.getHeight();
							this.startAngle = SamGlobals.PORT_BOTTOM_START_ANGLE;
						}
						else{
							result.y = parent.getY() + parent.getHeight() - this.getHeight();
							this.startAngle = SamGlobals.PORT_TOP_START_ANGLE;
						}
						
						result.x = parent.getX() + parent.getWidth() - ((locationOnComponent - SamGlobals.COMPONENT_BOTTOM + 1) * this.getWidth());
						result.width = SamGlobals.PORT_TOP_BOTTOM_WIDTH;
						result.height = SamGlobals.PORT_TOP_BOTTOM_HEIGHT;
					}
					else if(this.locationOnComponent < SamGlobals.COMPONENT_TOP){
						//port on left
						if(this.portType == SamGlobals.PORT_TYPE_OUTPUT){
							result.x = parent.getX() - this.getWidth();
							this.startAngle = SamGlobals.PORT_LEFT_START_ANGLE;
						}
						else{
							result.x = parent.getX();
							this.startAngle = SamGlobals.PORT_RIGHT_START_ANGLE;
						}
						result.width = SamGlobals.PORT_LEFT_RIGHT_WIDTH;
						result.height = SamGlobals.PORT_LEFT_RIGHT_HEIGHT;
						result.y = parent.getY() + parent.getHeight() - ((locationOnComponent - SamGlobals.COMPONENT_LEFT + 1) * this.getHeight());
					}
					else if(this.locationOnComponent < SamGlobals.COMPONENT_RIGHT){
						//port on top
						if(this.portType == SamGlobals.PORT_TYPE_OUTPUT){
							result.y = parent.getY() - this.getHeight();
							this.startAngle = SamGlobals.PORT_TOP_START_ANGLE;
						}
						else{
							result.y = parent.getY();
							this.startAngle = SamGlobals.PORT_BOTTOM_START_ANGLE;
						}
						result.width = SamGlobals.PORT_TOP_BOTTOM_WIDTH;
						result.height = SamGlobals.PORT_TOP_BOTTOM_HEIGHT;
						result.x = parent.getX() + ((locationOnComponent - SamGlobals.COMPONENT_TOP) * this.getWidth());
					}
					else{
						//port on right (hopefully)
						if(this.portType == SamGlobals.PORT_TYPE_OUTPUT){
							result.x = parent.getX() + parent.getWidth();
							this.startAngle = SamGlobals.PORT_RIGHT_START_ANGLE;
						}
						else{
							result.x = parent.getX() + parent.getWidth() - this.getWidth();
							this.startAngle = SamGlobals.PORT_LEFT_START_ANGLE;
						}
						result.width = SamGlobals.PORT_LEFT_RIGHT_WIDTH;
						result.height = SamGlobals.PORT_LEFT_RIGHT_HEIGHT;
						result.y = parent.getY() + ((locationOnComponent - SamGlobals.COMPONENT_RIGHT) * this.getHeight());
					}
				}
			}
		}
		
		
		return result;
				
	}
	
	public void delete(){
		super.delete();
		Component parent = this.getParentComponent();
		
		if(parent != null){
			parent.removePort(this.getName());
		}
		
		
	}
	
	//gets shape of port for drawing
	private GeneralPath getPortShape(){
		GeneralPath path = new GeneralPath();
		
		if(this.getParentModel() != null){
		
		Component parent = this.getParentModel().getComponent(componentName);
		
			if(parent != null){
				if(parent.containsPort(this.getName())){
					Rectangle dimensions = this.getBounds();
					this.setLocation(dimensions.x, dimensions.y);
					path.moveTo(0, 0);
					
					double width = dimensions.getWidth();
					double height = dimensions.getHeight();
					double x = 0;
					double y = 0;
					if(width < height){			
						if(startAngle == SamGlobals.PORT_RIGHT_START_ANGLE){
							x = -width;
						}
	
						width = width * 2;
					}
					else{	
						if(startAngle == SamGlobals.PORT_BOTTOM_START_ANGLE){
							y = -height;
						}
						height = height * 2;
					}
					
					path.append(new Arc2D.Double(x, y, width - 1, height - 1, this.startAngle, SamGlobals.PORT_ARC_SWEEP, Arc2D.OPEN), false);
					
				}
			}
		}
		return path;
	}
	
	public boolean removeArc(String name){
		if(arcName.compareTo(name) == 0){
			arcName = "";
			return true;
		}
		
		return false;
	}
	
	public void setPlaceInterface(String placeName){
		Place p = null;
		Place[] places = getPlaceListFromElementaryModel();
		for(int i=0; i<places.length; i++){
			if(places[i].getName().equals(placeName)){
				p = places[i];
				break;	
			}
		}
		if(p != null){
			this.placeInterface = p;
			this.isSetPlaceInterface = true;
		}
	}
	
	public boolean isSetPlaceInterface(){
		return this.isSetPlaceInterface;
	}
	
	public int getPlaceCountFromElementaryModel(){
		return this.getParentComponent().getElemNetModel().getPlacesCount();
	}
	
	private Place[] getPlaceListFromElementaryModel(){
		Place[] places = this.getParentComponent().getElemNetModel().getPlaces();
		return places;
	}
	
	public String[] getPlaceNameListFromElementaryModel(){
		String[] pn = null;
		Place[] places = getPlaceListFromElementaryModel();
		if(places !=null){
			int length = places.length;
			pn = new String[length];
			for(int i=0; i<length; i++){
				pn[i] = places[i].getName();
			}
		}	
		return pn;
	}
	
	public Place getPlaceInterface(){
		return this.placeInterface;
	}
	
	public boolean getIsSetPlaceInterface(){
		return this.isSetPlaceInterface;
	}
	
	public String getPlaceInterfaceName(){
		return this.placeInterface.getName();
	}

}
