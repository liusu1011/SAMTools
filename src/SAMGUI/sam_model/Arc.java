/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * contains everything for the arc
 * 
 * bounds always starts at (0,0)
 */
package SAMGUI.sam_model;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JComponent;

import SAMGUI.sam_controller.ArcMouseActionHandler;
import SAMGUI.sam_controller.MouseActionHandler;

import static java.awt.geom.AffineTransform.*;



public class Arc extends SamModelObject {
	private String start = "";
	private String end = "";
	
	private final int ARR_SIZE = 7;
	private final double pointHeight = 5;
	
	//first and last point are not kept in points, they are
	//always gotten from start and end objects
	private LinkedList<Point> points = new LinkedList<Point>();
	private int locationOnConnector = 0;
	
	//temp variable to keep track of point that is being moved
	private Point movingPoint;
	private int movingOffsetX = 0;
	private int movingOffsetY = 0;
	
	//variable on arc
	private String var = "";
	
	//variable string is kept inside lable, label is in super class
	public void setVariables(String variables){
		setLabelText(variables);
	}
	
	public String getVariables(){
		return label.getText();
	}
	
	//i don't think this constructor should really be used
	//if it is used you have to specify the parentModel and name with the
	//setters
	public Arc(){
		super();
		ArcMouseActionHandler mouseHandler = new ArcMouseActionHandler();
		
		this.addMouseListener(mouseHandler);
		this.addMouseMotionListener(mouseHandler);
	}
	
	public Arc(SamModel parent, String name){
		this();
		parentModel = parent;
		this.setName(name);
	}
	
	//must set name before adding to model
	public Arc(SamModel parent){
		this();
		parentModel = parent;
	}
	
	public void setPoints(LinkedList<Point> points){
		this.points = points;
	}
	
	public LinkedList<Point> getPoints(){
		return this.points;
	}
	
	//overwritten as a way to update bounds when drawing
	public void setBounds(int x, int y, int w, int h){
		Rectangle bounds = getLines().getBounds();
		super.setBounds(0, 0, bounds.x + bounds.width, bounds.y + bounds.height);
	}
	
	public void setBounds(Rectangle x){
		Rectangle bounds = getLines().getBounds();
		Rectangle relativeBounds = new Rectangle(0, 0, bounds.x + bounds.width, bounds.y + bounds.height);
		super.setBounds(relativeBounds);
	}
	
	@Override
	public Rectangle getBounds(){
		Rectangle bounds = getLines().getBounds();
		Rectangle relativeBounds = new Rectangle(0, 0, bounds.x + bounds.width, bounds.y + bounds.height);
		
		return relativeBounds;
	}
	
	public void addPoint(int x, int y){
		addPoint(new Point(x, y));
		
	}
	
	
	public void addPoint(Point p) {
		
		points.add(p);
		
	}
	
	//automatically gets point between end point and 
	//the point right before it and adds it to points
	public void addPoint(){
		Point p;
		
		if(points.isEmpty()){
			p = this.getStartPoint();
		}
		else{
			p = points.getLast();
		}
		
		points.add(getMidpoint(p));
	}
	
	public void removePoint(){
		if(points.size() > 0){
			points.removeLast();
		}
		
	}

	public boolean removePoint(Point p) {
		return points.remove(p);
	}

	public String getStartName() {
		return start;
	}

	public String getEndName() {
		return end;
	}

	public boolean setStart(String startName){
		return setStart(parentModel.getSamModelObject(startName));
	}
	
	public boolean setEnd(String endName){
		return setEnd(parentModel.getSamModelObject(endName));
	}

	//sets start object and verifies that is the correct type
	//and with correct settings
	public boolean setStart(SamModelObject o) {
		if(o != null){	
			if (o instanceof Port) {
				Port port = (Port) o;
				
				if(port.getPortType() == SamGlobals.PORT_TYPE_OUTPUT){
					
						if(end == null || end.compareTo("") == 0){
							this.start = o.getName();
							port.setArcName(this.getName());
							return true;
						}
						
						if(this.getEnd() instanceof Connector){
							this.start = o.getName();
							port.setArcName(this.getName());
							return true;
						}
					
				}
			}
			
			else if (o instanceof Connector) {
				Connector conn = (Connector) o;
				if(end == null || end.compareTo("") == 0){
					this.start = o.getName();
					conn.addArc(this.getName());
					return true;
				}
				
				if(this.getEnd() instanceof Port){
					Port port = (Port) this.getEnd();
					
					if(port.getPortType() == SamGlobals.PORT_TYPE_OUTPUT){
						this.start = o.getName();	
						conn.addArc(this.getName());
						return true;
					}
				}
				
			}
		}

		return false;
	}
	
	public void setStartFromFile(){
		
	}
	
	public void setEndFromFile(){
		
	}
	
	//sets end object and verifies that is of correct type and settings
	public boolean setEnd(SamModelObject o) {
		if(o != null){
			if (o instanceof Port) {
				Port port = (Port) o;
				
				if(port.getPortType() == SamGlobals.PORT_TYPE_INPUT){
				
						if(start == null || start.compareTo("") == 0){
							this.end = o.getName();
							port.setArcName(this.getName());
							return true;
						}
						
						if(this.getStart() instanceof Connector){
							this.end = o.getName();
							port.setArcName(this.getName());
							return true;
						}
				}
			}
			
			else if (o instanceof Connector) {
				Connector conn = (Connector) o;
				
				if(start == null || start.compareTo("") == 0){
					this.end = o.getName();
					conn.addArc(this.getName());
					return true;
				}
				//debug
				SamModelObject p = this.getStart();
				
				if(this.getStart() instanceof Port){
					this.end = o.getName();	
					conn.addArc(this.getName());
					return true;
					
				}
				
			}
		}
		getStart().removeArc(this.getName());

		return false;
	}

	public SamModelObject getEnd(){
		return parentModel.getSamModelObject(end);
	}
	
	public SamModelObject getStart(){
		return parentModel.getSamModelObject(start);
	}
	
	
	//gets shape for arrow at end of line
	//for parameters x1,y1 is location of first point and 2 is 
	//last point
	private GeneralPath getArrow(int x1, int y1, int x2, int y2) {
        GeneralPath path = new GeneralPath();
        path.moveTo(0, 0);
        

        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform at = getTranslateInstance(x1, y1);
        at.concatenate(getRotateInstance(angle));
        

        // Draw horizontal arrow starting in (0, 0)
        
        Polygon arrowHead = new Polygon();  
        arrowHead.addPoint( 0,5);
        arrowHead.addPoint( -5, -5);
        arrowHead.addPoint( 5,-5);
        
        path.moveTo((int) len, 0);
        
        Polygon arrow = new Polygon(new int[] {len, len-(ARR_SIZE*2), len-(ARR_SIZE*2), len},
                new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
        path.append(arrow, false);
        path.transform(at);
		return path;
    }
	
	public Point getStartPoint(){
		SamModelObject o = this.getStart();
		if(o instanceof Port){
			Port port = (Port) o;
			return port.getArcPoint();
		}
		if(o instanceof Connector){
			Connector connector = (Connector) o;
			return connector.getArcPoint(locationOnConnector);
		}
		
		return null;
	}
	
	public Point getEndPoint(){
		SamModelObject o = this.getEnd();
		if(o instanceof Port){
			Port port = (Port) o;
			return port.getArcPoint();
		}
		if(o instanceof Connector){
			Connector connector = (Connector) o;
			return connector.getArcPoint(locationOnConnector);
		}
		
		return null;
	}
	
	public int getLocationOnConnector() {
		return locationOnConnector;
	}

	public void setLocationOnConnector(int locationOnConnector) {
		this.locationOnConnector = locationOnConnector;
	}
	
	//overwritten so only line registers clicks
	public boolean contains(int x, int y){
		Area check = new Area(getLines());
		return check.contains(new Point2D.Double(x, y));
	}
	
	public boolean contains(Point p){
		Area check = new Area(getLines());
		return check.contains(new Point2D.Double(p.x, p.y));
	}
	
    public void paintComponent(Graphics g){
    	super.paintComponent(g);
    	Graphics2D g2 = (Graphics2D) g;
        GeneralPath p = getLines();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.draw(p);
        g2.fill(p);
        
    }
    
    //gets shape of all the lines and points put together with arrow at end
    private GeneralPath getLines(){
    	GeneralPath path = new GeneralPath();
    	
    	
    	Point start = this.getStartPoint();
    	Point last = this.getEndPoint();
    	if(start == null || last == null){
    		return path;
    	}
    	appendShapeWithPoint(start, path);
    	path.moveTo(start.x, start.y);
    	
    	Point prev = start;
    	for(Point p: points){
    		path.lineTo(p.x, p.y);
    		appendShapeWithPoint(p, path);
    		path.moveTo(p.x, p.y);
    		
    		prev = p;
    	}
    	
    	
    	path.lineTo(last.x, last.y);
    	path.append(getArrow(prev.x, prev.y, last.x, last.y), false);
        return path;
        
    }
    
    //helper to add a point (square box) to line
    private void appendShapeWithPoint(Point p, GeneralPath path){
    	path.moveTo(p.x, p.y);
    	path.append(getPointShape(p), false);
    }
    
    
    public int getPointCount(){
    	return points.size();
    }
    
    //returns true if p should be draggable with a mouse
    //used for checking if arc should be draggable
    //
    //also sets moving point to the point that corressponds
    //so that moving point can be changed and will be shown as
    //being dragged
    public boolean containsMovablePoint(Point p){
    	for(Point point: points){
    		Area check = new Area(getPointShape(point));
    		if(check.contains(new Point2D.Double(p.x, p.y))){
    			movingPoint = point;
    			movingOffsetX = point.x - p.x;
    			movingOffsetY = point.y - p.y;
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    //returns the shape of a point on line
    public Rectangle2D.Double getPointShape(Point p){
    	return new Rectangle2D.Double(p.x - 2, p.y - 2, pointHeight, pointHeight);
    }
    
    //updates bounds for drawing purposes, sometimes needs
    //to be called when repainting
    public void updateBounds(){
    	this.setBounds(null);
    }
    
    //overwritten so you can only set location of movingPoint
    @Override
    public void setLocation(Point p){
    	if(movingPoint == null){
    		return;
    	}
    	
    	movingPoint.x = p.x + movingOffsetX;
    	movingPoint.y = p.y + movingOffsetY;
    }
    
    @Override
    public void setLocation(int x, int y){
    	if(movingPoint == null){
    		return;
    	}
    	
    	movingPoint.x = x;
    	movingPoint.y = y;
    }
    
    //removes arc from start and end
    public void delete(){
    	super.delete();
    	SamModelObject start = this.getStart();
    	SamModelObject end = this.getEnd();
    	
    	if(start != null){
    		start.removeArc(this.getName());
    	}
    	
    	if(end != null){
    		end.removeArc(this.getName());
    	}
    }
    
    public void addNotify() {
		super.addNotify();
		this.updateBounds();
		
		if(!isLabelSet){
			this.setInitialLabelLocation();
		}
	} 
	
	public void setInitialLabelLocation(){
		Point p = getMidpoint();
		
		label.setBounds(p.x, p.y,
				label.getPreferredSize().width,
				label.getPreferredSize().height);
	}
	
	//returns midpoint from start to end
	private Point getMidpoint(){
		Point p = new Point();
		
		Point start = this.getStartPoint();
		Point end = this.getEndPoint();
		
		int y = start.y + (end.y - start.y) / 2;
		int x = start.x + (end.x - start.x) / 2;
		
		if(y < 0){
			y = y * -1;
		}
		
		if(x < 0){
			x = x * -1;
		}
		
		p.x = x;
		p.y = y;
		
		return p;
	}
	
	//returns midpoint from parameter to end
	private Point getMidpoint(Point start){
		Point p = new Point();
		
		Point end = this.getEndPoint();
		
		int y = start.y + (end.y - start.y) / 2;
		int x = start.x + (end.x - start.x) / 2;
		
		if(y < 0){
			y = y * -1;
		}
		
		if(x < 0){
			x = x * -1;
		}
		
		p.x = x;
		p.y = y;
		
		return p;
	}

	public void setVar(String _var){
		this.var = _var;
	}
	
	public String getVar(){
		return this.var;
	}



	
	


}
