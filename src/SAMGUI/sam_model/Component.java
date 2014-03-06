/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * 
 */
package SAMGUI.sam_model;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPopupMenu;
import javax.xml.transform.TransformerConfigurationException;

import SAMGUI.sam_controller.CanvasAddMouseHandler;
import SAMGUI.sam_controller.DeleteFromMenuHandler;
import SAMGUI.sam_controller.SamPopupMenu;
import org.w3c.dom.Document;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PNMLTransformer;
import pipe.dataLayer.Place;

public class Component extends RectangleObject {

	public static final int MAX_PORTS = 16;
	private String formula ="";
	
	//keeps track of names of ports and their locations
	private String[] ports = new String[MAX_PORTS];
	
	//keeps track of when ports are set... i don't remember why i needed this
	//could possibly be removed and ports[i] checked so it doesnt equal null
	private boolean[] portSettings = new boolean[MAX_PORTS];
	
	//lower level composition
	public SamModel subCompositionModel;
	public boolean isSetSubComposition = false;
	
	//elementary level net specification
	public Document elemNetSpecModel; 
	public DataLayer elemNetDataLayerModel;
	public boolean isSetElemNetSpec = false;
	

	// default constructor
	public Component() {
		super();
		this.setBounds(SamGlobals.COMPONENT);
		

	}
	
	//returns port and location index, if no port null
	public Port getPort(int index){
		String name = ports[index];
		Port port = null;
		if(name != null){
			port = parentModel.getPort(name);
		}
		
		return port;
	}
	
	//checks if this contains a port with name:name
	public boolean containsPort(String name){
		for(String x: ports){
			if(x != null && x.compareTo(name) == 0){
				return true;
			}
		}
		
		return false;
	}

	// adds a new string containing the name of a port to the list of ports in
	// this component, return -1 if no place found, index otherwise
	public int addPort(String name) {
		for(int i = 0; i < ports.length; i++){
			if(!portSettings[i]){
				portSettings[i] = true;
				ports[i] = name;
				
				Port p = getParentModel().getPort(name);
				if(p != null){
					p.setComponentName(this.getName());
					p.setLocationOnComponent(i);
					p.updateBounds();
				}
				
				
				return i;
				
				
			}
		}
		
		return -1;
		
	}

	// removes the specified port from the list of ports in this component
	public boolean removePort(String name) {
		for(int i = 0; i < ports.length; i++){
			String x = ports[i];
			if(x != null && x.compareTo(name) == 0){
				ports[i] = null;
				portSettings[i] = false;
				return true;
			}
		}
		
		return false;
	}

	// returns the list of names of the ports connected to this component
	public String[] getPorts() {
		return ports;
	}
	
	public void setPorts(String[] ports){
		this.ports = ports;
		
		for(int i = 0; i < ports.length; i++){
			if(this.ports[i] != null){
				this.portSettings[i] = true;
			}
			else{
				this.portSettings[i] = false;
			}
		}
	}

	// sets the temporal logic formula of this component to the specified string
	// s
	public void setFormula(String s) {
		formula = s;
	}

	// returns the temporal logic formula stored within this component
	public String getFormula() {
		return formula;
	}

	public void delete() {
		super.delete();
		
		portSettings = null;
		ports = null;

		formula = null;
	}
	
	public void swapPortLocations(int x, int y){
		
		if(x < ports.length && y < ports.length){
		
			String tempStr = ports[y];
			boolean tempBln = portSettings[y];
			
			ports[y] = ports[x];
			portSettings[y] = portSettings[x];
			
			ports[x] = tempStr;
			portSettings[x] = tempBln;
			
			Port portY = parentModel.getPort(ports[x]);
			
			if(portY != null){
				//update portY with new location
				portY.setLocationOnComponent(x);
				portY.setLabelLocation();
				portY.updateBounds();
			}
		}
		
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.drawRect(0, 0, (int) this.getBounds().getWidth() - 1, (int) this
				.getBounds().getHeight() - 1);
	}
	
	public boolean isFull(){
		for(boolean x: portSettings){
			if(!x){
				return false;
			}
		}
		
		return true;
	}
	
	//updates the location of ports inside for painting purposes
	public void updateChildren(){
		for(String p: ports){
			if(p != null){
				Port port = this.getParentModel().getPort(p);
				if(port != null){
					port.updateBounds();
					port.setLabelLocation();
				}
			}
		}
	}
	
	//overwritten so when port is input it is still clickable
	public boolean contains(int x, int y){
		
		if(x > SamGlobals.PORT_LEFT_RIGHT_WIDTH && x < this.getWidth() - SamGlobals.PORT_LEFT_RIGHT_WIDTH && y > SamGlobals.PORT_TOP_BOTTOM_HEIGHT && y < this.getHeight() - SamGlobals.PORT_TOP_BOTTOM_HEIGHT){
			return true;
		}
		
		for(int i = 0; i < ports.length; i++){
			Port port = this.getPort(i);
			if(port != null){
				Area area = new Area(port.getBounds());
				if(area.contains(new Point2D.Double(x + this.getX(), y + this.getY()))){
					return false;
				}
			}
		}
		
		return super.contains(x, y);
	}
	
	public boolean contains(Point p){
		return contains(p.x, p.y);
	}
	
	public void setElemSpec(Document pnDom){
		this.elemNetSpecModel = pnDom;
		this.isSetElemNetSpec = true;
	}
	
	public Document getElemSpecModel(){
		return this.elemNetSpecModel;
	}
	
	/**
	 * Elementary net, from document xml to model
	 * used while loading a xml file;
	 * @param pnDom
	 * @return
	 */
	public DataLayer elemPNDomToModel(Document pnDom){
		DataLayer model = new DataLayer();
		PNMLTransformer transformer = new PNMLTransformer();
		try {
			model.createFromPNML(transformer.transformPNML(pnDom));
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		return model;
	}
	
	public void setElemNetModel(DataLayer model){
		this.elemNetDataLayerModel = model;
	}
	
	public DataLayer getElemNetModel(){
		return this.elemNetDataLayerModel;
	}
	
	public void setSubCompositionModel(SamModel model){
		this.subCompositionModel = model;
		this.isSetSubComposition = true;
	}
	
	public SamModel getSubComposiionModel(){
		return this.subCompositionModel;
	}

}
