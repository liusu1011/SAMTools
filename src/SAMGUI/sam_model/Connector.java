/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * 
 */
package SAMGUI.sam_model;

import java.awt.Graphics;
import java.awt.Point;
import java.util.LinkedList;

public class Connector extends RectangleObject {
	private final int MAX_POINTS = 10;
	private final int LOCATIONS_ON_SIDE = 4;
	private String formula = "";
	
	//use to convert sam connector to regular transition. specified by user.
	private String netCompositionFormla;
	
	LinkedList<String> arcs = new LinkedList<String>();
	

	public Connector() {
		super();
		this.setBounds(SamGlobals.CONNECTOR);
		
	}

	public Connector(String s) {
		this();
		super.setName(s);

	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}
	
	public String getNetCompositionFormla(){
		return this.netCompositionFormla;
	}
	
	public void setNetCompositionFormula(String _formula){
		this.netCompositionFormla = _formula;
	}

	public void addArc(String name) {

		arcs.add(name);

	}

	public LinkedList<String> getArcs() {

		return arcs;

	}

	public boolean removeArc(String name) {

		return arcs.remove(name);

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.fillRect(0, 0, (int) this.getBounds().getWidth() - 1, (int) this
				.getBounds().getHeight() - 1);
	}
	
	//returns point where an arc should connect with this depending on 
	//the location on this
	public Point getArcPoint(int locationOnConnector){
		Point p = new Point();
		
		if(locationOnConnector < 0 || locationOnConnector > MAX_POINTS){
			return null;
		}
		
		else if(locationOnConnector < SamGlobals.CONNECTOR_LEFT){
			//arc on bottom of connector
			p.x = this.getX() + this.getWidth()/2;
			p.y = this.getY() + this.getHeight();
			
		}
		else if(locationOnConnector < SamGlobals.CONNECTOR_TOP){
			//arc on left of connector
			p.x = this.getX();
			p.y = this.getY() + this.getHeight() - ((locationOnConnector - SamGlobals.CONNECTOR_LEFT) * (this.getHeight() / LOCATIONS_ON_SIDE));
			
			
			
		}
		else if(locationOnConnector < SamGlobals.CONNECTOR_RIGHT){
			//arc on top of connector
			p.x = this.getX() +  this.getWidth()/2;
			p.y = this.getY();
			
		}
		else{
			//arc on right of connector
			p.x = this.getX() + this.getWidth();
			p.y = this.getY() + (locationOnConnector - SamGlobals.CONNECTOR_RIGHT) * (this.getHeight() / LOCATIONS_ON_SIDE);
		}
		
		return p;
	}
	
	//updates bounds for arcs
	public void updateChildren(){
		for(String a: arcs){
			Arc arc = this.getParentModel().getArc(a);
			if(arc != null){
				arc.updateBounds();
				arc.setInitialLabelLocation();
			}
			
		}
	}
	
	public void delete(){
		super.delete();
		arcs.clear();
		arcs = null;
	}
	
	
}
